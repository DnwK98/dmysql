package pl.dnwk.dmysql.integration.sql.executor;

import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.integration.IntegrationTestCase;
import pl.dnwk.dmysql.sql.executor.select.SelectExecutor;
import pl.dnwk.dmysql.sql.statement.Parser;
import pl.dnwk.dmysql.sql.statement.ast.SelectStatement;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SelectExecutorTest extends IntegrationTestCase {

    @Test
    public void executesSimpleSelectNotSharded() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("SELECT code, name FROM countries");

        // When
        Object[][] response = executor.execute(statement);

        // Then
        var expected = new Object[][] {
                {"DE", "Germany"},
                {"FR", "France"},
                {"JP", "Japan"},
                {"PL", "Poland"},
                {"US", "United States"},
        };

        assertArrayEquals(expected, response);
    }

    @Test
    public void executesCountNotSharded() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("SELECT count(code) FROM countries");

        // When
        Object[][] response = executor.execute(statement);

        // Then
        var expected = new Object[][] {{5}};
        assertArrayEquals(expected, response);
    }

    @Test
    public void executesSimpleSelectFromAllShards() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("SELECT id, ldap FROM users");

        // When
        Object[][] response = executor.execute(statement);

        // Then
        var expected = new Object[][] {
                {1, "test1"},
                {2, "test2"},
                {3, "test3"},
                {4, "test4"},
        };
        assertArrayEquals(expected, response);
    }

    @Test
    public void executesSelectAvgFromAllShards() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("" +
                "SELECT model, AVG(c.mileage) " +
                "FROM cars c " +
                "WHERE model IN('Honda', 'BMW', 'Porsche') " +
                "GROUP BY 1 " +
                "ORDER BY 2 DESC"
        );

        // When
        Object[][] response = executor.execute(statement);

        // Then
        var expected = new Object[][] {
                {"BMW", 154000.0f},
                {"Honda", 65000.0f},
                {"Porsche", null},
        };
        assertArrayEquals(expected, response);
    }

    @Test
    public void executesSelectSumFromAllShardsWithoutGrouping() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("" +
                "SELECT SUM(c.mileage) " +
                "FROM cars c " +
                "WHERE model IN('Honda', 'BMW', 'Porsche')"
        );

        // When
        Object[][] response = executor.execute(statement);

        // Then
        var expected = new Object[][] {{438000.0}};
        assertArrayEquals(expected, response);
    }

    @Test
    public void executeInnerJoinFromMultipleShards() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("" +
                "SELECT u.ldap, c.model, c.registration " +
                "FROM users u " +
                "JOIN cars c on u.id = c.owner_id " +
                "WHERE u.ldap = 'test2' OR u.ldap = 'test1'"
        );

        // When
        Object[][] response = executor.execute(statement);

        // Then
        var expected = new Object[][] {
                {"test1", "Honda", "GD 1234"},
                {"test2", "Audi", "DW 12H1"},
                {"test2", "Mercedes", "WB 721L"}
        };
        assertArrayEquals(expected, response);
    }

    @Test
    public void executeLeftJoinAndCountFromMultipleShards() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("" +
                "SELECT u.ldap, COUNT(c.registration) " +
                "FROM users u " +
                "LEFT JOIN cars c on u.id = c.owner_id " +
                "WHERE c.model = 'Mercedes'" +
                "GROUP BY 1"
        );

        // When
        Object[][] response = executor.execute(statement);

        // Then
        var expected = new Object[][] {
                {"test2", 1}
        };
        assertArrayEquals(expected, response);
    }

    @Test
    public void executeLeftJoinFromAllShardTableToSingleShardTable() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("" +
                "SELECT co.name, ca.registration " +
                "FROM countries co " +
                "LEFT JOIN cars ca on co.code = ca.production_country AND ca.model = 'Mercedes'"
        );

        // Then
        assertThrows(RuntimeException.class, () -> executor.execute(statement));
    }

    private static SelectStatement createStatement(String sql) {
        return (SelectStatement) Parser.parseSql(sql);
    }

    private SelectExecutor createExecutor() {
        return new SelectExecutor(server.getDistributedSchema(), server.getCluster().get());
    }
}
