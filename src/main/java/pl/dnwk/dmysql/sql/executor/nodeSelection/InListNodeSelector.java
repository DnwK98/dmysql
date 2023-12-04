package pl.dnwk.dmysql.sql.executor.nodeSelection;

import pl.dnwk.dmysql.common.ArrayBuilder;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.statement.ast.*;
import pl.dnwk.dmysql.sql.statement.identificationVariables.IdentificationVariables;

public class InListNodeSelector implements NodeSelector {

    @Override
    public String[] select(Conditional condition, IdentificationVariables identificationVariables, DistributedSchema schema, String[] allNodes) {
        if (!(condition instanceof InListCondition)) {
            // Other selector can select
            return null;
        }
        var expr = (InListCondition) condition;

        if (expr.not) {
            // Inverted condition is not supported
            // Other selector can select.
            return null;
        }

        PathExpression path = expr.path;
        var values = expr.literals.stream().map(l -> l.value).toArray(String[]::new);

        var tableName = identificationVariables.getField(path.toString()).table.name;
        var table = schema.get(tableName);

        if (!table.sharded || !table.shardKey.column().equals(path.field)) {
            // Table in this comparison is not sharded, or sharded by other colum.
            // Let other selector run.
            return null;
        }

        var shardKey = table.shardKey;
        var nodesCount = allNodes.length;

        var selectedShards = ArrayBuilder.create(new String[8]);
        int nodeId = -1;
        for (var nodeName : allNodes) {
            ++nodeId;
            for (var val : values) {
                if (shardKey.pick(val, nodesCount) == nodeId) {
                    selectedShards.add(nodeName);
                    break;
                }
            }
        }

        return selectedShards.toArray();
    }
}
