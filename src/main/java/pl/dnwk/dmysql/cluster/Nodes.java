package pl.dnwk.dmysql.cluster;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Nodes {

    Map<String, Connection> connections;

    public Nodes(Map<String, Connection> connections) {
        this.connections = connections;
    }

    public Connection get(String name) {
        return connections.get(name);
    }

    public Connection get(int id) {
        return list().toArray(new Connection[16])[id];
    }

    public List<Connection> list() {
        return new ArrayList<>(connections.values());
    }

    public List<String> names() {
        return new ArrayList<>(connections.keySet());
    }
}
