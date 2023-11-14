package pl.dnwk.dmysql.integration.connection;

import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.connection.Connection;
import pl.dnwk.dmysql.integration.IntegrationTestCase;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.config.element.NodeConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTest extends IntegrationTestCase {

    public Config config() {
        var config = super.config();
        config.cluster.poolSize = 2;
        config.cluster.nodes = new HashMap<>() {{
            put("dmysql_1", NodeConfig.create()
                    .setUrl("mysql://localhost:3309/dmysql_1")
                    .setUser("root")
                    .setPassword("rootpass")
                    .setSchema("dmysql_1")
            );
            put("dmysql_2", NodeConfig.create()
                    .setUrl("mysql://localhost:3309/dmysql_2")
                    .setUser("root")
                    .setPassword("rootpass")
                    .setSchema("dmysql_2")
            );
        }};

        return config;
    }

    @Test
    public void createsConnection() {
        // When
        Connection connection = server.createConnection();

        // Then
        assertInstanceOf(Connection.class, connection);
    }

    @Test
    public void testWaitsForAvailableConnectionInPool() {
        // Given
        server.createConnection();
        Connection connection = server.createConnection();
        executeAfter(250, (i) -> connection.close());

        // Then
        AtomicReference<Boolean> waited = new AtomicReference<>(false);
        executeAfter(200, (i) -> waited.set(true));

        // When
        assertTimeoutPreemptively(Duration.ofMillis(2000), () -> server.createConnection());

        // Then
        assertTrue(waited.get());
    }

    @Test
    public void executesSimpleSelect() {
        // Given
        Connection connection = server.createConnection();

        // When
        String response = connection.executeSql("SELECT * FROM users LIMIT 1");

        // Then
        assertEquals("1 | test\n2 | test2\n", response);
    }
}
