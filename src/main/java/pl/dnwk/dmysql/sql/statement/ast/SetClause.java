package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SetClause implements Serializable {
    public List<SetItem> items = new ArrayList<>();
}
