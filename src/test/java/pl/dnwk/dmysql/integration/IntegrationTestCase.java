package pl.dnwk.dmysql.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.config.element.NodeConfig;
import pl.dnwk.dmysql.sharding.key.IntShardKey;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sharding.schema.Table;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class IntegrationTestCase {

    protected Server server;

    public Config config() {
        var config = new Config();
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
        fixture();
        server = new Server(config());
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    protected void fixture() {
        fixtureOnAllNodes(new String[]{
                "CREATE TABLE countries (code VARCHAR(2) NOT NULL, name VARCHAR(256) NOT NULL)",
                "CREATE TABLE users (id int NOT NULL, ldap VARCHAR(256) NULL)",
                "CREATE TABLE cars (registration VARCHAR(32) NOT NULL, owner_id int NOT NULL, model VARCHAR(256) NOT NULL, production_country VARCHAR(2) NOT NULL, mileage DECIMAL(10,1) NULL)",
                "INSERT INTO countries (code, name) VALUES ('PL', 'Poland'), ('DE', 'Germany'), ('FR', 'France'), ('JP', 'Japan'), ('US', 'United States')"
        });

        fixtureOnNode("dmysql_1", new String[]{
                "INSERT INTO users (id, ldap) VALUES (1, 'test1')",
                "INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('GD 1234', 1, 'Honda', 'JP', 55000)",
                "INSERT INTO users (id, ldap) VALUES (4, 'test4')",
                "INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('WI 53D2', 4, 'Ford', 'US', 83000)",
        });

        fixtureOnNode("dmysql_2", new String[]{
                "INSERT INTO users (id, ldap) VALUES (2, 'test2')",
                "INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('DW 12H1', 2, 'Audi', 'DE', 127000)",
                "INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('WB 721L', 2, 'Mercedes', 'DE', 348000)",
        });

        fixtureOnNode("dmysql_3", new String[]{
                "INSERT INTO users (id, ldap) VALUES (3, 'test3')",
                "INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('BI 22E1', 3, 'BMW', 'DE', 73000)",
                "INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('BI K731', 3, 'BMW', 'DE', 235000)",
                "INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('WB 7622', 3, 'Porsche', 'DE', null)",
                "INSERT INTO cars (registration, owner_id, model, production_country, mileage) VALUES ('BI 9K36', 3, 'Honda', 'JP', 75000)",
        });
    }

    private void fixtureOnAllNodes(String[] statements) {
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

    protected void fixtureOnNode(String nodeName, String[] statements) {
        try {
            NodeConfig node = config().cluster.nodes.get(nodeName);
            Connection nodeConnection = DriverManager.getConnection(node.url.replace("/" + node.schema, ""), node.user, node.password);
            nodeConnection.createStatement().execute("USE " + node.schema);

            for (String statement : statements) {
                nodeConnection.createStatement().execute(statement);
            }
            nodeConnection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
