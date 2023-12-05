package pl.dnwk.dmysql.sql.statement.ast;

import pl.dnwk.dmysql.sql.statement.identificationVariables.IdentificationVariables;

import java.io.Serializable;

public class DeleteStatement extends Statement implements Serializable {
    public FromClause fromClause;
    public WhereClause whereClause;
    public IdentificationVariables identificationVariables;
}
