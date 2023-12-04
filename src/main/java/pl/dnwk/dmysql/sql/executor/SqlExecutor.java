package pl.dnwk.dmysql.sql.executor;

import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.executor.select.SelectExecutor;
import pl.dnwk.dmysql.sql.statement.Parser;
import pl.dnwk.dmysql.sql.statement.ast.SelectStatement;
import pl.dnwk.dmysql.sql.statement.ast.Statement;

public class SqlExecutor {
    private final Nodes nodes;
    private final SelectExecutor selectExecutor;

    public SqlExecutor(DistributedSchema schema, Nodes nodes) {

        this.nodes = nodes;
        this.selectExecutor = new SelectExecutor(schema, nodes);
    }

    public Object[][] executeSql(String sql) {
        return execute(Parser.parseSql(sql));
    }

    public Object[][] execute(Statement statement) {
        if(statement instanceof SelectStatement) {
            return selectExecutor.execute((SelectStatement) statement);
        }

        return null;
    }
}
