package pl.dnwk.dmysql.sql.executor.nodeSelection;

import pl.dnwk.dmysql.common.ArrayBuilder;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.statement.ast.*;
import pl.dnwk.dmysql.sql.statement.identificationVariables.IdentificationVariables;

import java.util.Arrays;
import java.util.stream.Stream;

public class IntersectionNodeSelector implements NodeSelector {

    @Override
    public String[] select(Conditional condition, IdentificationVariables identificationVariables, DistributedSchema schema, String[] allNodes) {
        if (!(condition instanceof ConditionalTerm)) {
            // Other selector can select
            return null;
        }
        var expr = (ConditionalTerm) condition;

        var selected = ArrayBuilder.create(new String[8][]);

        for (Conditional factor : expr.conditionalFactors) {
            var singleSelected = NodeSelector.INSTANCE.select(factor, identificationVariables, schema, allNodes);
            if (singleSelected != null) {
                selected.add(singleSelected);
            }
        }

        if (selected.empty()) {
            return null;
        }

        return intersection(selected.toArray());
    }

    private static String[] intersection(String[][] arrays){
        if(arrays.length == 0) {
            return new String[0];
        }

        var stream = Stream.of(arrays[0]);
        for(var array: arrays) {
            stream = stream.filter(Arrays.asList(array)::contains);
        }

        return stream.toArray(String[]::new);
    }
}
