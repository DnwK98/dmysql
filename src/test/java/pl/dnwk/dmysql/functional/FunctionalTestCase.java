package pl.dnwk.dmysql.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.config.Config;

public class FunctionalTestCase {

    protected Server server;

    public Config config() {
        return new Config();
    }

    @BeforeEach
    public void setUp() {
        server = new Server(config());
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }
}
