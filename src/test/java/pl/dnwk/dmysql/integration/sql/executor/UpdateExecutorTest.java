package pl.dnwk.dmysql.integration.sql.executor;

import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.integration.IntegrationTestCase;
import pl.dnwk.dmysql.sql.executor.update.UpdateExecutor;
import pl.dnwk.dmysql.sql.statement.Parser;
import pl.dnwk.dmysql.sql.statement.ast.UpdateStatement;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class UpdateExecutorTest extends IntegrationTestCase {

    @Test
    public void updateValueOnAllShards() {
        // Before
        assertSqlResponse("SELECT code, name FROM countries WHERE code = 'PL'", new Object[][]{
                {"PL", "Poland"},
        });

        // When
        createExecutor().execute(createStatement("" +
                "UPDATE countries SET name = 'Poland2' WHERE code = 'PL'"
        ));

        // Then
        assertSqlResponse("SELECT code, name FROM countries WHERE code = 'PL'", new Object[][]{
                {"PL", "Poland2"},
        });
    }

    @Test
    public void updateValueOnOneShardButExecuteOnAllWhenShardKeyNotProvided() {
        // Before
        assertSqlResponse("SELECT registration, model FROM cars WHERE registration = 'WB 721L'", new Object[][]{
                {"WB 721L", "Mercedes"},
        });

        // When
        createExecutor().execute(createStatement("" +
                "UPDATE cars SET model = 'BMW' WHERE registration = 'WB 721L'"
        ));

        // Then
        assertSqlResponse("SELECT registration, model FROM cars WHERE registration = 'WB 721L'", new Object[][]{
                {"WB 721L", "BMW"},
        });
    }

    @Test
    public void executeDeleteOnSingleShard() {
        // Before
        assertSqlResponse("SELECT registration, model FROM cars WHERE registration = 'WB 721L'", new Object[][]{
                {"WB 721L", "Mercedes"},
        });

        // When
        createExecutor().execute(createStatement("" +
                "UPDATE cars SET model = 'BMW' WHERE registration = 'WB 721L' AND owner_id = 2"
        ));

        // Then
        assertSqlResponse("SELECT registration, model FROM cars WHERE registration = 'WB 721L'", new Object[][]{
                {"WB 721L", "BMW"},
        });
    }

    private static UpdateStatement createStatement(String sql) {
        return (UpdateStatement) Parser.parseSql(sql);
    }

    private UpdateExecutor createExecutor() {
        return new UpdateExecutor(server.getDistributedSchema(), server.getCluster().get());
    }

    private void assertSqlResponse(String sql, Object[][] expectedResponse) {
        var connection = server.createConnection();
        var result = connection.executeSql(sql);
        connection.close();

        assertArrayEquals(expectedResponse, result.values);
    }
}
