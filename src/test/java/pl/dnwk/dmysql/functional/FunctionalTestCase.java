package pl.dnwk.dmysql.functional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.TestCluster;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.config.element.NodeConfig;
import pl.dnwk.dmysql.sharding.key.IntShardKey;
import pl.dnwk.dmysql.sharding.schema.Column;
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
        config.cluster = TestCluster.get();
        config.cluster.commitSemaphore = false;

        config.schema = new DistributedSchema() {{
            add(Table.OnAll("countries")
                    .primaryKey(new String[]{"code"})
                    .addColumn(new Column("code", "VARCHAR(2)"))
                    .addColumn(new Column("name", "VARCHAR(256)"))
            );
            add(Table.OnAll("workshops")
                    .primaryKey(new String[]{"id"})
                    .addColumn(new Column("id", "int"))
                    .addColumn(new Column("name", "VARCHAR(256)"))
            );
            add(Table.Sharded("users", new IntShardKey("id"))
                    .primaryKey(new String[]{"id"})
                    .addColumn(new Column("id", "int"))
                    .addColumn(new Column("name", "VARCHAR(256)", true))
            );
            add(Table.Sharded("cars", new IntShardKey("owner_id"))
                    .primaryKey(new String[]{"registration", "owner_id"})
                    .foreignKey("users", new String[]{"owner_id"}, new String[]{"id"})
                    .foreignKey("countries", new String[]{"production_country"}, new String[]{"code"})
                    .addColumn(new Column("registration", "VARCHAR(32)"))
                    .addColumn(new Column("owner_id", "int"))
                    .addColumn(new Column("model", "VARCHAR(256)"))
                    .addColumn(new Column("production_country", "VARCHAR(2)"))
                    .addColumn(new Column("mileage", "DECIMAL(10,1)", true))
            );
            add(Table.Sharded("cars_workshops", new IntShardKey("owner_id"))
                    .primaryKey(new String[]{"registration", "owner_id", "workshop_id"})
                    .foreignKey("cars", new String[]{"registration", "owner_id"}, new String[]{"registration", "owner_id"})
                    .foreignKey("workshops", new String[]{"workshop_id"}, new String[]{"id"})
                    .addColumn(new Column("registration", "VARCHAR(32)"))
                    .addColumn(new Column("owner_id", "int"))
                    .addColumn(new Column("workshop_id", "int"))
            );
            add(Table.Sharded("invoices", new IntShardKey("user_id"))
                    .primaryKey(new String[]{"id", "user_id"})
                    .foreignKey("users", new String[]{"user_id"}, new String[]{"id"})
                    .foreignKey("workshops", new String[]{"workshop_id"}, new String[]{"id"})
                    .addColumn(new Column("id", "int"))
                    .addColumn(new Column("user_id", "int"))
                    .addColumn(new Column("workshop_id", "int"))
                    .addColumn(new Column("amount", "DECIMAL(10,2)"))
            );
            add(Table.Sharded("invoice_items", new IntShardKey("user_id"))
                    .primaryKey(new String[]{"id", "invoice_id", "user_id"})
                    .foreignKey("invoices", new String[]{"invoice_id", "user_id"}, new String[]{"id", "user_id"})
                    .addColumn(new Column("id", "int"))
                    .addColumn(new Column("invoice_id", "int"))
                    .addColumn(new Column("user_id", "int"))
                    .addColumn(new Column("name", "VARCHAR(256)"))
                    .addColumn(new Column("amount", "DECIMAL(10,2)"))
            );
        }};

        return config;
    }

    @BeforeEach
    public void setUp() {
        Log.setLevel(Log.DEBUG);
        prepareSchema(new String[0]);

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
