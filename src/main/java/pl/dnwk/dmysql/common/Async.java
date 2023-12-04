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

    public static void executeAfter(int milliseconds, Consumer<Integer> executionCallback) {
        new Thread(() -> {
            try {
                Thread.sleep(milliseconds);
                executionCallback.accept(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
