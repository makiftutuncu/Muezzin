package com.mehmetakiftutuncu.muezzin.fragments.preferences

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeType
import com.mehmetakiftutuncu.muezzin.utilities.Pref

class ReminderPreferencesFragment: PreferenceFragmentCompat() {
    private val ctx: Context by lazy { requireActivity() }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences_prayertimereminder)

        PrayerTimeType.values().filterNot { it == PrayerTimeType.Qibla }.forEach { initialize(it) }
    }

    private fun initialize(type: PrayerTimeType) {
        findPreference<Preference>("${Pref.Reminders.soundBase}${type.name}")?.apply {
            summary = soundSummary(Pref.Reminders.sound(ctx, type))

            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = soundSummary(newValue as String)
                true
            }
        }

        findPreference<ListPreference>("${Pref.Reminders.timeToRemindBase}${type.name}")?.apply {
            summary = timeToRemindSummary(Pref.Reminders.timeToRemind(ctx, type))

            onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                summary = timeToRemindSummary((newValue as String).toInt())
                true
            }
        }
    }

    private fun timeToRemindSummary(value: Int): String =
        getString(R.string.preferences_reminders_timeToRemindSummary, value)

    private fun soundSummary(value: String?): String =
        if (value.isNullOrBlank()) {
            getString(R.string.preferences_reminders_silent)
        } else {
            RingtoneManager.getRingtone(activity, Uri.parse(value)).getTitle(activity)
        }
}