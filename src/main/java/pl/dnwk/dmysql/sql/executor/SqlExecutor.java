package pl.dnwk.dmysql.sql.executor;

import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.executor.insert.InsertExecutor;
import pl.dnwk.dmysql.sql.executor.select.SelectExecutor;
import pl.dnwk.dmysql.sql.statement.Parser;
import pl.dnwk.dmysql.sql.statement.ast.InsertStatement;
import pl.dnwk.dmysql.sql.statement.ast.SelectStatement;
import pl.dnwk.dmysql.sql.statement.ast.Statement;

public class SqlExecutor {
    private final Nodes nodes;
    private final SelectExecutor selectExecutor;
    private final InsertExecutor insertExecutor;

    public SqlExecutor(DistributedSchema schema, Nodes nodes) {

        this.nodes = nodes;
        this.selectExecutor = new SelectExecutor(schema, nodes);
        this.insertExecutor = new InsertExecutor(schema, nodes);
    }

    public Result executeSql(String sql) {
        return execute(Parser.parseSql(sql));
    }

    public Result execute(Statement statement) {
        if(statement instanceof SelectStatement) {
            return selectExecutor.execute((SelectStatement) statement);
        }
        if(statement instanceof InsertStatement) {
            return insertExecutor.execute((InsertStatement) statement);
        }

        return null;
    }
}
