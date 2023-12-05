package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InsertValue extends Statement implements Serializable {
    public List<Literal> columnsValues = new ArrayList<>();
}
