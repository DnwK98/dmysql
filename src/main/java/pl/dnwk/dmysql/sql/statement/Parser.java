package pl.dnwk.dmysql.sql.statement;

import pl.dnwk.dmysql.sql.statement.ast.*;
import pl.dnwk.dmysql.sql.statement.lexer.Lexer;
import pl.dnwk.dmysql.sql.statement.lexer.Token;

import static pl.dnwk.dmysql.sql.statement.lexer.Lexer.*;

public class Parser {

    private final Lexer lexer;

    public Parser(String sql) {
        this.lexer = new Lexer(sql);
    }

    public static Statement parseSql(String sql) {
        Parser parser = new Parser(sql);
        return parser.parse();
    }

    public Statement parse() {
        if (lexer.nextIs(T_SELECT)) {
            return parseSelect();
        }

        // TODO INSERT, UPDATE, DELETE statements

        throw new ParseException("Expected SELECT, INSERT, UPDATE or DELETE");
    }

    private SelectStatement parseSelect() {
        lexer.match(T_SELECT);
        SelectStatement statement = new SelectStatement();

        statement.selectClause = parseSelectClause();
        statement.fromClause = parseFromClause();
        statement.whereClause = lexer.nextIs(T_WHERE) ?  parseWhereClause() : null;
        statement.groupByClause = lexer.nextIs(T_GROUP) ?  parseGroupByClause() : null;
        statement.orderByClause = lexer.nextIs(T_ORDER) ?  parseOrderByClause() : null;

        if(lexer.getNextToken() != null) {
            lexer.match(T_EOF);
        }

        return statement;
    }

    private SelectClause parseSelectClause() {
        SelectClause selectClause = new SelectClause();
        selectClause.selectExpressions.add(parseSelectExpression());
        while (lexer.nextIs(T_COMMA)) {
            lexer.match(T_COMMA);
            selectClause.selectExpressions.add(parseSelectExpression());
        }

        return selectClause;
    }

    private SelectExpression parseSelectExpression() {
        SelectExpression selectExpression = new SelectExpression();

        if(isFunction()) {
            Function function = new Function();
            lexer.match(T_IDENTIFIER);
            function.functionName = lexer.getToken().value;
            lexer.match(T_OPEN_PARENTHESIS);
            function.arguments.add(parseScalarExpression());
            while(lexer.nextIs(T_COMMA)) {
                lexer.match(T_COMMA);
                function.arguments.add(parseScalarExpression());
            }
            lexer.match(T_CLOSE_PARENTHESIS);
            selectExpression.expression = function;

        } else {
            selectExpression.expression = parseScalarExpression();
        }

        if(lexer.nextIs(T_AS)) {
            lexer.match(T_AS);
            lexer.match(T_IDENTIFIER);
            selectExpression.alias = lexer.getToken().value;
        } else if(lexer.nextIs(T_IDENTIFIER)) {
            lexer.match(T_IDENTIFIER);
            selectExpression.alias = lexer.getToken().value;
        }

        return selectExpression;
    }

    private ValueExpression parseScalarExpression() {
        if (lexer.nextIs(T_STRING)) {
            lexer.match(T_STRING);
            return Literal.String(lexer.getToken().value);
        }
        if (lexer.nextIs(T_INT)) {
            lexer.match(T_INT);
            return Literal.Numeric(lexer.getToken().value);
        }
        if (lexer.nextIs(T_FLOAT)) {
            lexer.match(T_FLOAT);
            return Literal.Numeric(lexer.getToken().value);
        }

        PathExpression pathExpression = new PathExpression();
        lexer.match(T_IDENTIFIER);

        if (lexer.nextIs(T_DOT)) {
            pathExpression.tableIdentification = lexer.getToken().value;
            lexer.match(T_DOT);
            lexer.match(T_IDENTIFIER);
            pathExpression.field = lexer.getToken().value;
        } else {
            pathExpression.field = lexer.getToken().value;
        }

        return pathExpression;
    }

    private FromClause parseFromClause() {
        lexer.match(T_FROM);

        FromClause fromClause = new FromClause();

        lexer.match(T_IDENTIFIER);
        fromClause.table = lexer.getToken().value;

        if(lexer.nextIs(T_IDENTIFIER)) {
            lexer.match(T_IDENTIFIER);
            fromClause.alias = lexer.getToken().value;
        }

        boolean checkJoin = true;
        while (checkJoin) {
            if(lexer.nextIs(T_JOIN) || lexer.nextIs(T_LEFT) || lexer.nextIs(T_RIGHT) || lexer.nextIs(T_INNER) || lexer.nextIs(T_OUTER)) {
                fromClause.joins.add(parseJoin());
            } else {
                checkJoin = false;
            }
        }


        return fromClause;
    }

    private Join parseJoin() {
        var join = new Join();
        if(lexer.nextIs(T_JOIN)) {
            lexer.match(T_JOIN);
        } else {
            lexer.moveNext();
            join.type = lexer.getToken().value.toUpperCase();
            lexer.match(T_JOIN);
        }

        lexer.match(T_IDENTIFIER);
        join.table = lexer.getToken().value;

        if(lexer.nextIs(T_IDENTIFIER)) {
            lexer.match(T_IDENTIFIER);
            join.alias = lexer.getToken().value;
        }

        if(lexer.nextIs(T_ON)) {
            lexer.match(T_ON);
            join.condition = parseConditionalExpression();
        }

        return join;
    }


    private WhereClause parseWhereClause() {
        lexer.match(T_WHERE);

        return new WhereClause(parseConditionalExpression());
    }

    private Conditional parseConditionalExpression() {
        var conditionalExpression = new ConditionalExpression();
        conditionalExpression.conditionalTerms.add(parseConditionalTerm());

        while(lexer.nextIs(T_OR)) {
            lexer.match(T_OR);
            conditionalExpression.conditionalTerms.add(parseConditionalTerm());
        }

        if(conditionalExpression.conditionalTerms.size() == 1) {
            return conditionalExpression.conditionalTerms.get(0);
        }

        return conditionalExpression;
    }

    private Conditional parseConditionalTerm() {
        var conditionalTerm = new ConditionalTerm();
        conditionalTerm.conditionalFactors.add(parseConditionalPrimary());

        while(lexer.nextIs(T_AND)) {
            lexer.match(T_AND);
            conditionalTerm.conditionalFactors.add(parseConditionalPrimary());
        }

        if(conditionalTerm.conditionalFactors.size() == 1) {
            return conditionalTerm.conditionalFactors.get(0);
        }

        return conditionalTerm;
    }

    private Conditional parseConditionalPrimary() {
        if(lexer.nextIs(T_OPEN_PARENTHESIS)) {
            lexer.match(T_OPEN_PARENTHESIS);
            var expression = parseConditionalExpression();
            lexer.match(T_CLOSE_PARENTHESIS);
            return expression;
        }

        return parseSimpleConditionalExpression();

    }

    private Conditional parseSimpleConditionalExpression() {
        var left = parseScalarExpression();
        var operator = parseComparisonOperator();

        if(operator != null) {
            var right = parseScalarExpression();
            return new ComparisonCondition(left, operator, right);
        }

        boolean not = false;
        if(lexer.nextIs(T_NOT)) {
            lexer.match(T_NOT);
            not = true;
        }

        if(lexer.nextIs(T_LIKE)) {
            return null; // TODO LIKE expression!
        }

        if(lexer.nextIs(T_IN)) {
            lexer.match(T_IN);
            lexer.match(T_OPEN_PARENTHESIS);
            var inExpression = new InListCondition();
            inExpression.path = (PathExpression) left;
            inExpression.not = not;

            var inElement = parseScalarExpression();
            if(!(inElement instanceof Literal)) {
                throw new ParseException("IN() should contain only literals.");
            }
            inExpression.literals.add((Literal) inElement);

            while (lexer.nextIs(T_COMMA)) {
                lexer.match(T_COMMA);
                inElement = parseScalarExpression();
                if(!(inElement instanceof Literal)) {
                    throw new ParseException("IN() should contain only literals.");
                }
                inExpression.literals.add((Literal) inElement);
            }

            lexer.match(T_CLOSE_PARENTHESIS);
            return inExpression;
        }

        lexer.match(T_NONE);
        throw new ParseException("Unexpected token");
    }

    private String parseComparisonOperator() {
        if(lexer.nextIs(T_EQUALS)){
            lexer.match(T_EQUALS);
            return "=";
        }

        if(lexer.nextIs(T_LT)) {
            lexer.match(T_LT);
            if(lexer.nextIs(T_EQUALS)) {
                lexer.match(T_EQUALS);
                return "<=";
            }
            if(lexer.nextIs(T_NEGATE)) {
                lexer.match(T_NEGATE);
                return "!=";
            }

            return "<";
        }

        if(lexer.nextIs(T_GT)) {
            lexer.match(T_GT);
            if(lexer.nextIs(T_EQUALS)) {
                lexer.match(T_EQUALS);
                return ">=";
            }

            return ">";
        }

        if(lexer.nextIs(T_NEGATE)) {
            lexer.match(T_NEGATE);
            lexer.match(T_EQUALS);

            return "!=";
        }

        return null;
    }

    private GroupByClause parseGroupByClause() {
        var groupBy = new GroupByClause();
        lexer.match(T_GROUP);
        lexer.match(T_BY);

        groupBy.items.add((PathExpression) parseScalarExpression());

        while (lexer.nextIs(T_COMMA)) {
            lexer.match(T_COMMA);
            groupBy.items.add((PathExpression) parseScalarExpression());
        }

        return groupBy;
    }

    private OrderByClause parseOrderByClause() {
        var orderBy = new OrderByClause();
        lexer.match(T_ORDER);
        lexer.match(T_BY);

        orderBy.items.add((PathExpression) parseScalarExpression());

        while (lexer.nextIs(T_COMMA)) {
            lexer.match(T_COMMA);
            orderBy.items.add((PathExpression) parseScalarExpression());
        }

        return orderBy;
    }

    private boolean isFunction() {
        Token peek = lexer.peek();
        lexer.resetPeek();

        return lexer.nextIs(T_IDENTIFIER) && peek.is(T_OPEN_PARENTHESIS);
    }
}
