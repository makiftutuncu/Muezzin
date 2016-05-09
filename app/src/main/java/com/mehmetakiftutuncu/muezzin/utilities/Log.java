package com.mehmetakiftutuncu.muezzin.utilities;

/**
 * Created by akif on 08/05/16.
 */
public final class Log {
    public static void debug(Class<?> caller, String message, Object... args) {
        android.util.Log.d(tag(caller), String.format(message, args));
    }

    public static void warn(Class<?> caller, String message, Object... args) {
        android.util.Log.w(tag(caller), String.format(message, args));
    }

    public static void error(Class<?> caller, String message, Object... args) {
        android.util.Log.e(tag(caller), String.format(message, args));
    }

    public static void error(Class<?> caller, Throwable throwable, String message, Object... args) {
        android.util.Log.e(tag(caller), String.format(message, args), throwable);
    }

    private static String tag(Class<?> caller) {
        return "[Muezzin]" + (caller == null ? "" : (" [" + caller.getSimpleName() + "]"));
    }
}
