package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;

public class OrderByItem implements Serializable {

    public final static String ASC = "ASC";
    public final static String DESC = "DESC";

    public ValueExpression orderBy;
    public String direction = ASC;
}
