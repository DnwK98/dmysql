package pl.dnwk.dmysql.sql.statement.ast;

public class ComparisonCondition implements Conditional {
    public ValueExpression left;
    public ValueExpression right;
    public String operator;


    public ComparisonCondition(ValueExpression left, String operator, ValueExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }
}
