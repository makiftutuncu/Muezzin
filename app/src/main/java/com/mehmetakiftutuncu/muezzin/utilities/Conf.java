package com.mehmetakiftutuncu.muezzin.utilities;

import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;

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
        public static String countries(boolean force) {
            return SERVER + "/countries" + (force ? "/force" : "");
        }

        /** URL of countries API */
        public static String countries() {
            return countries(false);
        }

        /** URL of cities API */
        public static String cities(int countryId, boolean force) {
            return SERVER + "/" + countryId + "/cities" + (force ? "/force" : "");
        }

        /** URL of cities API */
        public static String cities(int countryId) {
            return cities(countryId, false);
        }

        /** URL of districts API */
        public static String districts(int cityId, boolean force) {
            return SERVER + "/" + cityId + "/districts" + (force ? "/force" : "");
        }

        /** URL of districts API */
        public static String districts(int cityId) {
            return districts(cityId, false);
        }

        /** URL of prayer times API */
        public static String prayerTimes(int countryId, int cityId, Option<Integer> districtId, boolean force) {
            return SERVER + "/prayertimes/" + countryId + "/" + cityId + "/" + (districtId.isDefined ? districtId.get() : "None") + (force ? "/force" : "");
        }

        /** URL of prayer times API */
        public static String prayerTimes(int countryId, int cityId, Option<Integer> districtId) {
            return prayerTimes(countryId, cityId, districtId, false);
        }

        /** URL of prayer times API */
        public static String prayerTimes(int countryId, int cityId, boolean force) {
            return prayerTimes(countryId, cityId, new None<Integer>(), force);
        }

        /** URL of prayer times API */
        public static String prayerTimes(int countryId, int cityId) {
            return prayerTimes(countryId, cityId, new None<Integer>(), false);
        }
    }

    /** Timeout values for HTTP requests */
    public static class Timeout {
        /** Connection timeout in seconds */
        public static final long connectTimeout = 10;

        /** Reading timeout in seconds */
        public static final long readTimeout = 10;
    }
}
