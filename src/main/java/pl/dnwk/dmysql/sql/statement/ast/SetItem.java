package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;

public class SetItem implements Serializable {
    public PathExpression column;
    public Literal value;
}
