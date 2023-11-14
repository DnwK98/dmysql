package pl.dnwk.dmysql.sql.statement.ast;

public class ComparisonExpression implements Conditional {
    public ValueExpression left;
    public ValueExpression right;
    public String operator;


    public ComparisonExpression(ValueExpression left, String operator, ValueExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}
