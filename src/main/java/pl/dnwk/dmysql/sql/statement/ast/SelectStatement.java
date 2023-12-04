package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;

public class SelectStatement extends Statement implements Serializable {
    public SelectClause selectClause;
    public FromClause fromClause;
    public WhereClause whereClause;
    public GroupByClause groupByClause;
    public OrderByClause orderByClause;
}
