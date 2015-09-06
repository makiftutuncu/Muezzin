package com.mehmetakiftutuncu.muezzin.utilities;

/**
 * A utility class containing constant definitions used throughout the application,
 * mostly configuration-like stuff
 *
 * @author mehmetakiftutuncu
 */
public class Conf {
    /** Package name of the application */
    public static final String PACKAGE_NAME = "com.mehmetakiftutuncu.muezzin";

    /** Relative path to folder in which data will be stored */
    public static final String DATA_PATH = "/Android/data/" + PACKAGE_NAME;

    /** Url definitions to the endpoints of Muezzin API web service */
    public static class Url {
        /** URL of the root of Muezzin API web service */
        public static final String SERVER = "https://muezzin.herokuapp.com";

        /** URL of countries API */
        public static final String countries = SERVER + "/prayertimes/countries";
    }

    /** Timeout values for HTTP requests */
    public static class Timeout {
        /** Connection timeout in seconds */
        public static final long connectTimeout = 10;

        /** Reading timeout in seconds */
        public static final long readTimeout = 10;
    }
}
