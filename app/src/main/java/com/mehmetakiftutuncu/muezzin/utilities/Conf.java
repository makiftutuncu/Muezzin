package com.mehmetakiftutuncu.muezzin.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by akif on 16/05/16.
 */
public final class Conf {
    private static final String PREF_FILE = "muezzin_preferences";

    public static class Places {
        private static final String CURRENT_PLACE = "currentPlace";

        public static void setCurrentPlace(Context context, Place place) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

            sharedPreferences.edit().putString(CURRENT_PLACE, place.toString()).apply();
        }

        @NonNull public static Optional<Place> getCurrentPlace(Context context) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

            try {
                JSONObject json = new JSONObject(sharedPreferences.getString(CURRENT_PLACE, ""));

                return Place.fromJson(json);
            } catch (JSONException e) {
                return new None<>();
            }
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
