package pl.dnwk.dmysql.unit.sql.statement;

import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.sql.statement.Parser;
import pl.dnwk.dmysql.sql.statement.SqlWalker;
import pl.dnwk.dmysql.sql.statement.ast.*;
import pl.dnwk.dmysql.unit.UnitTestCase;

import static org.junit.jupiter.api.Assertions.*;

public class SqlWalkerTest extends UnitTestCase {

    SqlWalker walker = new SqlWalker();

    @Test
    public void testWalkSimpleSelectStatement() {
        String sql = "SELECT c.owner, 123 AS num, 'abc' AS str, CONCAT(owner, '-', c.name) AS owner_and_car FROM cars c";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkSimpleJoin() {
        String sql = "SELECT c.owner FROM cars c LEFT JOIN owner o ON c.owner_id = o.id";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkSimpleWhereExpression() {
        String sql = "SELECT c.owner FROM cars c WHERE c.owner = 1";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkWhereExpressionWithFunction() {
        String sql = "SELECT c.owner FROM cars c WHERE c.owner IN(1, 2, 3)";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkWhereExpressionWithAnd() {
        String sql = "SELECT c.owner FROM cars c WHERE c.owner = 1 AND c.id > 10";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkWhereExpressionWithBothAndOr() {
        String sql = "SELECT c.owner FROM cars c WHERE (c.owner = 1 AND (c.id > 2 OR c.id <= 10) OR c.owner = 2)";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkOrderByGroupBy() {
        String sql = "SELECT c.owner, c.name, COUNT(c.id) FROM cars c GROUP BY c.owner, c.name ORDER BY c.owner, c.name";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkSimpleUpdate() {
        String sql = "UPDATE cars SET model = 'BMW' WHERE model = 'BM_'";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkSimpleInsert() {
        String sql = "INSERT INTO cars (registration, model) VALUES ('WB 1234', 'Mercedes'), ('BI 5544', 'BMW')";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkDeleteWithCondition() {
        String sql = "DELETE FROM cars WHERE model = 'Mercedes'";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkTransactionBeginStatement() {
        String sql = "BEGIN";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkTransactionCommitStatement() {
        String sql = "COMMIT";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }

    @Test
    public void testWalkTransactionRollbackStatement() {
        String sql = "ROLLBACK";

        Statement statement = Parser.parseSql(sql);
        String responseSql = walker.walkStatement(statement);

        assertEquals(sql, responseSql);
    }
}
