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
                .setUrl("mysql://localhost:3306/test")
                .setUser("root")
                .setPassword("")
                .setSchema("test")
        );

        return config;
    }
}