package pl.dnwk.dmysql.common;

import java.util.HashMap;
import java.util.Map;

public class Timer {
    public static Map<String, Long> times = new HashMap<>();
    public static Map<String, Long> starts = new HashMap<>();

    public static void start(String name) {
        starts.put(name, System.nanoTime());
    }

    public static void stop(String name) {
        long time = times.getOrDefault(name, 0L);
        long start = starts.get(name);

        times.put(name, time + (System.nanoTime() - start));
    }

    public static void report() {
        times.forEach((String name, Long time) -> System.out.println("Time: " + name + " - " + time));
    }

}
