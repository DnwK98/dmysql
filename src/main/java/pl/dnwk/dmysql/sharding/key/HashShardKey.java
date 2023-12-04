package pl.dnwk.dmysql.sharding.key;

public class HashShardKey implements ShardKey {

    private final String column;

    public HashShardKey(String column) {

        this.column = column;
    }

    public int pick(String id, int count) {

        try {
//            TODO implement hash shard key
//            byte[] bytesOfMessage = new byte[0];
//            bytesOfMessage = id.getBytes(StandardCharsets.UTF_8);
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            byte[] md5 = md.digest(bytesOfMessage);
//
//            md5.length
            return 0;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String column() {
        return column;
    }
}
