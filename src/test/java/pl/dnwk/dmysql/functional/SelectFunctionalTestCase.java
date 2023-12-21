package pl.dnwk.dmysql.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.fixtures.FixturesLoader;
import static org.junit.jupiter.api.Assertions.*;

public class SelectFunctionalTestCase extends FunctionalTestCase {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        var c = server.createConnection();
        FixturesLoader.load(c);
        c.close();
    }

    @Test
    public void testSelectUsers() {
        var result = server.createConnection().executeSql("SELECT id, name FROM users");

        assertEquals(10, result.values.length);
        assertEquals("Patrycja", result.values[3][1]);
    }

    @Test
    public void testSelectUserById() {
        var result = server.createConnection().executeSql("SELECT id, name FROM users WHERE id = 7");

        assertEquals(1, result.values.length);
        assertEquals("Renata", result.values[0][1]);
    }

    @Test
    public void testSelectUsersWithCars() {
        var result = server.createConnection().executeSql("" +
                "SELECT u.id, u.name, c.registration, c.model " +
                "FROM users u " +
                "LEFT JOIN cars c ON u.id = c.owner_id " +
                "WHERE u.id IN(1, 3) " +
                "");

        assertEquals(5, result.values.length);
    }

    @Test
    public void testSelectUserWithCarAndCountry() {
        var result = server.createConnection().executeSql("" +
                "SELECT u.id, c.registration, co.name " +
                "FROM users u " +
                "LEFT JOIN cars c ON u.id = c.owner_id " +
                "LEFT JOIN countries co ON c.production_country = co.code" +
                "");

        assertEquals(14, result.values.length);
        assertEquals(3, result.values[3][0]);
        assertEquals("WI 53D2", result.values[7][1]);
        assertEquals("United States", result.values[7][2]);
        assertNull(result.values[10][1]);
    }

    @Test
    public void testSelectWorkshopsWhichInCarWasRepaired() {
        var result = server.createConnection().executeSql("" +
                "SELECT w.name " +
                "    FROM cars c " +
                "JOIN cars_workshops cw on c.registration = cw.registration and c.owner_id = cw.owner_id " +
                "JOIN workshops w on cw.workshop_id = w.id " +
                "WHERE c.registration = 'WI 53D2'" +
                "");

        assertEquals(2, result.values.length);
        assertEquals("Auto Mistrz", result.values[0][0]);
        assertEquals("Ekspert Auto", result.values[1][0]);
    }

    @Test
    public void testSelectInvoicesSum() {
        var result = server.createConnection().executeSql("" +
                "SELECT SUM(i.amount) as sum " +
                "FROM invoices i " +
                "");

        assertEquals(16100.00, result.values[0][0]);
    }

    @Test
    public void testSelectInvoicesGroupByWorkshopName() {
        var result = server.createConnection().executeSql("" +
                "SELECT w.name, SUM(i.amount) as sum " +
                "FROM invoices i " +
                "JOIN workshops w on w.id = i.workshop_id " +
                "GROUP BY w.name " +
                "ORDER BY sum DESC" +
                "");

        assertEquals(3, result.values.length);
        assertEquals(9900.00, result.values[0][1]);
        assertEquals(5000.00, result.values[1][1]);
        assertEquals(1200.00, result.values[2][1]);
    }

}
