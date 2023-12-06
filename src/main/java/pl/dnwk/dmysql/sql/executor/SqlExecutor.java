package pl.dnwk.dmysql.sql.executor;

import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.executor.delete.DeleteExecutor;
import pl.dnwk.dmysql.sql.executor.insert.InsertExecutor;
import pl.dnwk.dmysql.sql.executor.select.SelectExecutor;
import pl.dnwk.dmysql.sql.executor.transaction.TransactionManagementExecutor;
import pl.dnwk.dmysql.sql.executor.update.UpdateExecutor;
import pl.dnwk.dmysql.sql.statement.Parser;
import pl.dnwk.dmysql.sql.statement.ast.*;

public class SqlExecutor {
    private final Nodes nodes;
    private final SelectExecutor selectExecutor;
    private final InsertExecutor insertExecutor;
    private final UpdateExecutor updateExecutor;
    private final DeleteExecutor deleteExecutor;
    private final TransactionManagementExecutor transactionManagementExecutor;

    public SqlExecutor(DistributedSchema schema, Nodes nodes) {

        this.nodes = nodes;
        this.selectExecutor = new SelectExecutor(schema, nodes);
        this.insertExecutor = new InsertExecutor(schema, nodes);
        this.updateExecutor = new UpdateExecutor(schema, nodes);
        this.deleteExecutor = new DeleteExecutor(schema, nodes);
        this.transactionManagementExecutor = new TransactionManagementExecutor(schema, nodes);
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
        if(statement instanceof UpdateStatement) {
            return updateExecutor.execute((UpdateStatement) statement);
        }
        if(statement instanceof DeleteStatement) {
            return deleteExecutor.execute((DeleteStatement) statement);
        }
        if(statement instanceof TransactionManagementStatement) {
            return transactionManagementExecutor.execute((TransactionManagementStatement) statement);
        }

        return null;
    }
}
