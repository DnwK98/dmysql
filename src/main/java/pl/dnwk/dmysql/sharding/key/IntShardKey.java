package pl.dnwk.dmysql.sharding.key;

public class IntShardKey implements ShardKey {

    private final String column;

    public IntShardKey(String column){

        this.column = column;
    }

    public int pick(String id, int count) {
        var i = Integer.parseInt(id) - 1;

        return i % count;
    }

    public String column() {
        return column;
    }
}
