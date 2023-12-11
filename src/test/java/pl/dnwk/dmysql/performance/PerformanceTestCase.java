package pl.dnwk.dmysql.performance;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.TestCluster;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.config.element.NodeConfig;
import pl.dnwk.dmysql.sharding.key.IntShardKey;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sharding.schema.Table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public abstract class PerformanceTestCase {

    protected Server server;

    @Test
    public void test() {
        var connection = server.createConnection();

        double appTime = 0L;
        double mySqlTime = 0L;
        for (var sql : beforeAll()) {
            connection.executeSql(sql);
        }

        // Heat up app
        for (var i = 0; i < loopCount(); i++) {
            for (var sql : beforeLoop()) {
                connection.executeSql(sql);
            }
            for (var sql : loop()) {
                connection.executeSql(sql);
            }
        }

        // Execute
        for (var i = 0; i < loopCount(); i++) {
            for (var sql : beforeLoop()) {
                connection.executeSql(sql);
            }

            long start = System.currentTimeMillis();
            for (var sql : loop()) {
                connection.executeSql(sql);
            }
            long finish = System.currentTimeMillis();
            appTime += finish - start;
        }

        for (var sql : afterAll()) {
            connection.executeSql(sql);
        }

        try {
            var sqlConnection = server.getCluster().get().getConnection(config().cluster.nodes.keySet().stream().findFirst().get());
            for (var sql : beforeAll()) {
                sqlConnection.prepareStatement(sql).execute();
            }
            for (var i = 0; i < loopCount(); i++) {
                for (var sql : beforeLoop()) {
                    sqlConnection.prepareStatement(sql).execute();
                }

                long start = System.currentTimeMillis();
                for (var sql : loop()) {
                    sqlConnection.prepareStatement(sql).execute();
                }
                long finish = System.currentTimeMillis();
                mySqlTime += finish - start;
            }

            for (var sql : afterAll()) {
                sqlConnection.prepareStatement(sql).execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        server.stop();

        appTime = appTime / 1000;
        mySqlTime = mySqlTime / 1000;

        System.out.println();
        System.out.println("Performance results");
        System.out.println("-".repeat(20));
        System.out.println("App time: " + appTime + "s");
        System.out.println("MySql time: " + mySqlTime + "s");
        System.out.println();

        assertTrue(mySqlTime * 5 > appTime, "App is 5x slower than MySql connection!\n" +
                "Performance results - app:" + appTime + " mysql time:" + mySqlTime);
    }

    public Integer loopCount() {
        return 1000;
    }

    public String[] beforeAll() {
        return new String[0];
    }

    public String[] beforeLoop() {
        return new String[0];
    }

    abstract public String[] loop();

    public String[] afterAll() {
        return new String[0];
    }

    public Config config() {
        var config = new Config();
        config.cluster = TestCluster.get();

        config.schema = new DistributedSchema() {{
            add(Table.OnAll("countries"));
            add(Table.Sharded("users", new IntShardKey("id")));
            add(Table.Sharded("cars", new IntShardKey("owner_id")));
        }};

        return config;
    }

    @BeforeEach
    public void setUp() {
        Log.setLevel(Log.WARNING);
        prepareSchema(new String[]{
                "CREATE TABLE countries (code VARCHAR(2) NOT NULL, name VARCHAR(256) NOT NULL)",
                "CREATE TABLE users (id int NOT NULL, name VARCHAR(256) NULL)",
                "CREATE TABLE cars (registration VARCHAR(32) NOT NULL, owner_id int NOT NULL, model VARCHAR(256) NOT NULL, production_country VARCHAR(2) NOT NULL, mileage DECIMAL(10,1) NULL)",
        });

        server = new Server(config());
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    private void prepareSchema(String[] statements) {
        try {
            for (String nodeName : config().cluster.nodes.keySet()) {
                NodeConfig node = config().cluster.nodes.get(nodeName);
                Connection nodeConnection = DriverManager.getConnection(node.url.replace("/" + node.schema, ""), node.user, node.password);
                nodeConnection.createStatement().execute("DROP DATABASE IF EXISTS " + node.schema);
                nodeConnection.createStatement().execute("CREATE DATABASE " + node.schema);
                nodeConnection.createStatement().execute("USE " + node.schema);

                for (String statement : statements) {
                    nodeConnection.createStatement().execute(statement);
                }
                nodeConnection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
