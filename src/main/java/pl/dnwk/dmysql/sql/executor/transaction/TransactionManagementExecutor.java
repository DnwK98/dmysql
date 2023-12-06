package pl.dnwk.dmysql.sql.executor.transaction;

import pl.dnwk.dmysql.cluster.Nodes;
import pl.dnwk.dmysql.common.Random;
import pl.dnwk.dmysql.sharding.schema.DistributedSchema;
import pl.dnwk.dmysql.sql.executor.Result;
import pl.dnwk.dmysql.sql.statement.ast.TransactionManagementStatement;

import java.util.HashMap;
import java.util.Map;

public class TransactionManagementExecutor {
    private final DistributedSchema schema;
    private final Nodes nodes;

    private String activeTransaction = null;

    public TransactionManagementExecutor(DistributedSchema schema, Nodes nodes) {
        this.schema = schema;
        this.nodes = nodes;
    }

    public Result execute(TransactionManagementStatement statement) {
        if (statement.begin) {
            executeBegin();
        }
        if (statement.rollback) {
            executeRollback();
        }
        if (statement.commit) {
            executeCommit();
        }

        return Result.text("OK");
    }

    private void executeBegin() {
        if (activeTransaction != null) {
            throw new RuntimeException("There is already active transaction");
        }
        activeTransaction = Random.string(16);

        nodes.executeStatement(onAll("XA START '{id}'"));
    }

    private void executeRollback() {
        if (activeTransaction == null) {
            throw new RuntimeException("There is no active transaction");
        }

        nodes.executeStatement(onAll("XA END '{id}'"));
        nodes.executeStatement(onAll("XA ROLLBACK '{id}'"));
        activeTransaction = null;
    }

    private void executeCommit() {
        if (activeTransaction == null) {
            throw new RuntimeException("There is no active transaction");
        }

        nodes.executeStatement(onAll("XA END '{id}'"));
        nodes.executeStatement(onAll("XA PREPARE '{id}'"));
        nodes.executeStatement(onAll("XA COMMIT '{id}'"));

        activeTransaction = null;
    }

    public Map<String, String> onAll(String sql) {
        var map = new HashMap<String, String>();

        for (var node : nodes.names()) {
            map.put(node, sql.replace("{id}", node + "-" + activeTransaction));
        }

        return map;
    }
}
