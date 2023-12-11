package pl.dnwk.dmysql;

import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.config.element.NodeConfig;
import pl.dnwk.dmysql.sharding.schema.Table;


public class Main {
    public static void main(String[] args) {
        Log.setLevel(Log.INFO);

        Server server = new Server(config());
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
        server.createSocket();
    }

    public static Config config() {
        var config = new Config();
        config.port = 9090;

        config.schema
                .add(Table.OnAll("countries"));

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