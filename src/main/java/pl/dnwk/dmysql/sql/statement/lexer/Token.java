package pl.dnwk.dmysql.sql.statement.lexer;

public class Token {

    public final String value;
    public final int type;

    public Token(String value, int type) {
        this.value = value;
        this.type = type;
    }

    public boolean is(int type)
    {
        return this.type == type;
    }
}
