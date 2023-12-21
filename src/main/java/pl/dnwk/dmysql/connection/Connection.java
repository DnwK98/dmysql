package pl.dnwk.dmysql.connection;

import pl.dnwk.dmysql.Server;
import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.common.Bytes;
import pl.dnwk.dmysql.common.Log;
import pl.dnwk.dmysql.common.StringUtils;
import pl.dnwk.dmysql.sql.executor.Result;
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

        String result = execute(s);

        output.append("\n---\n".getBytes(StandardCharsets.UTF_8));
        output.append(result.getBytes(StandardCharsets.UTF_8));
        output.append("\n---\n> ".getBytes(StandardCharsets.UTF_8));

        return 0;
    }

    public void close() {
        server.getCluster().restore(nodes);
        server.onConnectionClose(this);
    }

    public String execute(String sql) {
        try {
            StringBuilder response = new StringBuilder(256);
            Result result = executeSql(sql);

            if(result.text != null) {
                response.append(result.text);
                return response.toString();
            }

            var maxFieldLength = getMaxFieldLength(result);

            for(int i = 0; i < result.columns.length; i++) {
                response.append(StringUtils.pad(result.columns[i], maxFieldLength));
                if(i < result.columns.length - 1) {
                    response.append(" | ");
                }
            }
            response.append("\n")
                    .append("-".repeat((maxFieldLength + 3) * result.columns.length))
                    .append("\n");

            for (var row : result.values) {
                for (int i = 0; i < row.length; i++) {
                    response.append(StringUtils.pad(String.valueOf(row[i]), maxFieldLength));
                    if (i < row.length - 1) {
                        response.append(" | ");
                    }
                }
                response.append("\n");
            }

            response.append("\n");
            response.append("Count: ");
            response.append(result.values.length);

            return response.toString();
        } catch (Exception e) {
            Log.error("Invalid SQL: " + sql + "Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private int getMaxFieldLength(Result result) {
        var max = 5;
        for(var c: result.columns) {
            if(c.length() > max) {
                max = c.length();
            }
        }
        for(var r: result.values) {
            for(var c: r) {
                var cVal = String.valueOf(c);
                if(cVal.length() > max) {
                    max = cVal.length();
                }
            }
        }

        return max + 1;
    }

    public Result executeSql(String sql) {
        return sqlExecutor.executeSql(sql);
    }
}
