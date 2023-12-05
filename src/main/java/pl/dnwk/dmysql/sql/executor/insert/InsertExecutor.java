package pl.dnwk.dmysql.sql.executor.insert;

import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.common.DeepCopy;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.executor.Result;
import pl.dnwk.dmysql.sql.statement.SqlWalker;
import pl.dnwk.dmysql.sql.statement.ast.InsertStatement;
import pl.dnwk.dmysql.sql.statement.ast.InsertValue;
import pl.dnwk.dmysql.sql.statement.ast.Literal;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InsertExecutor {
    private final DistributedSchema schema;
    private final Nodes nodes;
    private final SqlWalker sqlWalker = new SqlWalker();

    public InsertExecutor(DistributedSchema schema, Nodes nodes) {
        this.schema = schema;
        this.nodes = nodes;
    }

    public Result execute(InsertStatement statement) {

        var nodeNumber = -1;

        var statementsToNodes = new HashMap<String, String>();
        for (String nodeName : nodes.names()) {
            ++nodeNumber;
            var nodeStatement = restrictRecordsForNode(statement, nodeNumber);
            if(nodeStatement == null) {
                continue;
            }
            String sql = sqlWalker.walkStatement(nodeStatement);
            statementsToNodes.put(nodeName, sql);
        }
        nodes.executeStatement(statementsToNodes);

        return Result.text("OK");
    }

    private InsertStatement restrictRecordsForNode(InsertStatement statement, int nodeNumber) {
        var tableSchema = schema.get(statement.table);
        if(!tableSchema.sharded) {
            return statement;
        }

        var _statement = DeepCopy.copy(statement);

        Integer idField = null;
        for(String columnName: _statement.columns) {
            var field = _statement.identificationVariables.getField(columnName);
            if(field.name.equals(tableSchema.shardKey.column())) {
                idField = field.column - 1;
            }
        }
        if(idField == null) {
            throw new RuntimeException("Missing shard identification for sharded table in INSERT columns");
        }

        List<InsertValue> valuesForShard = new ArrayList<>();
        for(InsertValue value: _statement.values) {
            var idFieldVal = value.columnsValues.toArray(new Literal[0])[idField].value;
            if(tableSchema.shardKey.pick(idFieldVal, nodes.names().size()) == nodeNumber) {
                valuesForShard.add(value);
            }
        }

        if(valuesForShard.isEmpty()) {
            return null;
        }

        _statement.values = valuesForShard;

        return _statement;
    }
}
