package pl.dnwk.dmysql.tcp;

import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.connection.Connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class TcpServer {
    ServerSocket socket;
    int port = 3306;
    private boolean shouldStop = false;

    private final List<TcpConnection> connections = new LinkedList<>();
    private final Object connectionsLock = new Object();
    private Supplier<TcpConnectionHandler> connectionHandlerFactory;
    private Thread socketThread;

    public TcpServer() {
    }

    public TcpServer(int port) {
        this.port = port;
    }

    public int run() {
        try {
            socket = new ServerSocket(port);
            Log.info("Server is listening on port " + port);
        } catch (IOException e) {
            Log.error("Failed to start listening on port " + port + " Error: " + e.getMessage());
            return 1;
        }

        socketThread = new Thread(() -> {
            while (!shouldStop) {
                try {
                    Socket client = socket.accept();

                    TcpConnection connection = new TcpConnection(client);
                    connection.setHandler(connectionHandlerFactory.get());

                    synchronized (connectionsLock) {
                        connections.add(connection);
                    }

                    new Thread(connection::handle).start();

                } catch (IOException e) {
                    Log.warning("Socket accept failure: " + e.getMessage());
                }
            }
        });
        socketThread.start();

        return 0;
    }

    public void stop() {
        try {
            synchronized (connectionsLock) {
                for (TcpConnection connection : connections) {
                    connection.close();
                }
            }

            shouldStop = true;
            socketThread.interrupt();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setConnectionHandlerFactory(Supplier<TcpConnectionHandler> factory) {
        connectionHandlerFactory = factory;
    }
}
