package pl.dnwk.dmysql.sharding.schema;

import pl.dnwk.dmysql.common.ArrayBuilder;
import pl.dnwk.dmysql.sharding.key.ShardKey;

import java.util.ArrayList;
import java.util.List;

public class Table {
    public String tableName;
    public boolean sharded;
    public ShardKey shardKey;
    public List<Column> columns = new ArrayList<>();
    public String[] primaryKey;

    public static Table OnAll(String tableName) {
        var t = new Table();
        t.tableName = tableName;
        t.sharded = false;
        t.shardKey = null;

        return t;
    }

    public static Table Sharded(String tableName, ShardKey shardKey) {
        var t = new Table();
        t.tableName = tableName;
        t.sharded = true;
        t.shardKey = shardKey;

        return t;
    }

    public Table primaryKey(String[] columns) {
        primaryKey = columns;

        return this;
    }

    public Table addColumn(Column c) {
        columns.add(c);

        return this;
    }

    public String getCreateStatement() {
        var s = new StringBuilder("CREATE TABLE ");
        s.append(tableName);
        s.append("(");

        var columnsDefs = ArrayBuilder.create(new String[8]);
        for(Column column: columns) {
            var c = new StringBuilder(column.name);
            c.append(" ");
            c.append(column.type);
            if(column.defaultValue != null) {
                c.append(" ");
                c.append(column.defaultValue);
            }
            c.append(" ");
            if(column.nullable) {
                c.append("NULL");
            } else {
                c.append("NOT NULL");
            }

            columnsDefs.add(c.toString());
        }

        if(primaryKey != null) {
            columnsDefs.add("PRIMARY KEY(" + String.join(", ", primaryKey) + ")");
        }
        s.append(String.join(", ", columnsDefs.toArray()));
        s.append(")");

        return s.toString();
    }
}
