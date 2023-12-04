package pl.dnwk.dmysql.sql.executor.nodeSelection;

import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.statement.ast.Conditional;
import pl.dnwk.dmysql.sql.statement.identificationVariables.IdentificationVariables;

public class AggregateNodeSelector implements NodeSelector {

    private final NodeSelector[] selectors;

    public AggregateNodeSelector(NodeSelector[] selectors) {
        this.selectors = selectors;
    }

    @Override
    public String[] select(Conditional condition, IdentificationVariables identificationVariables, DistributedSchema schema, String[] nodesNames) {
       for(var selector: selectors) {
           var selected = selector.select(condition, identificationVariables, schema, nodesNames);
           if(selected != null) {
               return selected;
           }
       }

       return null;
    }
}
