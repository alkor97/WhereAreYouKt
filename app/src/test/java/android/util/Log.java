package android.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class Log {
    public static int v(String tag, String message) {
        return log("v", tag, message);
    }

    public static int d(String tag, String message) {
        return log("d", tag, message);
    }

    public static int i(String tag, String message) {
        return log("i", tag, message);
    }

    public static int w(String tag, String message) {
        return log("w", tag, message);
    }

    public static int e(String tag, String message) {
        return log("e", tag, message);
    }

    public static int e(String tag, String message, Throwable e) {
        return log("e", tag, message, e);
    }

    private static int log(String type, String tag, String message) {
        return log(type, tag, message, null);
    }

    private static int log(String type, String tag, String message, Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(baos)) {
            ps.printf("%s/%s: %s\n", type, tag, message);
            if (e != null) {
                e.printStackTrace(ps);
            }
        }
        String output = baos.toString();
        System.out.println(output);
        return output.length();
    }
}
