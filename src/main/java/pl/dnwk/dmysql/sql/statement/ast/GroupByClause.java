package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GroupByClause implements Serializable {
    public List<ValueExpression> items = new ArrayList<>();
}
