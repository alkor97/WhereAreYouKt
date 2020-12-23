package android.util;

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

    private static int log(String type, String tag, String message) {
        String complete = type + "/" + tag + ": " + message;
        System.out.println(complete);
        return complete.length();
    }
}
