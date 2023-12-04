package pl.dnwk.dmysql.sql.executor.select.aggregation;

import pl.dnwk.dmysql.sql.statement.ast.Function;
import pl.dnwk.dmysql.sql.statement.ast.ValueExpression;

import java.util.HashMap;
import java.util.Map;

public interface Aggregation {
    Map<String, Aggregation> map = new HashMap<>(){{
        put("COUNT", new Count());
        put("SUM", new Sum());
        put("AVG", new Avg());
    }};

    ValueExpression getExpression(Function f);

    Object aggregate(Object[] column);
}
