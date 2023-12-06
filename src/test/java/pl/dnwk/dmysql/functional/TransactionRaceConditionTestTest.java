package pl.dnwk.dmysql.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.cluster.Cluster;
import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.common.ArrayBuilder;
import pl.dnwk.dmysql.sql.executor.select.RowMapper;

import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionRaceConditionTestTest extends FunctionalTestCase {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        server.getCluster().setTransactionIsolationLevel(Cluster.IsolationLevel.READ_COMMITTED);
    }

    @Test
    public void atomicallyInsertRecordsIntoTwoShards() {
        var con1 = server.createConnection();
        var con2 = server.createConnection();

        con1.executeSql("BEGIN");
        con1.executeSql("INSERT INTO users (id, name) VALUES (1, 'Piotr')");
        con1.executeSql("INSERT INTO users (id, name) VALUES (2, 'Karol')");
        con1.executeSql("COMMIT");

        var res = con2.executeSql("SELECT name FROM users");
        assertEquals(2, res.values.length);
    }

    @Test
    public void raceCondition() {
        var n1 = server.getCluster().get();
        var n2 = server.getCluster().get();

        statement(n1, 1, "XA START 't1'");
        statement(n1, 2, "XA START 't2'");
        statement(n1, 1, "INSERT INTO users (id, name) VALUES (1, 'Piotr')");
        statement(n1, 2, "INSERT INTO users (id, name) VALUES (2, 'Karol')");
        statement(n1, 1, "XA END 't1'");
        statement(n1, 2, "XA END 't2'");
        statement(n1, 1, "XA PREPARE 't1'");
        statement(n1, 2, "XA PREPARE 't2'");
        // First query is issued before INSERT transaction commit, so it returns 0 rows
        var r1 = query(n2, 1, "SELECT name FROM users");
        statement(n1, 1, "XA COMMIT 't1'");
        statement(n1, 2, "XA COMMIT 't2'");
        // Second query is issued after INSERT transaction commit, so it returns 1 row
        var r2 = query(n2, 2, "SELECT name FROM users");

        // Then we finish with not consistent state,
        // where we transactional inserted two users and fetched only one of them as query to two nodes.
        // It's something which we must live due to nature of distributed systems.
        // Operations between many nodes are not atomic.
        // Try to hold data which must be transactional consistent on one node.
        assertEquals(0, r1.length);
        assertEquals(1, r2.length);
    }

    private void statement(Nodes nodes, int node, String sql) {
        var map = new HashMap<String, String>();
        map.put(nodes.names().toArray(new String[0])[node], sql);
        nodes.executeStatement(map);
    }

    private Object[][] query(Nodes nodes, int node, String sql) {
        try {
            var map = new HashMap<String, String>();
            map.put(nodes.names().toArray(new String[0])[node], sql);
            var res = nodes.executeQuery(map);

            var result = res.get(nodes.names().toArray(new String[0])[node]);

            var rowMapper = RowMapper.ofResult(result);
            var rows = ArrayBuilder.create2D();

            while (result.next()) {
                rows.add(rowMapper.mapRow(result));
            }

            return rows.toArray();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

