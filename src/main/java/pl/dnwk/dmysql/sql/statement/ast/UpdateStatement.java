package pl.dnwk.dmysql.sql.statement.ast;

import pl.dnwk.dmysql.sql.statement.identificationVariables.IdentificationVariables;

import java.io.Serializable;

public class UpdateStatement extends Statement implements Serializable {
    public String table;
    public SetClause setClause;
    public WhereClause whereClause;
    public IdentificationVariables identificationVariables;
}
