package pl.dnwk.dmysql.sql.executor.select.aggregation;

import pl.dnwk.dmysql.sql.statement.ast.Function;
import pl.dnwk.dmysql.sql.statement.ast.ValueExpression;

public class AnyValue implements Aggregation {
    @Override
    public ValueExpression getExpression(Function f) {
        return f;
    }

    @Override
    public Object aggregate(Object[] column) {
        if(column.length > 0) {
            return column[0];
        }

        return null;
    }
}
