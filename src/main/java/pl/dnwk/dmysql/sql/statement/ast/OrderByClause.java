package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderByClause implements Serializable {
    public List<OrderByItem> items = new ArrayList<>();
}
