package com.mehmetakiftutuncu.muezzin.utilities;

/**
 * Created by akif on 08/05/16.
 */
public final class Log {
    public static void debug(String message, Class<?> caller, String method) {
        android.util.Log.d(tag(caller, method), message);
    }

    public static void error(String message, Class<?> caller, String method) {
        android.util.Log.e(tag(caller, method), message);
    }

    public static void error(String message, Throwable t, Class<?> caller, String method) {
        android.util.Log.e(tag(caller, method), message, t);
    }

    private static String tag(Class<?> caller, String method) {
        return caller == null ? "" : String.format("%s.%s", caller.getSimpleName(), method);
    }
}
