package pl.dnwk.dmysql.cluster;

import pl.dnwk.dmysql.cluster.commitSemaphore.CommitSemaphore;
import pl.dnwk.dmysql.common.Log;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Nodes {

    final Map<String, Connection> connections;
    final Map<String, String> transactions =  new HashMap<>();
    private final CommitSemaphore commitSemaphore;

    public Nodes(Map<String, Connection> connections, CommitSemaphore commitSemaphore) {
        this.connections = connections;
        this.commitSemaphore = commitSemaphore;
    }

    public List<String> names() {
        return new ArrayList<>(connections.keySet());
    }

    public Connection getConnection(String connection) {
        return this.connections.get(connection);
    }

    public Map<String, ResultSet> executeQuery(Map<String, String> queries) {
        var results = new HashMap<String, ResultSet>();
        if(queries.isEmpty()) {
            return results;
        }
        try {
            var threads = new HashMap<String, Thread>();
            var errors = new ArrayList<String>();
            commitSemaphore.acquire(queries.values().stream().findFirst().get());

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
                        Log.error("Error during executing query (" + query + "): " + e.getMessage());
                        synchronized (errors) {
                            errors.add(e.getMessage());
                        }
                    }
                });
                t.start();
                threads.put(node, t);
            }
            for (var thread : threads.values()) {
                thread.join();
            }
            if(!errors.isEmpty()) {
                throw new RuntimeException(errors.stream().findFirst().get());
            }

            return results;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            commitSemaphore.release(queries.values().stream().findFirst().get());
        }
    }

    public void executeStatement(Map<String, String> statements) {
        if(statements.isEmpty()) {
            return;
        }
        try {
            var threads = new ArrayList<Thread>();
            var errors = new ArrayList<String>();
            manageTransactions(statements);
            commitSemaphore.acquire(statements.values().stream().findFirst().get());

            for (var node : statements.keySet()) {
                var statement = statements.get(node);
                Log.debug("Execute statement on node (" + node + "): " + statement);
                var connection = connections.get(node);
                var t = new Thread(() -> {
                    try {
                        connection.prepareStatement(statement).execute();
                    } catch (SQLException e) {
                        Log.error("Error during executing statement (" + statement + "): " + e.getMessage());
                        synchronized (errors) {
                            errors.add(e.getMessage());
                        }
                    }
                });
                t.start();
                threads.add(t);
            }
            for (var thread : threads) {
                thread.join();
            }
            if(!errors.isEmpty()) {
                throw new RuntimeException(errors.stream().findFirst().get());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            commitSemaphore.release(statements.values().stream().findFirst().get());
        }
    }

    public void rollbackTransactions() {
        for (var node: transactions.keySet().toArray(new String[0])) {
            var transaction = transactions.get(node);
            executeStatement(new HashMap<>(){{put(node, "XA END" + transaction);}});
            executeStatement(new HashMap<>(){{put(node, "XA ROLLBACK" + transaction);}});
        }
    }

    private void manageTransactions(Map<String, String> statements) {
        for (var node: statements.keySet()) {
            var statement = statements.get(node);
            if(statement.contains("XA START")) {
                transactions.put(node, statement.replace("XA START", ""));
            }
            if(statement.contains("XA COMMIT") || statement.contains("XA ROLLBACK")) {
                transactions.remove(node);
            }
        }
    }
}
