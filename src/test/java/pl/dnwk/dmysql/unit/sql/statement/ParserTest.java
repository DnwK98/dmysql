package pl.dnwk.dmysql.unit.sql.statement;

import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.sql.statement.Parser;
import pl.dnwk.dmysql.sql.statement.ast.*;
import pl.dnwk.dmysql.unit.UnitTestCase;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest extends UnitTestCase {

    @Test
    public void testParsesSimpleSelectClause() {
        String sql = "SELECT c.owner AS o, 123 as num, 'abc' as str FROM cars c";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        PathExpression firstExpression = (PathExpression) statement.selectClause.selectExpressions.get(0).expression;
        assertEquals("c", firstExpression.tableIdentification);
        assertEquals("owner", firstExpression.field);

        Literal secondExpression = (Literal) statement.selectClause.selectExpressions.get(1).expression;
        assertEquals(Literal.NUMERIC, secondExpression.type);
        assertEquals("123", secondExpression.value);

        Literal thirdExpression = (Literal) statement.selectClause.selectExpressions.get(2).expression;
        assertEquals(Literal.STRING, thirdExpression.type);
        assertEquals("abc", thirdExpression.value);
    }

    @Test
    public void testParsesSelectClauseWithFunction() {
        String sql = "SELECT CONCAT(owner, '-', c.name) AS owner_and_car FROM cars c";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);
        var concatFunction = (Function) statement.selectClause.selectExpressions.get(0).expression;

        assertEquals("owner", ((PathExpression) concatFunction.arguments.get(0)).field);
        assertEquals("-", ((Literal) concatFunction.arguments.get(1)).value);
        assertEquals("name", ((PathExpression) concatFunction.arguments.get(2)).field);
        assertEquals("c", ((PathExpression) concatFunction.arguments.get(2)).tableIdentification);
    }

    @Test
    public void testParsesSimpleFromClause() {
        String sql = "SELECT owner FROM cars";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        assertEquals("cars", statement.fromClause.table);
        assertNull(statement.fromClause.alias);
    }

    @Test
    public void testParsesSimpleFromClauseWithAlias() {
        String sql = "SELECT c.owner FROM cars c";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        assertEquals("cars", statement.fromClause.table);
        assertEquals("c", statement.fromClause.alias);
    }

    @Test
    public void testParsesSimpleJoin() {
        String sql = "SELECT c.owner FROM cars c LEFT JOIN owner o ON c.owner_id = o.id";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        assertEquals("owner", statement.fromClause.joins.get(0).table);
        assertEquals("o", statement.fromClause.joins.get(0).alias);

        var condition = (ComparisonCondition) statement.fromClause.joins.get(0).condition;
        assertEquals("owner_id", ((PathExpression) condition.left).field);
        assertEquals("id", ((PathExpression) condition.right).field);
        assertEquals("=", condition.operator);
    }

    @Test
    public void testParsesSimpleWhereExpression() {
        String sql = "SELECT c.owner FROM cars c WHERE c.owner = 1";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        var comparison = (ComparisonCondition) statement.whereClause.expression;
        assertEquals("owner", ((PathExpression) comparison.left).field);
        assertEquals("1", ((Literal) comparison.right).value);
    }

    @Test
    public void testParsesWhereExpressionWithFunction() {
        String sql = "SELECT c.owner FROM cars c WHERE c.owner IN(1,2,3)";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        var inList = (InListCondition) statement.whereClause.expression;

        assertEquals("1", inList.literals.get(0).value);
        assertEquals("3", inList.literals.get(2).value);
    }

    @Test
    public void testParsesWhereExpressionWithAnd() {
        String sql = "SELECT c.owner FROM cars c WHERE c.owner = 1 AND c.id > 10";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        var secondExpression = ((ConditionalTerm) statement.whereClause.expression).conditionalFactors.get(1);
        var comparison = (ComparisonCondition) secondExpression;

        assertEquals("id", ((PathExpression) comparison.left).field);
        assertEquals("10", ((Literal) comparison.right).value);
        assertEquals(">", comparison.operator);
    }

    @Test
    public void testParsesWhereExpressionWithBothAndOr() {
        String sql = "SELECT c.owner FROM cars c WHERE c.owner = 1 AND (c.id > 2 OR c.id <= 10) OR c.owner = 2";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        var parenthesis = (ConditionalExpression) ((ConditionalTerm) ((ConditionalExpression) statement.whereClause.expression).conditionalTerms.get(0)).conditionalFactors.get(1);
        var firstInParenthesis = (ComparisonCondition) parenthesis.conditionalTerms.get(0);

        assertEquals("id", ((PathExpression) firstInParenthesis.left).field);
        assertEquals("2", ((Literal) firstInParenthesis.right).value);
        assertEquals(">", firstInParenthesis.operator);
    }

    @Test
    public void testParsesGroupBy() {
        String sql = "SELECT c.owner, c.name, COUNT(c.id) FROM cars c GROUP BY c.owner, c.name";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        assertEquals("owner", ((PathExpression) statement.groupByClause.items.get(0)).field);
        assertEquals("name", ((PathExpression) statement.groupByClause.items.get(1)).field);
    }

    @Test
    public void testParsesOrderBy() {
        String sql = "" +
                "SELECT c.owner, c.name, COUNT(c.id) as count " +
                "FROM cars c " +
                "ORDER BY c.owner , c.name DESC, count ASC ";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        assertEquals("owner", ((PathExpression) statement.orderByClause.items.get(0).orderBy).field);
        assertEquals("name", ((PathExpression) statement.orderByClause.items.get(1).orderBy).field);
        assertEquals(OrderByItem.DESC, statement.orderByClause.items.get(1).direction);
    }

    @Test
    public void testParseComplexQueries() {
        String sql = "SELECT c.owner, c.name, COUNT(c.id), AVG(c.value) " +
                "FROM cars c " +
                "JOIN owner " +
                "LEFT JOIN documents d ON c.id=d.car_id " +
                "WHERE c.owner NOT IN (1,2,3) AND c.owner > 2 OR c.name != 'Jan' " +
                "GROUP BY c.owner, c.name " +
                "ORDER BY c.owner, c.name";

        SelectStatement statement = (SelectStatement) Parser.parseSql(sql);

        // Assert not failed
        assertTrue(true);
    }

    @Test
    public void testParseSimpleInsert() {
        String sql = "" +
                "INSERT INTO cars (registration, model) VALUES" +
                "('WB 1234', 'Mercedes'), " +
                "('BI 5544', 'BMW');";

        InsertStatement statement = (InsertStatement) Parser.parseSql(sql);

        assertEquals("cars", statement.table);

        assertEquals("WB 1234", statement.values.get(0).columnsValues.get(0).value);
        assertEquals("Mercedes", statement.values.get(0).columnsValues.get(1).value);

        assertEquals("BI 5544", statement.values.get(1).columnsValues.get(0).value);
        assertEquals("BMW", statement.values.get(1).columnsValues.get(1).value);
    }

    @Test
    public void testParseSimpleUpdate() {
        String sql = "" +
                "UPDATE cars SET model = 'BMW' WHERE model = 'BM_'";

        UpdateStatement statement = (UpdateStatement) Parser.parseSql(sql);

        assertEquals("model", statement.setClause.items.get(0).column.field);
        assertEquals("BMW", statement.setClause.items.get(0).value.value);

        assertEquals("model", ((ComparisonCondition) statement.whereClause.expression).left.toString());
        assertEquals("BM_", ((ComparisonCondition) statement.whereClause.expression).right.toString());
    }

    @Test
    public void testParseSimpleDelete() {
        String sql = "" +
                "DELETE FROM cars";

        DeleteStatement statement = (DeleteStatement) Parser.parseSql(sql);

        assertEquals("cars", statement.fromClause.table);
    }

    @Test
    public void testParseDeleteWithCondition() {
        String sql = "" +
                "DELETE FROM cars WHERE model = 'Mercedes'";

        DeleteStatement statement = (DeleteStatement) Parser.parseSql(sql);

        assertEquals("cars", statement.fromClause.table);
        var model = ((ComparisonCondition) statement.whereClause.expression).right.toString();
        assertEquals("Mercedes", model);
    }
}
