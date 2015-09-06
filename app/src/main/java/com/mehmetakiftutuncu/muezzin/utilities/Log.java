package com.mehmetakiftutuncu.muezzin.utilities;

import com.mehmetakiftutuncu.muezzin.BuildConfig;

/**
 * A utility class for easily logging stuff, basically a wrapper over {@link android.util.Log}
 *
 * @author mehmetakiftutuncu
 */
public class Log {
    /**
     * Generates a tag for the logging using given reference to the object calling this logger
     *
     * @param loggingObject A reference to the object calling this logger
     *
     * @return A tag for the logging containing information about the caller of this logger
     */
    private static String getTag(Object loggingObject) {
        return String.format("[Muezzin.%s]", loggingObject.getClass().getSimpleName());
    }

    /**
     * Logs given message with {@link android.util.Log#INFO} logging level
     *
     * @param loggingObject A reference to the object calling this logger
     * @param message       Message to log
     */
    public static void info(Object loggingObject, String message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(getTag(loggingObject), message);
        }
    }

    /**
     * Logs an error with given message with {@link android.util.Log#ERROR} logging level
     *
     * @param loggingObject A reference to the object calling this logger
     * @param message       Error message to log
     */
    public static void error(Object loggingObject, String message) {
        android.util.Log.e(getTag(loggingObject), message);
    }

    /**
     * Logs an error with given message with {@link android.util.Log#ERROR} logging level,
     * including the stack trace of the error
     *
     * @param loggingObject A reference to the object calling this logger
     * @param message       Error message to log
     * @param error         Throwable object of the error for getting stack trace
     */
    public static void error(Object loggingObject, String message, Throwable error) {
        android.util.Log.e(getTag(loggingObject), message, error);
    }
}
