package pl.dnwk.dmysql.sharding.schema;

public class Column {
    public final String name;
    public final String type;
    public final String defaultValue;
    public final boolean nullable;

    public Column(String name, String type, String defaultValue, boolean nullable) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.nullable = nullable;
    }

    public Column(String name, String type, boolean nullable) {
        this.name = name;
        this.type = type;
        this.defaultValue = null;
        this.nullable = nullable;
    }

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
        this.defaultValue = null;
        this.nullable = false;
    }
}
