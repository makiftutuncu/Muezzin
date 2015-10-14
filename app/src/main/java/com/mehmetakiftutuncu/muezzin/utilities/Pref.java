package com.mehmetakiftutuncu.muezzin.utilities;

import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.pixplicity.easyprefs.library.Prefs;

public class Pref {
    public static class CurrentLocation {
        private static final String KEY = "currentLocation";

        public static String get() {
            return Prefs.getString(KEY, "");
        }

        public static void set(int countryId, int cityId, Option<Integer> districtId) {
            Prefs.putString(KEY, countryId + "." + cityId + "." + (districtId.isDefined ? districtId.get() : "None"));
        }
    }

    public static class HijriDate {
        private static final String KEY = "hijriDate";

        public static Boolean get() {
            return Prefs.getBoolean(KEY, false);
        }

        public static void set(boolean hijriDate) {
            Prefs.putBoolean(KEY, hijriDate);
        }
    }
}
