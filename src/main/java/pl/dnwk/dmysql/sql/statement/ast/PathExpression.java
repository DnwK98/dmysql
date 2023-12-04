package pl.dnwk.dmysql.sql.statement.ast;

public class PathExpression implements ValueExpression {
    public String tableIdentification;
    public String field;

    public String toString() {
        String s = "";
        if(tableIdentification != null) {
            s += tableIdentification + ".";
        }

        s+= field;

        return s;
    }
}
