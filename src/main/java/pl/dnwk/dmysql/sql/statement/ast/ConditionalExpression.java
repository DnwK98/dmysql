package pl.dnwk.dmysql.sql.statement.ast;

import java.util.ArrayList;
import java.util.List;

public class ConditionalExpression implements Conditional {
    public List<Conditional> conditionalTerms = new ArrayList<>();
}
