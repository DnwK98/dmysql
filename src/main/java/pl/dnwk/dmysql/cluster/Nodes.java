package pl.dnwk.dmysql.cluster;

import pl.dnwk.dmysql.common.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Nodes {

    Map<String, Connection> connections;

    public Nodes(Map<String, Connection> connections) {
        this.connections = connections;
    }

    public List<String> names() {
        return new ArrayList<>(connections.keySet());
    }

    public Connection getConnection(String connection) {
        return this.connections.get(connection);
    }

    public Map<String, ResultSet> executeQuery(Map<String, String> queries) {
        try {
            var threads = new HashMap<String, Thread>();
            var results = new HashMap<String, ResultSet>();

            for (var node : queries.keySet()) {
                var query = queries.get(node);
                Log.debug("Execute query on node (" + node + "): " + query);
                var connection = connections.get(node);
                var t = new Thread(() -> {
                    try {
                        ResultSet result = connection.prepareStatement(query).executeQuery();

                        synchronized (results) {
                            results.put(node, result);
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                t.start();
                threads.put(node, t);
            }
            for (var thread : threads.values()) {
                thread.join();
            }

            return results;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void executeStatement(Map<String, String> statements) {
        try {
            var threads = new ArrayList<Thread>();

            for (var node : statements.keySet()) {
                var statement = statements.get(node);
                Log.debug("Execute statement on node (" + node + "): " + statement);
                var connection = connections.get(node);
                var t = new Thread(() -> {
                    try {
                        connection.prepareStatement(statement).execute();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
                t.start();
                threads.add(t);
            }
            for (var thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
