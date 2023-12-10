package pl.dnwk.dmysql.cluster.commitSemaphore;

/**
 * Commit semaphore implementation doing nothing
 */
public class NullCommitSemaphore extends CommitSemaphore{

    @Override
    public void acquire(String statement) {

    }

    @Override
    public void release(String statement) {

    }
}
