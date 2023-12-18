package pl.dnwk.dmysql.integration.sql.executor;

import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.integration.IntegrationTestCase;
import pl.dnwk.dmysql.sql.executor.insert.InsertExecutor;
import pl.dnwk.dmysql.sql.statement.Parser;
import pl.dnwk.dmysql.sql.statement.ast.InsertStatement;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class InsertExecutorTest extends IntegrationTestCase {

    @Test
    public void executesSimpleInsert() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("" +
                "INSERT INTO users (id, ldap) VALUES " +
                "(5, 'test5')"
        );

        // When
        executor.execute(statement);

        // Then
        assertSqlResponse("SELECT id, ldap FROM users WHERE id = 5", new Object[][]{
                {5, "test5"}
        });
    }

    @Test
    public void executeInsertWithNullValue() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("" +
                "INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES " +
                "('WK 7622', 3, 'Porsche', 'DE', null)"
        );

        // When
        executor.execute(statement);

        // Then
        assertSqlResponse("SELECT registration, mileage FROM cars WHERE registration = 'WK 7622'", new Object[][]{
                {"WK 7622", null}
        });
    }

    @Test
    public void executeInsertToTwoShards() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("" +
                "INSERT INTO users (id, ldap) VALUES " +
                "(5, 'test5'), " +
                "(6, 'test6')"
        );

        // When
        executor.execute(statement);

        // Then
        assertSqlResponse("SELECT id, ldap FROM users WHERE id IN(5,6)", new Object[][]{
                {5, "test5"},
                {6, "test6"}
        });
    }

    @Test
    public void executeInsertToNotShardedTable() {
        // Given
        var executor = createExecutor();
        var statement = createStatement("" +
                "INSERT INTO countries (code, name) VALUES " +
                "('CY', 'Cypr')"
        );

        // When
        executor.execute(statement);

        // Then
        assertSqlResponse("SELECT code, name FROM countries WHERE code = 'CY'", new Object[][]{
                {"CY", "Cypr"},
        });
    }

    private static InsertStatement createStatement(String sql) {
        return (InsertStatement) Parser.parseSql(sql);
    }

    private InsertExecutor createExecutor() {
        return new InsertExecutor(server.getDistributedSchema(), server.getCluster().get());
    }

    private void assertSqlResponse(String sql, Object[][] expectedResponse) {
        var connection = server.createConnection();
        var result = connection.executeSql(sql);
        connection.close();

        assertArrayEquals(expectedResponse, result.values);
    }
}
