package pl.dnwk.dmysql.sql.executor.delete;

import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.executor.Result;
import pl.dnwk.dmysql.sql.executor.nodeSelection.NodeSelector;
import pl.dnwk.dmysql.sql.statement.SqlWalker;
import pl.dnwk.dmysql.sql.statement.ast.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

public class DeleteExecutor {
    private final DistributedSchema schema;
    private final Nodes nodes;
    private final SqlWalker sqlWalker = new SqlWalker();

    public DeleteExecutor(DistributedSchema schema, Nodes nodes) {
        this.schema = schema;
        this.nodes = nodes;
    }

    public Result execute(DeleteStatement statement) {

        var nodesStatements = restrictStatementsToNodes(statement);
        for (String nodeName : nodesStatements.keySet()) {
            DeleteStatement nodeStatement = nodesStatements.get(nodeName);
            String sql = sqlWalker.walkStatement(nodeStatement);
            executeStatement(nodes.get(nodeName), sql);
            Log.debug("Execute statement on node (" + nodeName + ") SQL: " + sql);
        }

        return Result.text("OK");
    }

    private HashMap<String, DeleteStatement> restrictStatementsToNodes(DeleteStatement statement) {
        var map = new HashMap<String, DeleteStatement>();
        if (statement.whereClause != null) {
            var selected = NodeSelector.INSTANCE.select(
                    statement.whereClause.expression,
                    statement.identificationVariables,
                    schema,
                    nodes.names().toArray(new String[0])
            );
            if (selected != null) {
                for (var singleSelected : selected) {
                    map.put(singleSelected, statement);
                }

                return map;
            }
        }

        // Execute against all nodes
        for(var node: nodes.names()) {
            map.put(node, statement);
        }

        return map;
    }

    private static void executeStatement(Connection connection, String sql) {
        try {
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
