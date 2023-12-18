package pl.dnwk.dmysql.performance;

import pl.dnwk.dmysql.common.ArrayBuilder;

public class IdSelectPerformanceTest extends PerformanceTestCase {

    @Override
    public String[] beforeAll() {
        return ArrayBuilder.create(new String[8])
                .add("DELETE FROM users")
                .add("INSERT INTO users(id, name) VALUES (1, 'aaaa')")
                .add("INSERT INTO users(id, name) VALUES (2, 'bbbb')")
                .add("INSERT INTO users(id, name) VALUES (3, 'cccc')")
                .add("INSERT INTO users(id, name) VALUES (4, 'dddd')")
                .add("INSERT INTO users(id, name) VALUES (5, 'eeee')")
                .add("INSERT INTO users(id, name) VALUES (6, 'ffff')")
                .add("INSERT INTO users(id, name) VALUES (7, 'gggg')")
                .add("INSERT INTO users(id, name) VALUES (8, 'hhhh')")
                .add("INSERT INTO users(id, name) VALUES (9, 'iiii')")
                .add("INSERT INTO users(id, name) VALUES (10, 'jjjj')")
                .add("INSERT INTO users(id, name) VALUES (11, 'kkkk')")
                .add("INSERT INTO users(id, name) VALUES (12, 'gggg')")
                .toArray();
    }

    @Override
    public String[] loop() {
        var queries = ArrayBuilder.create(new String[8]);

        for (var i = 1; i <= 12; i++) {
            queries.add("SELECT id, name FROM users WHERE id='" + i + "'");
        }

        return queries.toArray();
    }


    @Override
    public String[] afterAll() {
        return ArrayBuilder.create(new String[8])
                .add("DELETE FROM users")
                .toArray();
    }
}
