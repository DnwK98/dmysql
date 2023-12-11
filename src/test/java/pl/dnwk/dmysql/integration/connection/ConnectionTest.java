package pl.dnwk.dmysql.integration.connection;

import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.common.Async;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.connection.Connection;
import pl.dnwk.dmysql.integration.IntegrationTestCase;

import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTest extends IntegrationTestCase {

    @Override
    public Config config() {
        var config = super.config();
        config.cluster.poolSize = 2;

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
        Async.executeAfter(250, (i) -> connection.close());

        // Then
        AtomicReference<Boolean> waited = new AtomicReference<>(false);
        Async.executeAfter(200, (i) -> waited.set(true));

        // When
        assertTimeoutPreemptively(Duration.ofMillis(2000), () -> server.createConnection());

        // Then
        assertTrue(waited.get());
    }
}
