package pl.dnwk.dmysql.performance;

import pl.dnwk.dmysql.common.ArrayBuilder;

public class AggregationPerformanceTest extends PerformanceTestCase {

    @Override
    public Integer loopCount() {
        return 1000;
    }

    @Override
    public String[] beforeAll() {
        var queries = ArrayBuilder.create(new String[8]);

        queries.add("DELETE FROM users");
        queries.add("DELETE FROM countries");
        queries.add("DELETE FROM cars");
        queries.add("INSERT INTO countries (code, name) VALUES ('PL', 'Poland')");
        for (var i = 1; i <= 3; i++) {
            queries.add("INSERT INTO users (id, name) VALUES (" + i + ", 'name-" + i + "')");
            for (var j = 0; j < 300; j++) {
                queries.add("INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES " +
                        "('WB 12" + i + j + "', " + i + j + ", 'Mercedes', 'PL', 100" + ((j + i) * 100) + ")");
            }
        }

        return queries.toArray();
    }

    @Override
    public String[] loop() {
        var queries = ArrayBuilder.create(new String[8]);

        queries.add("SELECT owner_id, AVG(mileage) as avg FROM cars GROUP BY owner_id");

        return queries.toArray();
    }


    @Override
    public String[] afterAll() {
        return ArrayBuilder.create(new String[8])
                .add("DELETE FROM users")
                .add("DELETE FROM countries")
                .add("DELETE FROM cars")
                .toArray();
    }
}
