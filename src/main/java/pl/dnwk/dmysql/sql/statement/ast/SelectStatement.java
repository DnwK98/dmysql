package pl.dnwk.dmysql.sql.statement.ast;

public class SelectStatement extends Statement {
    public SelectClause selectClause;
    public FromClause fromClause;
    public WhereClause whereClause;
    public GroupByClause groupByClause;
    public OrderByClause orderByClause;
}
