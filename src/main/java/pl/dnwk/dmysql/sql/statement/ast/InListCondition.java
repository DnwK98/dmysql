package pl.dnwk.dmysql.sql.statement.ast;

import java.util.ArrayList;
import java.util.List;

public class InListCondition implements Conditional {
    public PathExpression path;
    public boolean not = false;
    public List<Literal> literals = new ArrayList<>();
}
