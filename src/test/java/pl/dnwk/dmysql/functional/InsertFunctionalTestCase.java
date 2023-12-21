package pl.dnwk.dmysql.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.fixtures.FixturesLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InsertFunctionalTestCase extends FunctionalTestCase {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        var c = server.createConnection();
        FixturesLoader.load(c);
        c.close();
    }

    @Test
    public void testInsertsNewUsers() {
        // When
        server.createConnection().executeSql("" +
                "INSERT INTO users (id, name)\n" +
                "VALUES (100, 'Adam'),\n" +
                "       (101, 'Karol'),\n" +
                "       (102, 'Natalia'),\n" +
                "       (103, 'Ryszard'),\n" +
                "       (104, 'Agata');");

        // Then
        var result1 = server.createConnection().executeSql("SELECT id FROM users WHERE id=101");
        assertEquals(1, result1.values.length);
        var result2 = server.createConnection().executeSql("SELECT id FROM users WHERE id=102");
        assertEquals(1, result2.values.length);
    }

}
