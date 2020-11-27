package com.mehmetakiftutuncu.muezzin.utilities

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.preference.PreferenceManager
import com.mehmetakiftutuncu.muezzin.models.Place
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeType
import org.json.JSONObject

object Pref {
    fun get(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun edit(context: Context, action: SharedPreferences.Editor.() -> Unit): Unit =
        get(context).edit().also { action(it) }.apply()

    fun appVersionName(context: Context): String =
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            // Should never happen!
            throw RuntimeException("Could not get package name: " + e.message)
        }

    fun appVersion(context: Context): Int =
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) {
                packageInfo.versionCode
            } else {
                packageInfo.longVersionCode.toInt()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // Should never happen!
            throw RuntimeException("Could not get package name: " + e.message)
        }

    object Places {
        private const val currentPlace = "currentPlace"
        private const val lastPlace    = "lastPlace"

        fun setCurrentPlace(context: Context, place: Place) {
            getCurrentPlace(context)?.takeUnless { it == place }?.also {
                edit(context) { putString(lastPlace , it.toJson()) }
            }

            edit(context) { putString(currentPlace, place.toJson()) }
        }

        fun getCurrentPlace(context: Context): Place? = getPlace(context, currentPlace)

        fun getLastPlace(context: Context): Place? = getPlace(context, lastPlace)

        private fun getPlace(context: Context, name: String): Place? =
            get(context).getString(name, "")?.runCatching {
                JSONObject(this)
            }?.fold(
                { json -> Place.fromJson(json) },
                { _ -> null }
            )
    }

    object Reminders {
        const val enabledBase = "reminders_enabled_"
        const val soundBase = "reminders_sound_"
        const val vibrationBase = "reminders_vibration_"
        const val remindBeforePrayerTimeBase = "reminders_remindBeforePrayerTime_"
        const val timeToRemindBase = "reminders_timeToRemind_"
        const val remindOnPrayerTimeBase = "reminders_remindOnPrayerTime_"
        const val uriDefaultNotificationSound = "content://settings/system/notification_sound"

        fun isEnabled(context: Context, prayerTimeType: PrayerTimeType): Boolean =
            get(context).getBoolean(enabledBase + prayerTimeType.name, false)

        fun sound(context: Context, prayerTimeType: PrayerTimeType): String =
            get(context).getString(soundBase + prayerTimeType.name, null) ?: uriDefaultNotificationSound

        fun vibrate(context: Context, prayerTimeType: PrayerTimeType): Boolean =
            get(context).getBoolean(vibrationBase + prayerTimeType.name, true)

        fun remindBeforePrayerTime(context: Context, prayerTimeType: PrayerTimeType): Boolean =
            get(context).getBoolean(remindBeforePrayerTimeBase + prayerTimeType.name, false)

        fun timeToRemind(context: Context, prayerTimeType: PrayerTimeType): Int =
            get(context).getInt(timeToRemindBase + prayerTimeType.name, 45)

        fun remindOnPrayerTime(context: Context, prayerTimeType: PrayerTimeType): Boolean =
            get(context).getBoolean(remindOnPrayerTimeBase + prayerTimeType.name, false)
    }

    object Application {
        private const val version = "version"

        fun setVersion(context: Context) =
            edit(context) { putInt(version, appVersion(context)) }

        fun getVersion(context: Context): Int =
            get(context).getInt(version, 0)
    }
}