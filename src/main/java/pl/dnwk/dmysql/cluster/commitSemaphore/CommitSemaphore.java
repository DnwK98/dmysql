package pl.dnwk.dmysql.cluster.commitSemaphore;

import pl.dnwk.dmysql.common.Async;
import pl.dnwk.dmysql.common.Log;

/**
 * Hypothetical race prevention.
 * It would work when we assume that we have only one instance of this application.
 * This would create bottleneck which this application will become, it could run only in one instance.
 * Other solution would be usage of distributed lock with these semaphores for multiple instances of this application
 */
public class CommitSemaphore {
    private Integer querySemaphore = 0;
    private Integer commitSemaphore = 0;
    private final Object lock = new Object();

    public void acquire(String statement) {
        if(statement.contains("XA COMMIT")) {
            acquireCommit();
        } else {
            acquireQuery();
        }
    }

    public void release(String statement) {
        if(statement.contains("XA COMMIT")) {
            releaseCommit();
        } else {
            releaseQuery();
        }
    }

    private void acquireQuery() {
        var wait = true;
        while (wait) {
            wait = false;
            synchronized (lock) {
                if (commitSemaphore > 0) {
                    Log.debug("Waiting with query for commit, q:" + querySemaphore + " c:" + commitSemaphore);
                    wait = true;
                } else {
                    querySemaphore++;
                }
            }
            if (wait) {
                Async.sleep(10);
            }
        }
    }

    private void releaseQuery() {
        synchronized (lock) {
            querySemaphore--;
        }
    }

    private void acquireCommit() {
        synchronized (lock) {
            commitSemaphore++;
        }

        var wait = true;
        while (wait) {
            wait = false;

            // This lock is optional, because when commit semaphore
            // is greater than 0, then query semaphore can't grow
            synchronized (lock) {
                if (querySemaphore > 0) {
                    Log.warning("Waiting with COMMIT for queries, q:" + querySemaphore + " c:" + commitSemaphore);
                    wait = true;
                }
            }
            if (wait) {
                Async.sleep(10);
            }
        }
    }

    private void releaseCommit() {
        synchronized (lock) {
            commitSemaphore--;
        }
    }
}
