package pl.dnwk.dmysql.sql.executor.select;

import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.statement.ast.*;
import pl.dnwk.dmysql.sql.statement.identificationVariables.IdentificationVariables;

public class JoinRestrictor {

    private final DistributedSchema schema;

    public JoinRestrictor(DistributedSchema schema) {
        this.schema = schema;
    }

    void restrict(SelectStatement statement) {
        for (Join join : statement.fromClause.joins) {
            var ok = false;
            if (join.condition instanceof ComparisonCondition) {
                var condition = (ComparisonCondition) join.condition;
                if (joinOnIsAllowed(condition, statement.identificationVariables)) {
                    ok = true;
                }
            }

            if (join.condition instanceof ConditionalTerm) {
                var condition = (ConditionalTerm) join.condition;
                for (Conditional factor : condition.conditionalFactors) {
                    if (factor instanceof ComparisonCondition) {
                        if (joinOnIsAllowed((ComparisonCondition) factor, statement.identificationVariables)) {
                            ok = true;
                        }
                    }
                }
            }
            if (!ok) {
                throw new RuntimeException("JOIN " + join.table + " is not allowed, because condition doesn't contain same sharding key.");
            }
        }
    }

    private boolean joinOnIsAllowed(ComparisonCondition condition, IdentificationVariables identificationVariables) {
        if (!(condition.left instanceof PathExpression) || !(condition.right instanceof PathExpression)) {
            return false;
        }

        var left = (PathExpression) condition.left;
        var right = (PathExpression) condition.right;

        var leftField = identificationVariables.getField(left.toString());
        var rightField = identificationVariables.getField(right.toString());

        var leftSchema = schema.get(leftField.table.name);
        var rightSchema = schema.get(rightField.table.name);

        // Allow JOIN between tables where one of them is not sharded.
        if (!leftSchema.sharded || !rightSchema.sharded) {
            return true;
        }

        // Disallow JOIN when both tables are sharded, and in condition
        // shardKey is not used
        if (!leftSchema.shardKey.column().equals(leftField.name)) {
            return false;
        }
        if (!rightSchema.shardKey.column().equals(rightField.name)) {
            return false;
        }

        return true;
    }
}
