package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;

public class WhereClause implements Serializable {
    public Conditional expression;

    public WhereClause(Conditional expression) {
        this.expression = expression;
    }
}
