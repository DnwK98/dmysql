package pl.dnwk.dmysql.sql.statement.ast;

import pl.dnwk.dmysql.sql.statement.identificationVariables.IdentificationVariables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InsertStatement extends Statement implements Serializable {
    public String table;
    public List<String> columns = new ArrayList<>();
    public List<InsertValue> values = new ArrayList<>();

    public IdentificationVariables identificationVariables;
}
