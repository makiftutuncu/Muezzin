package com.mehmetakiftutuncu.muezzin.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeType;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by akif on 16/05/16.
 */
public final class Pref {
    public static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static class Places {
        private static final String CURRENT_PLACE = "currentPlace";
        private static final String LAST_PLACE    = "lastPlace";

        public static void setCurrentPlace(Context context, Place place) {
            Optional<Place> maybeCurrentPlace = getCurrentPlace(context);

            if (maybeCurrentPlace.isDefined() && !maybeCurrentPlace.get().equals(place)) {
                getSharedPreferences(context).edit().putString(LAST_PLACE, maybeCurrentPlace.get().toString()).apply();
            }

            getSharedPreferences(context).edit().putString(CURRENT_PLACE, place.toString()).apply();
        }

        @NonNull public static Optional<Place> getCurrentPlace(Context context) {
            try {
                JSONObject json = new JSONObject(getSharedPreferences(context).getString(CURRENT_PLACE, ""));

                return Place.fromJson(json);
            } catch (JSONException e) {
                return Optional.empty();
            }
        }

        @NonNull public static Optional<Place> getLastPlace(Context context) {
            try {
                JSONObject json = new JSONObject(getSharedPreferences(context).getString(LAST_PLACE, ""));

                return Place.fromJson(json);
            } catch (JSONException e) {
                return Optional.empty();
            }
        }
    }

    public static class Reminders {
        public static final String ENABLED_BASE                   = "reminders_enabled_";
        public static final String SOUND_BASE                     = "reminders_sound_";
        public static final String VIBRATION_BASE                 = "reminders_vibration_";
        public static final String REMIND_BEFORE_PRAYER_TIME_BASE = "reminders_remindBeforePrayerTime_";
        public static final String TIME_TO_REMIND_BASE            = "reminders_timeToRemind_";
        public static final String REMIND_ON_PRAYER_TIME_BASE     = "reminders_remindOnPrayerTime_";

        public static final String URI_DEFAULT_NOTIFICATION_SOUND = "content://settings/system/notification_sound";

        public static boolean isEnabled(Context context, PrayerTimeType prayerTimeType) {
            return getSharedPreferences(context).getBoolean(ENABLED_BASE + prayerTimeType.name, false);
        }

        public static String sound(Context context, PrayerTimeType prayerTimeType) {
            return getSharedPreferences(context).getString(SOUND_BASE + prayerTimeType.name, URI_DEFAULT_NOTIFICATION_SOUND);
        }

        public static boolean vibrate(Context context, PrayerTimeType prayerTimeType) {
            return getSharedPreferences(context).getBoolean(VIBRATION_BASE + prayerTimeType.name, true);
        }

        public static boolean remindBeforePrayerTime(Context context, PrayerTimeType prayerTimeType) {
            return getSharedPreferences(context).getBoolean(REMIND_BEFORE_PRAYER_TIME_BASE + prayerTimeType.name, false);
        }

        public static int timeToRemind(Context context, PrayerTimeType prayerTimeType) {
            return Integer.parseInt(getSharedPreferences(context).getString(TIME_TO_REMIND_BASE + prayerTimeType.name, "45"));
        }

        public static boolean remindOnPrayerTime(Context context, PrayerTimeType prayerTimeType) {
            return getSharedPreferences(context).getBoolean(REMIND_ON_PRAYER_TIME_BASE + prayerTimeType.name, false);
        }
    }

    public static class Application {
        private static final String VERSION = "version";

        public static void setVersion(Context context) {
            getSharedPreferences(context).edit().putInt(VERSION, getAppVersion(context)).apply();
        }

        @NonNull public static int getVersion(Context context) {
            return getSharedPreferences(context).getInt(VERSION, 0);
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

    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // Should never happen!
            throw new RuntimeException("Could not get package name: " + e.getMessage());
        }
    }
}
