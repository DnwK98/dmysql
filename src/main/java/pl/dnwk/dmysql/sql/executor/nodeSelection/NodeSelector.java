package pl.dnwk.dmysql.sql.executor.nodeSelection;

import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.statement.ast.Conditional;
import pl.dnwk.dmysql.sql.statement.identificationVariables.IdentificationVariables;

public interface NodeSelector {
    NodeSelector INSTANCE = new AggregateNodeSelector(new NodeSelector[]{
            new ComparisonNodeSelector(),
            new IntersectionNodeSelector(),
            new InListNodeSelector(),
    });

    String[] select(Conditional condition, IdentificationVariables identificationVariables, DistributedSchema schema, String[] nodesNames);
}
