package pl.dnwk.dmysql.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.config.element.NodeConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;

public class IntegrationTestCase {

    protected Server server;

    public Config config() {
        return new Config();
    }

    @BeforeEach
    public void setUp() {
        try {
            NodeConfig node = config().cluster.nodes.get("dmysql_1");
            Connection nodeConnection = DriverManager.getConnection(node.url.replace("/dmysql_1", ""), node.user, node.password);
            nodeConnection.createStatement().execute("DROP DATABASE IF EXISTS " + node.schema);
            nodeConnection.createStatement().execute("CREATE DATABASE " + node.schema);
            nodeConnection.createStatement().execute("USE " + node.schema);
            nodeConnection.createStatement().execute("CREATE TABLE users (id int NULL, ldap VARCHAR(256) NULL)");
            nodeConnection.createStatement().execute("INSERT INTO users (id, ldap) VALUES (1, 'test')");

            node = config().cluster.nodes.get("dmysql_2");
            nodeConnection = DriverManager.getConnection(node.url.replace("/dmysql_2", ""), node.user, node.password);
            nodeConnection.createStatement().execute("DROP DATABASE IF EXISTS " + node.schema);
            nodeConnection.createStatement().execute("CREATE DATABASE " + node.schema);
            nodeConnection.createStatement().execute("USE " + node.schema);
            nodeConnection.createStatement().execute("CREATE TABLE users (id int NULL, ldap VARCHAR(256) NULL)");
            nodeConnection.createStatement().execute("INSERT INTO users (id, ldap) VALUES (2, 'test2')");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        server = new Server(config());
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    protected static void executeAfter(int milliseconds, Consumer<Integer> executionCallback) {
        new Thread(() -> {
            try {
                Thread.sleep(milliseconds);
                executionCallback.accept(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
