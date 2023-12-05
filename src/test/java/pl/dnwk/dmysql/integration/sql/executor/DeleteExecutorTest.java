package pl.dnwk.dmysql.integration.sql.executor;

import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.integration.IntegrationTestCase;
import pl.dnwk.dmysql.sql.executor.delete.DeleteExecutor;
import pl.dnwk.dmysql.sql.statement.Parser;
import pl.dnwk.dmysql.sql.statement.ast.DeleteStatement;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class DeleteExecutorTest extends IntegrationTestCase {

    @Test
    public void executeDeleteForTableOnAllShards() {
        // Before
        assertSqlResponse("SELECT code FROM countries WHERE code = 'PL'", new Object[][]{
                {"PL"},
        });

        // When
        createExecutor().execute(createStatement("" +
                "DELETE FROM countries WHERE code = 'PL'"
        ));

        // Then
        assertSqlResponse("SELECT code FROM countries WHERE code = 'PL'", new Object[][]{});
    }

    @Test
    public void executeDeleteOnAllWhenShardKeyNotProvided() {
        // Before
        assertSqlResponse("SELECT registration FROM cars WHERE registration = 'WB 721L'", new Object[][]{
                {"WB 721L"},
        });

        // When
        createExecutor().execute(createStatement("" +
                "DELETE FROM cars WHERE registration = 'WB 721L'"
        ));

        // Then
        assertSqlResponse("SELECT registration FROM cars WHERE registration = 'WB 721L'", new Object[][]{});
    }

    @Test
    public void executeDeleteOnSingleShard() {
        // Before
        assertSqlResponse("SELECT registration FROM cars WHERE registration = 'WB 721L'", new Object[][]{
                {"WB 721L"},
        });

        // When
        createExecutor().execute(createStatement("" +
                "DELETE FROM cars WHERE registration = 'WB 721L' AND owner_id = 2"
        ));

        // Then
        assertSqlResponse("SELECT registration FROM cars WHERE registration = 'WB 721L'", new Object[][]{});
    }

    private static DeleteStatement createStatement(String sql) {
        return (DeleteStatement) Parser.parseSql(sql);
    }

    private DeleteExecutor createExecutor() {
        return new DeleteExecutor(server.getDistributedSchema(), server.getCluster().get());
    }

    private void assertSqlResponse(String sql, Object[][] expectedResponse) {
        var connection = server.createConnection();
        var result = connection.executeSql(sql);
        connection.close();

        assertArrayEquals(expectedResponse, result.values);
    }
}
