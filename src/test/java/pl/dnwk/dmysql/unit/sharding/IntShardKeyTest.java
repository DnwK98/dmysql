package pl.dnwk.dmysql.unit.sharding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import pl.dnwk.dmysql.sharding.key.IntShardKey;
import pl.dnwk.dmysql.unit.UnitTestCase;

public class IntShardKeyTest extends UnitTestCase {

    @Test
    public void testShardPickFrom3() {
        var shardKey = new IntShardKey("id");

        var picked = new Integer[]{
                shardKey.pick("1", 3), // 0
                shardKey.pick("2", 3), // 1
                shardKey.pick("3", 3), // 2
                shardKey.pick("4", 3), // 0
                shardKey.pick("5", 3), // 1
                shardKey.pick("6", 3), // 2
        };

        var expected = new Integer[]{0, 1, 2, 0, 1, 2};
        assertArrayEquals(expected, picked);
    }

    @Test
    public void testShardPickFrom1() {
        var shardKey = new IntShardKey("id");

        var picked = new Integer[]{
                shardKey.pick("1", 1),
                shardKey.pick("2", 1),
                shardKey.pick("3", 1),
                shardKey.pick("4", 1),
        };

        var expected = new Integer[]{0, 0, 0, 0};
        assertArrayEquals(expected, picked);
    }
}
