package pl.dnwk.dmysql.sql.statement.ast;

import java.util.ArrayList;
import java.util.List;

public class Function implements ValueExpression{
    public String functionName;
    public List<ValueExpression> arguments = new ArrayList<>();
}
