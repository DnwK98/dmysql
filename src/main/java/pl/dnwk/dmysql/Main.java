package pl.dnwk.dmysql;

import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.config.element.NodeConfig;
import pl.dnwk.dmysql.fixtures.FixturesLoader;
import pl.dnwk.dmysql.sharding.key.IntShardKey;
import pl.dnwk.dmysql.sharding.schema.Column;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sharding.schema.Table;


public class Main {
    public static void main(String[] args) {
        Log.setLevel(Log.INFO);

        Server server = new Server(config());
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
        server.createSocket();

        var c = server.createConnection();
        FixturesLoader.load(c);
        c.close();
    }

    public static Config config() {
        var config = new Config();
        config.port = 9090;

        Table.Sharded("users", new IntShardKey("id"))
                .primaryKey(new String[]{"id"})
                .addColumn(new Column("id", "int"))
                .addColumn(new Column("name", "VARCHAR(256)", true));

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

        config.cluster.poolSize = 4;
        config.cluster.commitSemaphore = false;

        config.cluster.nodes.put("dmysql_1", NodeConfig.create()
                .setUrl("mysql://mysql-node-1:3306/dmysql_db")
                .setUser("dmysql")
                .setPassword("change-me-nf342bfc2")
                .setSchema("dmysql_db")
        );
        config.cluster.nodes.put("dmysql_2", NodeConfig.create()
                .setUrl("mysql://mysql-node-2:3306/dmysql_db")
                .setUser("dmysql")
                .setPassword("change-me-nf342bfc2")
                .setSchema("dmysql_db")
        );
        config.cluster.nodes.put("dmysql_3", NodeConfig.create()
                .setUrl("mysql://mysql-node-3:3306/dmysql_db")
                .setUser("dmysql")
                .setPassword("change-me-nf342bfc2")
                .setSchema("dmysql_db")
        );

        return config;
    }
}