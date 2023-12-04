package pl.dnwk.dmysql.sql.statement.ast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SelectClause implements Serializable {
    public List<SelectExpression> selectExpressions = new ArrayList<>();
}
