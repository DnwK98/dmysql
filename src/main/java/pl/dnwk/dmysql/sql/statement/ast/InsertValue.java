package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InsertValue implements Serializable {
    public List<Literal> columnsValues = new ArrayList<>();
}
