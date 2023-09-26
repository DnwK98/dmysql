package pl.dnwk.dmysql.connection;

import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.common.Bytes;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.tcp.TcpConnectionHandler;

import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Connection implements TcpConnectionHandler {

    private final Server server;
    private final Nodes nodes;

    public Connection(Server server) {
        Log.debug("Db connection created");
        this.server = server;

        this.nodes = server.getCluster().get();
    }

    @Override
    public void init(Bytes output) {
        String s = "Hello!\n> ";
        output.append(s.getBytes(StandardCharsets.UTF_8));
    }

    public int handle(Bytes input, Bytes output) {
        String s = new String(input.toArray(), StandardCharsets.UTF_8);
        if(s.trim().equals("quit")) {
            return 1;
        }

        String result = executeSql(s);

        output.append("\n---\n".getBytes(StandardCharsets.UTF_8));
        output.append(result.getBytes(StandardCharsets.UTF_8));
        output.append("\n---\n> ".getBytes(StandardCharsets.UTF_8));

        return 0;
    }

    public void close() {
        server.getCluster().restore(nodes);
        server.onConnectionClose(this);
    }

    public String executeSql(String sql) {
        try {
            StringBuilder response = new StringBuilder();
            for (java.sql.Connection c: nodes.list()) {
                ResultSet results = c.createStatement().executeQuery(sql);
                int columnCount = results.getMetaData().getColumnCount();
                Log.info("Column count " + columnCount);
                while (results.next()) {
                    for (int i = 0; i < columnCount; ++i) {
                        response.append(results.getString(i + 1));
                        if(i + 1 < columnCount) {
                            response.append(" | ");
                        }
                    }

                    response.append("\n");
                }
            }

            return response.toString();

        } catch (SQLException e) {
            Log.error("Invalid SQL: " + sql + "Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
