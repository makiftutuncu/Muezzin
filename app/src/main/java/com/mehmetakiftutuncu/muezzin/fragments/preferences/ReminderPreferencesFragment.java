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
import com.mehmetakiftutuncu.muezzin.utilities.Pref;

/**
 * Created by akif on 08/05/16.
 */
public class ReminderPreferencesFragment extends PreferenceFragment {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences_prayertimereminder);

        initializeForPrayerTime("fajr");
        initializeForPrayerTime("shuruq");
        initializeForPrayerTime("dhuhr");
        initializeForPrayerTime("asr");
        initializeForPrayerTime("maghrib");
        initializeForPrayerTime("isha");
    }

    private void initializeForPrayerTime(final String prayerTimeName) {
        String soundKey = Pref.Reminders.SOUND_BASE + prayerTimeName;

        RingtonePreference sound = (RingtonePreference) findPreference(soundKey);
        sound.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateSoundSummary(preference, (String) newValue);

                return true;
            }
        });

        String currentSound = Pref.Reminders.sound(getActivity(), prayerTimeName);
        updateSoundSummary(sound, currentSound);

        String timeToRemindKey = Pref.Reminders.TIME_TO_REMIND_BASE + prayerTimeName;

        ListPreference timeToRemind = (ListPreference) findPreference(timeToRemindKey);
        timeToRemind.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
                preference.setSummary(getString(R.string.preferences_reminders_timeToRemindSummary, Integer.parseInt((String) newValue)));

                return true;
            }
        });

        int currentTimeToRemind = Pref.Reminders.timeToRemind(getActivity(), prayerTimeName);
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
