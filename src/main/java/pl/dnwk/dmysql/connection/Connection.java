package pl.dnwk.dmysql.connection;

import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.common.Bytes;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.sql.executor.SqlExecutor;
import pl.dnwk.dmysql.tcp.TcpConnectionHandler;

import java.nio.charset.StandardCharsets;

public class Connection implements TcpConnectionHandler {

    private final Server server;
    private final Nodes nodes;
    private final SqlExecutor sqlExecutor;

    public Connection(Server server) {
        Log.debug("Db connection created");
        this.server = server;

        this.nodes = server.getCluster().get();
        this.sqlExecutor = new SqlExecutor(server.getDistributedSchema(), this.nodes);
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
            StringBuilder result = new StringBuilder(256);
            Object[][] response = sqlExecutor.executeSql(sql);
            for (var row : response) {
                for(int i = 0; i < row.length; i++) {
                    result.append(row[i]);
                    if(i < row.length - 1) {
                        result.append(" | ");
                    }
                    result.append("\n");
                }
            }

            return result.toString();
        } catch (Exception e) {
            Log.error("Invalid SQL: " + sql + "Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
