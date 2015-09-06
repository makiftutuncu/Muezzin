package com.mehmetakiftutuncu.muezzin.utilities;

/**
 * A utility class for basic String operations
 *
 * @author mehmetakiftutuncu
 */
public class StringUtils {
    /**
     * Checks whether or not given String is empty
     *
     * @param s String to check
     *
     * @return true if given String is not null and not empty or false otherwise
     */
    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
