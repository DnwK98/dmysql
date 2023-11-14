package pl.dnwk.dmysql.sql.statement.ast;

import java.util.ArrayList;
import java.util.List;

public class InListExpression implements Conditional {
    public boolean not = false;
    public List<Literal> literals = new ArrayList<>();
}
