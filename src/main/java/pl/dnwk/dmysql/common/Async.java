package pl.dnwk.dmysql.common;

import java.util.function.Consumer;

public class Async {

    public static void execute(Consumer<Integer> executionCallback) {
        new Thread(() -> {
            try {
                executionCallback.accept(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static Thread executeAfter(int milliseconds, Consumer<Integer> executionCallback) {
        var t = new Thread(() -> {
            try {
                Thread.sleep(milliseconds);
                executionCallback.accept(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();

        return t;
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void waitFor(Thread[] threads) {
        for(var t: threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        sleep(10);
    }
}
