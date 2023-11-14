package pl.dnwk.dmysql.sql.statement.ast;

public class WhereClause {
    public Conditional expression;

    public WhereClause(Conditional expression) {
        this.expression = expression;
    }
}
