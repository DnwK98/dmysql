package pl.dnwk.dmysql;

import pl.dnwk.dmysql.cluster.Cluster;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.connection.Connection;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.tcp.TcpServer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Server {

    private final Config config;
    private final List<Connection> connections = new LinkedList<>();
    private Cluster cluster;
    private TcpServer tcpServer = null;

    public Server(Config config) {
        this.config = config;
    }

    public void start() {
        cluster = new Cluster(config.cluster);
        cluster.init();
    }

    public void createSocket() {
        tcpServer = new TcpServer(config.port);
        tcpServer.setConnectionHandlerFactory(this::createConnection);

        tcpServer.run();
    }

    public Connection createConnection() {
        Connection connection = new Connection(this);
        synchronized (connections) {
            connections.add(connection);
        }

        return connection;
    }

    public void onConnectionClose(Connection connection) {
        synchronized (connections) {
            connections.remove(connection);
        }
    }

    public void stop() {
        Log.info("Stopping (" + connections.size() + ") connections...");
        synchronized (connections) {
            for (Connection connection: new ArrayList<>(connections)) {
                connection.close();
            }
        }

        if(tcpServer != null) {
            Log.info("Stopping TCP server...");
            tcpServer.stop();
        }

        cluster.close();

        Log.info("Server stopped successfully");
    }

    public Cluster getCluster() {
        return cluster;
    }

    public DistributedSchema getDistributedSchema() {
        return this.config.schema;
    }
}
