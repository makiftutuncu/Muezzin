package com.mehmetakiftutuncu.muezzin.fragments.preferences;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.preferences.ReminderPreferencesActivity;
import com.mehmetakiftutuncu.muezzin.utilities.Conf;

/**
 * Created by akif on 08/05/16.
 */
public class PreferencesFragment extends PreferenceFragment {
    public static final String KEY_GENERAL_REMINDERS = "general_reminders";
    public static final String KEY_MORE_RATE         = "more_rate";
    public static final String KEY_MORE_FEEDBACK     = "more_feedback";
    public static final String KEY_MORE_VERSION      = "more_version";
    public static final String KEY_MORE_LICENSES     = "more_licenses";

    public static final String RATE_URI = "market://details?id=com.mehmetakiftutuncu.muezzin";

    public static final String FEEDBACK_CONTACT = "m.akif.tutuncu@gmail.com";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        initializePreferences();
    }

    private void initializePreferences() {
        Preference reminders = findPreference(KEY_GENERAL_REMINDERS);
        reminders.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), ReminderPreferencesActivity.class);
                startActivity(intent);

                return true;
            }
        });

        Preference rate = findPreference(KEY_MORE_RATE);
        rate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(RATE_URI));
                startActivity(Intent.createChooser(intent, getString(R.string.preferences_more_rate)));

                return true;
            }
        });

        Preference feedback = findPreference(KEY_MORE_FEEDBACK);
        feedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{FEEDBACK_CONTACT});
                intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.applicationName));
                startActivity(Intent.createChooser(intent, getString(R.string.preferences_more_feedback)));

                return true;
            }
        });

        Preference version = findPreference(KEY_MORE_VERSION);
        String versionName = Conf.getAppVersionName(getActivity());
        version.setSummary(versionName);

        Preference licenses = findPreference(KEY_MORE_LICENSES);
    }
}
