package pl.dnwk.dmysql.sharding.schema;

import pl.dnwk.dmysql.sharding.key.ShardKey;

public class Table {
    public String tableName;
    public boolean sharded;
    public ShardKey shardKey;

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
}
