package pl.dnwk.dmysql.performance;

import pl.dnwk.dmysql.common.ArrayBuilder;

public class InsertPerformanceTest extends PerformanceTestCase {

    @Override
    public Integer loopCount() {
        return 500;
    }

    @Override
    public String[] beforeLoop() {
        return ArrayBuilder.create(new String[8])
                .add("DELETE FROM users")
                .toArray();
    }

    @Override
    public String[] loop() {
        return ArrayBuilder.create(new String[8])
                .add("INSERT INTO users(id, name) VALUES (1, 'aaaa'), (2, 'bbbb'), (5, 'cccc'), (4, 'dddd')")
                .add("INSERT INTO users(id, name) VALUES (3, '5aaaa'), (6, '6bbbb'), (7, '7cccc'), (9, '8dddd')")
                .add("INSERT INTO users(id, name) VALUES (8, '9aaaa'), (14, '10bbbb'), (11, '11cccc'), (12, '12dddd')")
                .add("INSERT INTO users(id, name) VALUES (13, '13aaaa'), (10, '14bbbb'), (15, '15cccc'), (16, '16dddd')")
                .toArray();
    }


    @Override
    public String[] afterAll() {
        return ArrayBuilder.create(new String[8])
                .add("DELETE FROM users")
                .toArray();
    }
}
