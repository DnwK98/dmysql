package pl.dnwk.dmysql.connection;

import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.common.Bytes;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.tcp.TcpConnectionHandler;

public class Connection implements TcpConnectionHandler {

    private final Server server;

    public Connection(Server server) {
        Log.debug("Db connection created");
        this.server = server;
    }

    @Override
    public void init(Bytes output) {
        output.clear();
    }

    public void handle(Bytes input, Bytes output) {
        output.clear();
    }

    public void close() {
        // noop
    }
}
