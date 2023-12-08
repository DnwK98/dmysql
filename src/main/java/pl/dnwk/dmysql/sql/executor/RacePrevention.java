package pl.dnwk.dmysql.sql.executor;

import pl.dnwk.dmysql.common.Async;
import pl.dnwk.dmysql.common.Log;

/**
 * Hypothetical race prevention.
 * It would work when we assume that we have only one instance of this application.
 * This would create bottleneck which this application will become, so drop this solution.
 * Other solution would be usage of distributed lock with these semaphores for multiple instances of this application
 */
public class RacePrevention {
    private static Integer querySemaphore = 0;
    private static Integer commitSemaphore = 0;
    private static final Object lock = new Object();

    public static void inQuery() {
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

    public static void outQuery() {
        synchronized (lock) {
            querySemaphore--;
        }
    }

    public static void inCommit() {
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

    public static void outCommit() {
        synchronized (lock) {
            commitSemaphore--;
        }
    }
}
