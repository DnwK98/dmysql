package pl.dnwk.dmysql.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import pl.dnwk.dmysql.Server;
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

public class FunctionalTestCase {

    protected Server server;

    public Config config() {
        var config = new Config();
        config.cluster.poolSize = 4;
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
            put("dmysql_3", NodeConfig.create()
                    .setUrl("mysql://localhost:3309/dmysql_3")
                    .setUser("root")
                    .setPassword("rootpass")
                    .setSchema("dmysql_3")
            );
        }};

        config.schema = new DistributedSchema() {{
            add(Table.OnAll("countries"));
            add(Table.Sharded("users", new IntShardKey("id")));
            add(Table.Sharded("cars", new IntShardKey("owner_id")));
        }};

        return config;
    }

    @BeforeEach
    public void setUp() {
        Log.setLevel(Log.DEBUG);
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
