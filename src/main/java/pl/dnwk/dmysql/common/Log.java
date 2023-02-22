package pl.dnwk.dmysql.common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Log {
    public static final int DEBUG = 2;
    public static final int INFO = 4;
    public static final int NOTICE = 8;
    public static final int WARNING = 16;
    public static final int ERROR = 32;

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static int level = INFO;


    public static void setLevel(int newLevel)
    {
        level = newLevel;
    }
    public static void debug(String v)
    {
        log(v, DEBUG);
    }

    public static void info(String v)
    {
        log(v, INFO);
    }

    public static void notice(String v)
    {
        log(v, NOTICE);
    }

    public static void warning(String v)
    {
        log(v, WARNING);
    }

    public static void error(String v)
    {
        log(v, ERROR);
    }

    private static void log(String v, int logLevel)
    {
        if(logLevel < level) {
            return;
        }

        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = trace[3];

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(timeFormatter.format(LocalTime.now()));
        sb.append("][");
        sb.append(caller.getClassName());
        sb.append("]");
        if(logLevel >= ERROR) {
            sb.append("[ERROR]");
        } else if(logLevel >= WARNING) {
            sb.append("[WARNING]");
        } else if(logLevel >= NOTICE) {
            sb.append("[NOTICE]");
        } else if(logLevel >= INFO) {
            sb.append("[INFO]");
        } else if(logLevel >= DEBUG) {
            sb.append("[DEBUG]");
        }
        sb.append(" ");
        sb.append(v);

        System.out.println(sb);
    }
}
