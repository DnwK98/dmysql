package pl.dnwk.dmysql.cluster;

import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.config.ClusterConfig;
import pl.dnwk.dmysql.config.element.NodeConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Cluster {

    private final ClusterConfig config;
    private final Map<Nodes, Integer> connections = new HashMap<>();

    private static final int CONNECTION_AVAILABLE = 1;
    private static final int CONNECTION_NOT_AVAILABLE = 2;

    public Cluster(ClusterConfig config) {
        this.config = config;
    }

    public void init() {
        try {
            synchronized (connections) {
                for (int i = 0; i < config.poolSize; ++i) {
                    Map<String, Connection> nodes = new HashMap<>();
                    for (String nodeName : config.nodes.keySet()) {
                        NodeConfig node = config.nodes.get(nodeName);
                        Connection nodeConnection = DriverManager.getConnection(node.url, node.user, node.password);
                        nodeConnection.createStatement().execute("USE " + node.schema);

                        nodes.put(nodeName, nodeConnection);
                    }

                    connections.put(new Nodes(nodes), CONNECTION_AVAILABLE);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public Nodes get() {
        try {
            while (true) {
                synchronized (connections) {
                    for (Nodes pack : connections.keySet()) {
                        if (connections.get(pack) == CONNECTION_AVAILABLE) {
                            connections.put(pack, CONNECTION_NOT_AVAILABLE);
                            return pack;
                        }
                    }
                }
                Log.warning("Connections pool to small, waiting for available Nodes...");

                //noinspection BusyWait
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void restore(Nodes nodes) {
        try {
            // TODO clear nodes

            synchronized (connections) {
                connections.put(nodes, CONNECTION_AVAILABLE);
            }
            Log.debug("Nodes restored to Cluster");
        } catch (Exception e) {
            Log.error("Failed to restore ConnectionPack in cluster");
        }
    }
}
