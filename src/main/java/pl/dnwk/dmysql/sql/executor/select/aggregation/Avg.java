package pl.dnwk.dmysql.sql.executor.select.aggregation;

import pl.dnwk.dmysql.sql.statement.ast.Function;
import pl.dnwk.dmysql.sql.statement.ast.Literal;
import pl.dnwk.dmysql.sql.statement.ast.ValueExpression;

public class Avg implements Aggregation {
    @Override
    public ValueExpression getExpression(Function f) {
        return Function.create("CONCAT", new ValueExpression[]{
                Function.create("SUM", new ValueExpression[]{f.arguments.get(0)}),
                Literal.String("#"),
                Function.create("COUNT", new ValueExpression[]{f.arguments.get(0)})
        });
    }

    public Object aggregate(Object[] column) {
        float sum = 0;
        var count = 0;
        for (var v : column) {
            if (v == null) {
                continue;
            }

            var avgColumn = (String) v;
            String[] avgSumAndCount = avgColumn.split("#");
            sum += Float.parseFloat(avgSumAndCount[0]);
            count += Integer.parseInt(avgSumAndCount[1]);
        }

        return count > 0 ?
                sum / count :
                null;
    }
}
