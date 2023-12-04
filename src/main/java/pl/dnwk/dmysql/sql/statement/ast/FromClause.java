package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FromClause implements Serializable {
    public String table;
    public String alias = null;
    public List<Join> joins  = new ArrayList<>();
}
