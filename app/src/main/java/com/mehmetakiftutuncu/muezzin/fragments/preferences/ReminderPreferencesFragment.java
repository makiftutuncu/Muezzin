package com.mehmetakiftutuncu.muezzin.fragments.preferences;

import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;

/**
 * Created by akif on 08/05/16.
 */
public class ReminderPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
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

    @Override public void onResume() {
        super.onResume();

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override public void onPause() {
        super.onPause();

        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.startsWith(Pref.Reminders.ENABLED_BASE) || key.startsWith(Pref.Reminders.TIME_TO_REMIND_BASE)) {
            PrayerTimeReminder.reschedulePrayerTimeReminders(getActivity());
        }
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
