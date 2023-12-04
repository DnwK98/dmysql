package pl.dnwk.dmysql.sql.statement.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Function implements ValueExpression{
    public String functionName;
    public List<ValueExpression> arguments = new ArrayList<>();

    public static Function create(String name, ValueExpression[] arguments) {
        var f = new Function();
        f.functionName = name;
        f.arguments = new ArrayList<>(Arrays.asList(arguments));

        return f;
    }
}
