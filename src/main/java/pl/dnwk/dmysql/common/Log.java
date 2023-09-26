package pl.dnwk.dmysql.common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Log {
    public static final int DEBUG = 2;
    public static final int INFO = 4;
    public static final int NOTICE = 8;
    public static final int WARNING = 16;
    public static final int ERROR = 32;

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static int level = INFO;
    private static final List<String> skippedNamespaces = new ArrayList<>();
    private static final HashMap<String, Integer> skippedNamespacesLevels = new HashMap<>();

    public static void skipNamespace(String namespace, int level) {
        skippedNamespaces.add(namespace);
        skippedNamespacesLevels.put(namespace, level);
    }

    public static void setLevel(int newLevel) {
        level = newLevel;
    }

    public static void debug(String v) {
        log(v, DEBUG);
    }

    public static void info(String v) {
        log(v, INFO);
    }

    public static void notice(String v) {
        log(v, NOTICE);
    }

    public static void warning(String v) {
        log(v, WARNING);
    }

    public static void error(String v) {
        log(v, ERROR);
    }

    private static void log(String v, int logLevel) {
        if (logLevel < level) {
            return;
        }

        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = trace[3];
        String callerClass = caller.getClassName();

        for (String skippedNamespace : skippedNamespaces) {
            if(callerClass.contains(skippedNamespace)) {
                int skippLevel = skippedNamespacesLevels.get(skippedNamespace);
                if(skippLevel >= logLevel) {
                    return;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(timeFormatter.format(LocalTime.now()));
        sb.append("][");
        sb.append(callerClass);
        sb.append("]");
        if (logLevel >= ERROR) {
            sb.append("[ERROR]");
        } else if (logLevel >= WARNING) {
            sb.append("[WARNING]");
        } else if (logLevel >= NOTICE) {
            sb.append("[NOTICE]");
        } else if (logLevel >= INFO) {
            sb.append("[INFO]");
        } else if (logLevel >= DEBUG) {
            sb.append("[DEBUG]");
        }
        sb.append(" ");
        sb.append(v);

        System.out.println(sb);
    }
}
