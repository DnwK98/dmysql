package pl.dnwk.dmysql;

import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.config.element.NodeConfig;
import pl.dnwk.dmysql.sharding.schema.Column;
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

        config.schema.add(Table.OnAll("countries")
                .primaryKey(new String[]{"code"})
                .addColumn(new Column("code", "VARCHAR(2)"))
                .addColumn(new Column("name", "VARCHAR(256)"))
        );
        config.schema.add(Table.Sharded("users", new IntShardKey("id"))
                .primaryKey(new String[]{"id"})
                .addColumn(new Column("id", "int"))
                .addColumn(new Column("name", "VARCHAR(256)", true))
        );
        config.schema.add(Table.Sharded("cars", new IntShardKey("owner_id"))
                .primaryKey(new String[]{"registration", "owner_id"})
                .addColumn(new Column("registration", "VARCHAR(32)"))
                .addColumn(new Column("owner_id", "int"))
                .addColumn(new Column("model", "VARCHAR(256)"))
                .addColumn(new Column("production_country", "VARCHAR(2)"))
                .addColumn(new Column("mileage", "DECIMAL(10,1)", true))
        );

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