package pl.dnwk.dmysql.sql.executor.select;

import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.common.ArrayBuilder;
import pl.dnwk.dmysql.common.DeepCopy;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.executor.Result;
import pl.dnwk.dmysql.sql.executor.select.aggregation.Aggregation;
import pl.dnwk.dmysql.sql.executor.select.aggregation.AnyValue;
import pl.dnwk.dmysql.sql.executor.nodeSelection.NodeSelector;
import pl.dnwk.dmysql.sql.statement.SqlWalker;
import pl.dnwk.dmysql.sql.statement.ast.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SelectExecutor {

    private final DistributedSchema schema;
    private final Nodes nodes;
    private final SqlWalker sqlWalker = new SqlWalker();
    private final JoinRestrictor joinRestrictor;

    public SelectExecutor(DistributedSchema schema, Nodes nodes) {
        this.schema = schema;
        this.nodes = nodes;
        this.joinRestrictor = new JoinRestrictor(schema);
    }

    public Result execute(SelectStatement originalStatement) {
        try {
            // Not all Joins are allowed. Restrict them.
            joinRestrictor.restrict(originalStatement);

            var statement = modifyAggregationStatement(originalStatement);
            var result = ArrayBuilder.create2D();

            // Get statements to execute on each node
            Map<String, String> nodesStatements = getNodesStatements(statement);

            // Execute statements on nodes
            nodes.executeQuery(nodesStatements).values().stream()
                    .map(SelectExecutor::normalizeResult)
                    .forEach(result::addAll);

            // Group results
            var resultArray = result.toArray();
            if (originalStatement.groupByClause != null) {
                resultArray = applyGroupBy(originalStatement, resultArray);
            } else if (hasAggregation(statement)) {
                resultArray = new Object[][]{applyAggregation(originalStatement, resultArray)};
            }

            // Order results
            resultArray = applyOrderBy(originalStatement, resultArray);


            return Result.table(
                    statement.identificationVariables.getFieldsAliases(),
                    resultArray
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, String> getNodesStatements(SelectStatement statement) {
        var map = new HashMap<String, String>();

        // Select table to check shard (from, join)
        var shardTable = schema.get(statement.fromClause.table);
        if (!shardTable.sharded) {
            shardTable = null;
            for (Join join : statement.fromClause.joins) {
                shardTable = schema.get(join.table);
                if (shardTable.sharded) {
                    if (join.type.equals(Join.TYPE_LEFT)) {
                        // There we try to LEFT JOIN table which is on one node to table which is on all shards.
                        // It is possible but requires two queries:
                        //   1) INNER JOIN - to all shards for all joined objects
                        //   2) LEFT JOIN ON b.key = null - to random shard for all objects without left side only once
                        // and then merge these responses with removal duplicates from second one.

                        throw new RuntimeException("LEFT JOIN of sharded table to table on all shards is not supported yet.");
                    }

                    break;
                } else {
                    shardTable = null;
                }
            }
        }

        // If there is any shard, use random node
        if (shardTable == null) {
            String[] keys = nodes.names().toArray(new String[0]);
            int randomKeyPos = new Random().nextInt(keys.length);
            String key = keys[randomKeyPos];
            map.put(key, sqlWalker.walkStatement(statement));

            return map;
        }

        // If there is shard table and where clause, execute only against certain nodes
        if (statement.whereClause != null) {
            var selected = NodeSelector.INSTANCE.select(
                    statement.whereClause.expression,
                    statement.identificationVariables,
                    schema,
                    nodes.names().toArray(new String[0])
            );
            if (selected != null) {
                for (var singleSelected : selected) {
                    map.put(singleSelected, sqlWalker.walkStatement(statement));
                }

                return map;
            }
        }

        // Execute against all nodes
        for(var node: nodes.names()) {
            map.put(node, sqlWalker.walkStatement(statement));
        }

        return map;
    }

    private static Object[][] applyGroupBy(SelectStatement statement, Object[][] result) {
        var groupMap = new HashMap<String, ArrayList<Object[]>>();

        for (var row : result) {
            String groupHash = "|";
            for (var groupItem : statement.groupByClause.items) {
                var g = statement.identificationVariables.getField(groupItem.toString()).column - 1;
                groupHash += row[g] + "|";
            }

            var grouped = groupMap.getOrDefault(groupHash, new ArrayList<>());
            grouped.add(row);
            groupMap.put(groupHash, grouped);
        }

        var groupedResults = new Object[groupMap.size()][statement.selectClause.selectExpressions.size()];
        var i = 0;
        for (var groupList : groupMap.values()) {
            Object[][] group = groupList.toArray(new Object[groupList.size()][]);
            groupedResults[i++] = applyAggregation(statement, group);
        }

        return groupedResults;
    }

    private static boolean hasAggregation(SelectStatement statement) {
        for (var select : statement.selectClause.selectExpressions) {
            if (select.expression instanceof Function) {
                var f = (Function) select.expression;
                if (Aggregation.map.containsKey(f.functionName.toUpperCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Object[] applyAggregation(SelectStatement statement, Object[][] group) {
        if (group.length == 0) {
            return group;
        }

        var aggregated = DeepCopy.copy(group[0]); // Copy is not required there. Used only for function pureness.

        var i = 0;
        for (var columnDef : statement.selectClause.selectExpressions) {
            var column = new Object[group.length];
            var j = 0;
            for (var row : group) {
                column[j++] = row[i];
            }

            Aggregation aggregationAlgorithm = new AnyValue();
            if (columnDef.expression instanceof Function) {
                var f = (Function) columnDef.expression;
                if (Aggregation.map.containsKey(f.functionName)) {
                    aggregationAlgorithm = Aggregation.map.get(f.functionName);
                }
            }

            aggregated[i] = aggregationAlgorithm.aggregate(column);

            ++i;
        }

        return aggregated;
    }

    private static SelectStatement modifyAggregationStatement(SelectStatement originalStatement) {
        var statement = DeepCopy.copy(originalStatement);
        for (var select : statement.selectClause.selectExpressions) {
            if (select.expression instanceof Function) {
                var f = (Function) select.expression;
                if (Aggregation.map.containsKey(f.functionName)) {
                    select.expression = Aggregation.map.get(f.functionName).getExpression(f);
                }
            }
        }

        return statement;
    }

    private static Object[][] applyOrderBy(SelectStatement statement, Object[][] result) {
        var orderByColumns = ArrayBuilder.create(new RowsComparator.ColumnDef[16]);
        if (statement.orderByClause != null) {
            for (OrderByItem orderByItem : statement.orderByClause.items) {
                var descending = orderByItem.direction.equals(OrderByItem.DESC);
                var columnPos = statement.identificationVariables.getField(orderByItem.orderBy.toString()).column - 1;

                orderByColumns.add(new RowsComparator.ColumnDef(columnPos, descending));
            }
        }

        if (orderByColumns.empty()) {
            orderByColumns.add(new RowsComparator.ColumnDef(0));
        }

        result = DeepCopy.copy(result); // copy only for pureness.
        Arrays.sort(result, new RowsComparator(orderByColumns.toArray()));

        return result;
    }

    private static Object[][] normalizeResult(ResultSet result) {
        try {
            var rowMapper = RowMapper.ofResult(result);
            var rows = ArrayBuilder.create2D();

            while (result.next()) {
                rows.add(rowMapper.mapRow(result));
            }

            return rows.toArray();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
