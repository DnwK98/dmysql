package pl.dnwk.dmysql.sharding.key;

public interface ShardKey {
    int pick(String id, int count);
    String column();
}
