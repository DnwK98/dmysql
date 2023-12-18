package pl.dnwk.dmysql.sql.statement.ast;

public class Literal implements ValueExpression {

    public static final String STRING = "STRING";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String NUMERIC = "NUMERIC";
    public static final String NULL = "NULL";

    public String type;
    public String value;

    public static Literal String(String value)
    {
        var l = new Literal();
        l.type = STRING;
        l.value = value;

        return l;
    }

    public static Literal Boolean(String value)
    {
        var l = new Literal();
        l.type = BOOLEAN;
        l.value = value;

        return l;
    }

    public static Literal Numeric(String value)
    {
        var l = new Literal();
        l.type = NUMERIC;
        l.value = value;

        return l;

    }

    public static ValueExpression Null() {
        var l = new Literal();
        l.type = NULL;

        return l;
    }

    public String toString() {
        return value;
    }
}
