package com.mehmetakiftutuncu.muezzin.fragments.preferences;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeType;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;

/**
 * Created by akif on 08/05/16.
 */
public class ReminderPreferencesFragment extends PreferenceFragment {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_prayertimereminder);

        initializeForPrayerTime(PrayerTimeType.Fajr);
        initializeForPrayerTime(PrayerTimeType.Shuruq);
        initializeForPrayerTime(PrayerTimeType.Dhuhr);
        initializeForPrayerTime(PrayerTimeType.Asr);
        initializeForPrayerTime(PrayerTimeType.Maghrib);
        initializeForPrayerTime(PrayerTimeType.Isha);
    }

    private void initializeForPrayerTime(final PrayerTimeType prayerTimeType) {
        String soundKey = Pref.Reminders.SOUND_BASE + prayerTimeType.name;

        RingtonePreference sound = (RingtonePreference) findPreference(soundKey);
        sound.setOnPreferenceChangeListener((preference, newValue) -> {
            updateSoundSummary(preference, (String) newValue);

            return true;
        });

        String currentSound = Pref.Reminders.sound(getActivity(), prayerTimeType);
        updateSoundSummary(sound, currentSound);

        String timeToRemindKey = Pref.Reminders.TIME_TO_REMIND_BASE + prayerTimeType.name;

        ListPreference timeToRemind = (ListPreference) findPreference(timeToRemindKey);
        timeToRemind.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary(getString(R.string.preferences_reminders_timeToRemindSummary, Integer.parseInt((String) newValue)));

            return true;
        });

        int currentTimeToRemind = Pref.Reminders.timeToRemind(getActivity(), prayerTimeType);
        timeToRemind.setSummary(getString(R.string.preferences_reminders_timeToRemindSummary, currentTimeToRemind));
    }

    private void updateSoundSummary(Preference preference, String newValue) {
        if (newValue == null || newValue.trim().isEmpty()) {
            preference.setSummary(getString(R.string.preferences_reminders_silent));
        } else {
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(newValue));
            String name = ringtone.getTitle(getActivity());

            preference.setSummary(name);
        }
    }
}
