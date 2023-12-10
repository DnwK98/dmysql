package pl.dnwk.dmysql.config;

import pl.dnwk.dmysql.config.element.NodeConfig;

import java.util.HashMap;
import java.util.Map;


public class ClusterConfig {
    public boolean commitSemaphore;
    public int poolSize;
    public Map<String, NodeConfig> nodes = new HashMap<>();
}
