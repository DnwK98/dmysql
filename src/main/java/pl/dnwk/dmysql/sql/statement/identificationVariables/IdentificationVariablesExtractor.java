package pl.dnwk.dmysql.sql.statement.identificationVariables;

import pl.dnwk.dmysql.sql.statement.ast.*;

public class IdentificationVariablesExtractor {

    public IdentificationVariables extract(Statement statement) {
        if(statement instanceof SelectStatement) {
            return extractSelect((SelectStatement) statement);
        }
        if(statement instanceof InsertStatement) {
            return extractInsert((InsertStatement) statement);
        }

        throw new RuntimeException("Not implemented yet");
    }

    private IdentificationVariables extractSelect(SelectStatement statement) {
        var variables = new IdentificationVariables();

        identificationFromClause(statement.fromClause, variables);
        identificationSelectClause(statement.selectClause, variables);

        return variables;
    }


    private IdentificationVariables extractInsert(InsertStatement statement) {
        var variables = new IdentificationVariables();

        variables.addTable(new IdentificationVariables.Table(
                statement.table,
                statement.table
        ));

        var i = 1;
        for(var column: statement.columns) {
            variables.addField(new IdentificationVariables.Field(
                    variables.getSingleTable(),
                    column,
                    column,
                    i++
            ));
        }

        return variables;
    }

    private void identificationSelectClause(SelectClause selectClause, IdentificationVariables variables) {
        var i = 1;
        for(SelectExpression expr: selectClause.selectExpressions) {
            if(expr.expression instanceof PathExpression) {
                var e = (PathExpression) expr.expression;
                if(e.tableIdentification != null) {
                    variables.addField(new IdentificationVariables.Field(
                            variables.getTable(e.tableIdentification),
                            e.field,
                            expr.alias,
                            i
                    ));
                } else if(variables.hasSingleTable()) {
                    variables.addField(new IdentificationVariables.Field(
                            variables.getSingleTable(),
                            e.field,
                            expr.alias,
                            i
                    ));
                } else {
                    throw new RuntimeException("Not able to determine table for " + e);
                }
            } else {
                var alias = expr.alias;
                if(alias == null) {
                    if(expr.expression instanceof Function) {
                        alias = ((Function)expr.expression).functionName.toLowerCase() + "__";
                    } else if (expr.expression instanceof Literal) {
                        alias = ((Literal)expr.expression).value.toLowerCase();
                    }
                }
                variables.addField(new IdentificationVariables.Field(
                        null,
                        null,
                        alias,
                        i
                ));
            }
            ++i;
        }
    }

    private static void identificationFromClause(FromClause from, IdentificationVariables variables) {
        variables.addTable(new IdentificationVariables.Table(
                from.table,
                from.alias
        ));

        for(Join join: from.joins) {
            variables.addTable(new IdentificationVariables.Table(
                    join.table,
                    join.alias
            ));
        }
    }
}
