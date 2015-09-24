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
}
