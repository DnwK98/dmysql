package pl.dnwk.dmysql;

import pl.dnwk.dmysql.cluster.Cluster;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.config.Config;
import pl.dnwk.dmysql.connection.Connection;
import pl.dnwk.dmysql.connection.ForwardConnection;
import pl.dnwk.dmysql.tcp.TcpServer;

import java.util.LinkedList;
import java.util.List;

public class Server {

    private final Config config;
    private final List<Connection> connections = new LinkedList<>();
    private final Object connectionsLock = new Object();
    private Cluster cluster;
    private TcpServer tcpServer;

    public Server(Config config) {
        this.config = config;
    }

    public void start() {
        cluster = new Cluster();
        cluster.init();

        tcpServer = new TcpServer(config.port);
        tcpServer.setConnectionHandlerFactory(this::createConnection);

        tcpServer.run();
    }

    public Connection createConnection() {
        Connection connection = new ForwardConnection(this);
        synchronized (connectionsLock) {
            connections.add(connection);
        }

        return connection;
    }

    public void stop() {
        tcpServer.stop();
    }
}
