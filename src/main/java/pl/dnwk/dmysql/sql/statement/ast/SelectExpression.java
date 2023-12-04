package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;

public class SelectExpression implements Serializable {

    public ValueExpression expression;
    public String alias = null;
}
