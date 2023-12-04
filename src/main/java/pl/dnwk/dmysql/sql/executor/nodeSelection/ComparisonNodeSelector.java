package pl.dnwk.dmysql.sql.executor.nodeSelection;

import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.statement.ast.*;
import pl.dnwk.dmysql.sql.statement.identificationVariables.IdentificationVariables;

public class ComparisonNodeSelector implements NodeSelector {

    @Override
    public String[] select(Conditional condition, IdentificationVariables identificationVariables, DistributedSchema schema, String[] allNodes) {
       if (!(condition instanceof ComparisonCondition)) {
            // Other selector can select
            return null;
        }
        var expr = (ComparisonCondition) condition;

        if (!expr.operator.equals("=")) {
            // It was comparison, but other than equal.
            // Other selector can select.
            return null;
        }

        PathExpression path = null;
        Literal val = null;
        if (expr.left instanceof PathExpression) {
            path = (PathExpression) expr.left;
        }
        if (expr.right instanceof PathExpression) {
            path = (PathExpression) expr.right;
        }
        if (expr.right instanceof Literal) {
            val = (Literal) expr.right;
        }
        if (expr.left instanceof Literal) {
            val = (Literal) expr.left;
        }

        if (path == null || val == null) {
            // It is comparison but not path = val
            // Let other selector run.
            return null;
        }

        var tableName = identificationVariables.getField(path.toString()).table.name;
        var table = schema.get(tableName);

        if (!table.sharded || !table.shardKey.column().equals(path.field)) {
            // Table in this comparison is not sharded, or sharded by other colum.
            // Let other selector run.
            return null;
        }

        var shardKey = table.shardKey;
        var nodesCount = allNodes.length;

        int nodeId = -1;
        for(var nodeName: allNodes) {
            ++nodeId;
            if(shardKey.pick(val.value, nodesCount) == nodeId) {
                // It is single comparison, so it can be only on one node
                // Return this node.
                return new String[]{nodeName};
            }
        }

        // This is not expected. Node should be selected already.
        throw new RuntimeException("Unexpected state. Node not selected in ComparisonShardSelector");
    }
}
