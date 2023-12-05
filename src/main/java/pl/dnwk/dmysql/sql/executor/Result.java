package pl.dnwk.dmysql.sql.executor;

public class Result {

    public final String[] columns;
    public final Object[][] values;
    public final String text;

    public static Result table(String[] columns, Object[][] values) {
        return new Result(columns, values, null);
    }

    public static Result text(String text) {
        return new Result(null, null, text);
    }

    private Result(String[] columns, Object[][] values, String text) {
        this.columns = columns;
        this.values = values;
        this.text = text;
    }
}
