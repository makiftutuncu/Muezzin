package com.mehmetakiftutuncu.muezzin.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by akif on 16/05/16.
 */
public final class Pref {
    private static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static class Places {
        private static final String CURRENT_PLACE = "currentPlace";
        private static final String LAST_PLACE    = "lastPlace";

        public static void setCurrentPlace(Context context, Place place) {
            Optional<Place> maybeCurrentPlace = getCurrentPlace(context);

            if (maybeCurrentPlace.isDefined && !maybeCurrentPlace.get().equals(place)) {
                getSharedPreferences(context).edit().putString(LAST_PLACE, maybeCurrentPlace.get().toString()).apply();
            }

            getSharedPreferences(context).edit().putString(CURRENT_PLACE, place.toString()).apply();
        }

        @NonNull public static Optional<Place> getCurrentPlace(Context context) {
            try {
                JSONObject json = new JSONObject(getSharedPreferences(context).getString(CURRENT_PLACE, ""));

                return Place.fromJson(json);
            } catch (JSONException e) {
                return new None<>();
            }
        }

        @NonNull public static Optional<Place> getLastPlace(Context context) {
            try {
                JSONObject json = new JSONObject(getSharedPreferences(context).getString(LAST_PLACE, ""));

                return Place.fromJson(json);
            } catch (JSONException e) {
                return new None<>();
            }
        }
    }

    public static class Reminders {
        public static final String ENABLED_BASE        = "reminders_enabled_";
        public static final String SOUND_BASE          = "reminders_sound_";
        public static final String VIBRATION_BASE      = "reminders_vibration_";
        public static final String TIME_TO_REMIND_BASE = "reminders_timeToRemind_";

        public static final String URI_DEFAULT_NOTIFICATION_SOUND = "content://settings/system/notification_sound";

        public static boolean isEnabled(Context context, String prayerTimeName) {
            return getSharedPreferences(context).getBoolean(ENABLED_BASE + prayerTimeName, false);
        }

        public static String sound(Context context, String prayerTimeName) {
            return getSharedPreferences(context).getString(SOUND_BASE + prayerTimeName, URI_DEFAULT_NOTIFICATION_SOUND);
        }

        public static boolean vibrate(Context context, String prayerTimeName) {
            return getSharedPreferences(context).getBoolean(VIBRATION_BASE + prayerTimeName, true);
        }

        public static int timeToRemind(Context context, String prayerTimeName) {
            return getSharedPreferences(context).getInt(TIME_TO_REMIND_BASE + prayerTimeName, 45);
        }
    }

    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Should never happen!
            throw new RuntimeException("Could not get package name: " + e.getMessage());
        }
    }
}
