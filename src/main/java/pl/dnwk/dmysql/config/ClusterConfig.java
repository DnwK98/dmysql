package pl.dnwk.dmysql.config;

import pl.dnwk.dmysql.config.element.NodeConfig;

import java.util.HashMap;
import java.util.Map;


public class ClusterConfig {
    public boolean commitSemaphore = false;
    public int poolSize = 2;
    public Map<String, NodeConfig> nodes = new HashMap<>() {{
        put("dmysql_1", NodeConfig.create()
                .setUrl("mysql://localhost:3306/test")
                .setUser("root")
                .setPassword("")
                .setSchema("test")
        );
    }};
}
