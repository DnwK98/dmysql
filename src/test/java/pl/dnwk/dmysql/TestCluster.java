package pl.dnwk.dmysql;

import pl.dnwk.dmysql.config.ClusterConfig;
import pl.dnwk.dmysql.config.element.NodeConfig;

import java.util.HashMap;

public class TestCluster {

    public static ClusterConfig get() {
        var user = "dmysql";
        var password = "test";
        var schema = "dmysql_test_db";

        var config = new ClusterConfig();

        config.poolSize = 4;
        config.commitSemaphore = false;
        config.nodes = new HashMap<>() {{
            put("dmysql_1", NodeConfig.create()
                    .setUrl("mysql://mysql-node-1:3306/" + schema)
                    .setUser(user)
                    .setPassword(password)
                    .setSchema(schema)
            );
            put("dmysql_2", NodeConfig.create()
                    .setUrl("mysql://mysql-node-2:3306/" + schema)
                    .setUser(user)
                    .setPassword(password)
                    .setSchema(schema)
            );
            put("dmysql_3", NodeConfig.create()
                    .setUrl("mysql://mysql-node-3:3306/" + schema)
                    .setUser(user)
                    .setPassword(password)
                    .setSchema(schema)
            );
        }};

        return config;
    }
}
