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

/**
 * Created by akif on 08/05/16.
 */
public class ReminderPreferencesFragment extends PreferenceFragment {
    public static final String KEY_REMINDERS_ENABLED_BASE      = "reminders_enabled_";
    public static final String KEY_REMINDERS_SOUND_BASE        = "reminders_sound_";
    public static final String KEY_REMINDERS_VIBRATION_BASE    = "reminders_vibration_";
    public static final String KEY_REMINDERS_TIMETOREMIND_BASE = "reminders_timeToRemind_";

    public static final String URI_DEFAULT_NOTIFICATION_SOUND = "content://settings/system/notification_sound";

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
        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

        String soundKey = KEY_REMINDERS_SOUND_BASE + prayerTimeName;

        RingtonePreference sound = (RingtonePreference) findPreference(soundKey);
        sound.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
                updateSoundSummary(preference, (String) newValue);

                return true;
            }
        });

        String currentSound = sharedPreferences.getString(soundKey, URI_DEFAULT_NOTIFICATION_SOUND);
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
