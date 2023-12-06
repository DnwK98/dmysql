package pl.dnwk.dmysql.sql.statement.ast;

public class TransactionManagementStatement extends Statement {
    public boolean begin = false;
    public boolean commit = false;
    public boolean rollback = false;

    public static TransactionManagementStatement begin() {
        var s = new TransactionManagementStatement();
        s.begin = true;

        return s;
    }

    public static TransactionManagementStatement commit() {
        var s = new TransactionManagementStatement();
        s.commit = true;

        return s;
    }

    public static TransactionManagementStatement rollback() {
        var s = new TransactionManagementStatement();
        s.rollback = true;

        return s;
    }
}
