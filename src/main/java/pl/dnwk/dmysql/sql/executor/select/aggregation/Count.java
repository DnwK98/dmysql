package pl.dnwk.dmysql.sql.executor.select.aggregation;

import pl.dnwk.dmysql.sql.statement.ast.Function;
import pl.dnwk.dmysql.sql.statement.ast.ValueExpression;

public class Count implements Aggregation {
    @Override
    public ValueExpression getExpression(Function f) {
        return f;
    }

    public Object aggregate(Object[] column) {
        if (column.length > 0) {
            var sum = 0;
            for (var v : column) {
                if (v == null) {
                    continue;
                }
                sum += (int) v;
            }
            return sum;
        }

        return 0;
    }
}
