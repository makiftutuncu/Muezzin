package com.mehmetakiftutuncu.muezzin.utilities;

import com.mehmetakiftutuncu.muezzin.BuildConfig;

/**
 * A utility class for easily logging stuff, basically a wrapper over {@link android.util.Log}
 *
 * @author mehmetakiftutuncu
 */
public class Log {
    /**
     * Logs given message with {@link android.util.Log#INFO} logging level
     *
     * @param tag     A reference to the object calling this logger
     * @param message Message to log
     */
    public static void info(String tag, String message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i("[" + tag + "]", message);
        }
    }

    /**
     * Logs an error with given message with {@link android.util.Log#ERROR} logging level
     *
     * @param tag     A reference to the object calling this logger
     * @param message Error message to log
     */
    public static void error(String tag, String message) {
        android.util.Log.e("[" + tag + "]", message);
    }

    /**
     * Logs an error with given message with {@link android.util.Log#ERROR} logging level,
     * including the stack trace of the error
     *
     * @param tag     A reference to the object calling this logger
     * @param message Error message to log
     * @param error   Throwable object of the error for getting stack trace
     */
    public static void error(String tag, String message, Throwable error) {
        android.util.Log.e("[" + tag + "]", message, error);
    }
}
