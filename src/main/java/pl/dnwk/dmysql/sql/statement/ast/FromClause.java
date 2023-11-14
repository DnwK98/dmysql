package pl.dnwk.dmysql.sql.statement.ast;

import java.util.ArrayList;
import java.util.List;

public class FromClause {
    public String table;
    public String alias = null;
    public List<Join> joins  = new ArrayList<>();
}
