package pl.dnwk.dmysql.performance;

import pl.dnwk.dmysql.common.ArrayBuilder;

public class BigResultSelectPerformanceTest extends PerformanceTestCase {

    @Override
    public Integer loopCount() {
        return 1500;
    }

    @Override
    public String[] beforeAll() {
        var queries = ArrayBuilder.create(new String[8]);

        queries.add("INSERT INTO countries (code, name) VALUES ('PL', 'Poland')");
        for (var i = 1; i <= 3; i++) {
            queries.add("INSERT INTO users (id, name) VALUES (" + i + ", 'name-" + i + "')");
            for (var j = 0; j < 400; j++) {
                queries.add("INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES " +
                        "('WB 12" + i + "', " + i + ", 'Mercedes', 'PL', 100" + ((j + i) * 100) + ")");
            }
        }

        return queries.toArray();
    }

    @Override
    public String[] loop() {
        var queries = ArrayBuilder.create(new String[8]);

        queries.add("SELECT u.name, c.registration, c.model, c.mileage FROM users u JOIN cars c ON u.id = c.owner_id");

        return queries.toArray();
    }


    @Override
    public String[] afterAll() {
        return ArrayBuilder.create(new String[8])
                .add("DELETE FROM users")
                .toArray();
    }
}
