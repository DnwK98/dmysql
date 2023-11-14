package pl.dnwk.dmysql.sql.statement;

import pl.dnwk.dmysql.sql.statement.ast.*;

import java.util.ArrayList;

public class SqlWalker {
    public String walkStatement(Statement statement) {
        if (statement instanceof SelectStatement) {
            return walkSelectStatement((SelectStatement) statement);
        }

        return null; // TODO walk other statements;
    }

    private String walkSelectStatement(SelectStatement statement) {
        StringBuilder sql = new StringBuilder();
        sql.append(walkSelectClause(statement.selectClause));
        sql.append(walkFromClause(statement.fromClause));
        if(statement.whereClause != null) {
            sql.append(walkWhereClause(statement.whereClause));
        }
        if(statement.groupByClause != null) {
            sql.append(walkGroupByClause(statement.groupByClause));
        }
        if(statement.orderByClause != null) {
            sql.append(walkOrderByClause(statement.orderByClause));
        }

        return sql.toString();
    }

    private String walkSelectClause(SelectClause selectClause) {
        ArrayList<String> parts = new ArrayList<>();
        for (SelectExpression expression : selectClause.selectExpressions) {
            StringBuilder part = new StringBuilder();
            part.append(walkValueExpression(expression.expression));
            if (expression.alias != null) {
                part.append(" AS ");
                part.append(expression.alias);
            }
            parts.add(part.toString());
        }

        return "SELECT " + String.join(", ", parts);
    }

    private String walkFromClause(FromClause fromClause) {
        StringBuilder sql = new StringBuilder(" FROM ");
        sql.append(fromClause.table);
        if (fromClause.alias != null) {
            sql.append(" ");
            sql.append(fromClause.alias);
        }

        for (Join join : fromClause.joins) {
            sql.append(walkJoin(join));
        }

        return sql.toString();
    }

    private String walkJoin(Join join) {
        var sql = new StringBuilder();
        sql.append(" ");
        sql.append(join.type);
        sql.append(" JOIN ");
        sql.append(join.table);
        if (join.alias != null) {
            sql.append(" ");
            sql.append(join.alias);
        }

        if(join.condition != null) {
            sql.append(" ON ");
            sql.append(walkCondition(join.condition));
        }

        return sql.toString();
    }

    private String walkWhereClause(WhereClause whereClause) {
        var sql = new StringBuilder(" WHERE ");
        sql.append(walkCondition(whereClause.expression));

        return sql.toString();
    }

    private String walkGroupByClause(GroupByClause groupByClause) {
        var sql = new StringBuilder(" GROUP BY ");
        ArrayList<String> itemsSql = new ArrayList<>();
        for (PathExpression path : groupByClause.items) {
            itemsSql.add(walkValueExpression(path));
        }
        sql.append(String.join(", ", itemsSql));

        return sql.toString();
    }

    private String walkOrderByClause(OrderByClause orderByClause) {
        var sql = new StringBuilder(" ORDER BY ");
        ArrayList<String> itemsSql = new ArrayList<>();
        for (PathExpression path : orderByClause.items) {
            itemsSql.add(walkValueExpression(path));
        }
        sql.append(String.join(", ", itemsSql));

        return sql.toString();
    }

    private String walkCondition(Conditional condition) {
        var sql = new StringBuilder();

        if(condition instanceof ConditionalExpression) {
            var c =(ConditionalExpression) condition;
            ArrayList<String> termsSql = new ArrayList<>();
            for (Conditional term : c.conditionalTerms) {
                termsSql.add(walkCondition(term));
            }
            sql.append("(");
            sql.append(String.join(" OR ", termsSql));
            sql.append(")");
        }

        if(condition instanceof ConditionalTerm) {
            var c =(ConditionalTerm) condition;
            ArrayList<String> factorsSql = new ArrayList<>();
            for (Conditional factor : c.conditionalFactors) {
                factorsSql.add(walkCondition(factor));
            }
            sql.append(String.join(" AND ", factorsSql));
        }

        if(condition instanceof ComparisonCondition) {
            var c = (ComparisonCondition) condition;
            sql.append(walkValueExpression(c.left));
            sql.append(" ");
            sql.append(c.operator);
            sql.append(" ");
            sql.append(walkValueExpression(c.right));
        }

        if(condition instanceof InListCondition) {
            var c = (InListCondition) condition;
            sql.append(walkValueExpression(c.path));
            if(c.not) {
                sql.append(" NOT");
            }
            sql.append(" IN(");
            ArrayList<String> arguments = new ArrayList<>();
            for (ValueExpression arg : c.literals) {
                arguments.add(walkValueExpression(arg));
            }
            sql.append(String.join(", ", arguments));
            sql.append(")");
        }

        return sql.toString();
    }

    private String walkValueExpression(ValueExpression expression) {
        StringBuilder sql = new StringBuilder();

        if (expression instanceof PathExpression) {
            var pathExpression = (PathExpression) expression;
            if (pathExpression.tableIdentification != null) {
                sql.append(pathExpression.tableIdentification);
                sql.append(".");
            }
            sql.append(pathExpression.field);
        }

        if (expression instanceof Literal) {
            var literal = (Literal) expression;
            if (literal.type.equals(Literal.NUMERIC)) {
                sql.append(literal.value);
            }
            if (literal.type.equals(Literal.STRING)) {
                sql.append("'");
                sql.append(literal.value);
                sql.append("'");
            }
        }

        if (expression instanceof Function) {
            var function = (Function) expression;
            sql.append(function.functionName);
            sql.append("(");

            ArrayList<String> arguments = new ArrayList<>();
            for (ValueExpression arg : function.arguments) {
                arguments.add(walkValueExpression(arg));
            }
            sql.append(String.join(", ", arguments));
            sql.append(")");
        }

        return sql.toString();
    }
}
