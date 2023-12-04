package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;

public class Join implements Serializable {
    public static final String TYPE_INNER = "INNER";
    public static final String TYPE_LEFT = "LEFT";
    public static final String TYPE_RIGHT = "RIGHT";
    public static final String TYPE_OUTER = "OUTER";

    public String type = TYPE_INNER;
    public String table;
    public String alias;
    public Conditional condition;
}
