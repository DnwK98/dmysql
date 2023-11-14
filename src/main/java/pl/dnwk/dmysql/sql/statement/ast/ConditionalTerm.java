package pl.dnwk.dmysql.sql.statement.ast;

import java.util.ArrayList;
import java.util.List;

public class ConditionalTerm implements Conditional {
    public List<Conditional> conditionalFactors = new ArrayList<>();
}
