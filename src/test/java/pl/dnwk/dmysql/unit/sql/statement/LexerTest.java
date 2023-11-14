package pl.dnwk.dmysql.unit.sql.statement;

import org.junit.jupiter.api.Test;
import pl.dnwk.dmysql.unit.UnitTestCase;
import pl.dnwk.dmysql.sql.statement.lexer.Lexer;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static pl.dnwk.dmysql.sql.statement.lexer.Lexer.*;

public class LexerTest extends UnitTestCase {

    @Test
    public void testCreateTokensForSelect() {
        String sql = "" +
                "SELECT t.id, t.name " +
                "FROM table t " +
                "WHERE t.name='abc' AND t.id=1";

        Lexer lexer = new Lexer(sql);

        List<String> tokens = Arrays.asList(
                "SELECT", "t", ".", "id", ",", "t", ".", "name",
                "FROM", "table", "t",
                "WHERE", "t", ".", "name", "=", "abc", "AND", "t", ".", "id", "=", "1"
        );

        List<Integer> tokensTypes = Arrays.asList(
                T_SELECT, T_IDENTIFIER, T_DOT, T_IDENTIFIER, T_COMMA, T_IDENTIFIER, T_DOT, T_IDENTIFIER,
                T_FROM, T_IDENTIFIER, T_IDENTIFIER,
                T_WHERE, T_IDENTIFIER, T_DOT, T_IDENTIFIER, T_EQUALS, T_STRING, T_AND, T_IDENTIFIER, T_DOT, T_IDENTIFIER, T_EQUALS, T_INT
        );

        for (String token: tokens) {
            lexer.moveNext();
            assertEquals(token, lexer.getToken().value);
        }

        lexer.reset();
        for (int tokenType: tokensTypes) {
            lexer.moveNext();
            assertEquals(tokenType, lexer.getToken().type);
        }
    }

    @Test
    public void testCreateTokensForMathOperationAndFunction() {
        String sql = "" +
                "SELECT " +
                "t.one + t.two, " +
                "FUNCTION(t.name) " +
                "FROM table t";

        Lexer lexer = new Lexer(sql);

        List<String> tokens = Arrays.asList(
                "SELECT",
                "t", ".", "one", "+", "t", ".", "two", ",",
                "FUNCTION", "(", "t", ".", "name", ")",
                "FROM", "table", "t"
        );

        for (String token: tokens) {
            lexer.moveNext();
            assertEquals(token, lexer.getToken().value);
        }
    }

    @Test
    public void testCreateTokensForJoin() {
        String sql = "" +
                "SELECT t.id FROM table t " +
                "INNER JOIN second s on t.id=s.table_id " +
                "WHERE t.name='abc'";

        Lexer lexer = new Lexer(sql);

        List<String> tokens = Arrays.asList(
                "SELECT", "t", ".", "id", "FROM", "table", "t",
                "INNER", "JOIN", "second", "s", "on", "t", ".", "id", "=", "s", ".", "table_id",
                "WHERE", "t", ".", "name", "=", "abc"
        );

        for (String token: tokens) {
            lexer.moveNext();
            assertEquals(token, lexer.getToken().value);
        }
    }

    @Test
    public void testEscapesString()
    {
        String sql = "" +
                "SELECT * FROM table t " +
                "WHERE t.name='a.b*c''d'";

        Lexer lexer = new Lexer(sql);

        List<String> tokens = Arrays.asList(
                "SELECT", "*", "FROM", "table", "t",
                "WHERE", "t", ".", "name", "=", "a.b*c'd"
        );

        for (String token: tokens) {
            lexer.moveNext();
            assertEquals(token, lexer.getToken().value);
        }
    }
}
