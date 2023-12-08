package pl.dnwk.dmysql.functional.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.cluster.Cluster;
import pl.dnwk.dmysql.functional.FunctionalTestCase;
import pl.dnwk.dmysql.sql.executor.Result;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionalTest extends FunctionalTestCase {

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        server.getCluster().setTransactionIsolationLevel(Cluster.IsolationLevel.READ_COMMITTED);
    }

    @Test
    public void otherThreadDoesNotSeeNotCommittedTransaction() {
        var con1 = server.createConnection();
        var con2 = server.createConnection();

        con1.executeSql("BEGIN");
        con1.executeSql("INSERT INTO users (id, name) VALUES (1, 'Piotr')");

        var res = con2.executeSql("SELECT id FROM users");
        assertEquals(0, res.values.length);
    }

    @Test
    public void otherThreadSeeCommittedTransaction() {
        var con1 = server.createConnection();
        var con2 = server.createConnection();

        con1.executeSql("BEGIN");
        con1.executeSql("INSERT INTO users (id, name) VALUES (1, 'Piotr')");
        con1.executeSql("COMMIT");

        var res = con2.executeSql("SELECT id FROM users");
        assertEquals(1, res.values.length);
    }

    @Test
    public void sameThreadSeesNotCommittedChanges() {
        var con = server.createConnection();

        con.executeSql("BEGIN");
        con.executeSql("INSERT INTO users (id, name) VALUES (1, 'Piotr')");

        var res = con.executeSql("SELECT id FROM users");
        assertEquals(1, res.values.length);
    }

    @Test
    public void readCommitted() {
        var con0 = server.createConnection();
        con0.executeSql("BEGIN");
        con0.executeSql("INSERT INTO users (id, name) VALUES (1, 'Piotr')");
        con0.executeSql("COMMIT");
        // Do not restore this connection to cluster, because next create connection will use it.
        // Then isolation level will be unable to test.

        var con1 = server.createConnection();
        var con2 = server.createConnection();
        Result res = null;

        con1.executeSql("BEGIN");
        res = con1.executeSql("SELECT id, name FROM users WHERE id=1");
        assertEquals("Piotr", res.values[0][1]);

        con2.executeSql("BEGIN");
        con2.executeSql("UPDATE users SET name='Pawel' WHERE id=1");

        res = con1.executeSql("SELECT id, name FROM users WHERE id=1");
        assertEquals("Piotr", res.values[0][1]);

        con2.executeSql("COMMIT");

        res = con1.executeSql("SELECT id, name FROM users WHERE id=1");
        assertEquals("Pawel", res.values[0][1]);
    }

    @Test
    public void repeatableRead() {
        server.getCluster().setTransactionIsolationLevel(Cluster.IsolationLevel.REPEATABLE_READ);
        var con0 = server.createConnection();
        con0.executeSql("BEGIN");
        con0.executeSql("INSERT INTO users (id, name) VALUES (1, 'Piotr')");
        con0.executeSql("COMMIT");
        // Do not restore this connection to cluster, because next create connection will use it.
        // Then isolation level will be unable to test.

        var con1 = server.createConnection();
        var con2 = server.createConnection();
        Result res = null;

        con1.executeSql("BEGIN");
        res = con1.executeSql("SELECT id, name FROM users WHERE id=1");
        assertEquals("Piotr", res.values[0][1]);

        con2.executeSql("BEGIN");
        con2.executeSql("UPDATE users SET name='Pawel' WHERE id=1");
        con2.executeSql("COMMIT");

        // After other thread commit on repeatable read isolation we get Piotr again
        res = con1.executeSql("SELECT id, name FROM users WHERE id=1");
        assertEquals("Piotr", res.values[0][1]);

        // When we roll back transaction, then we get Pawel, saved in other thread.
        con1.executeSql("ROLLBACK");
        res = con1.executeSql("SELECT id, name FROM users WHERE id=1");
        assertEquals("Pawel", res.values[0][1]);
    }
}
