package com.mehmetakiftutuncu.muezzin.activities.preferences;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragments.preferences.ReminderPreferencesFragment;

public class ReminderPreferencesActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminderpreferences);

        ReminderPreferencesFragment reminderPreferencesFragment = new ReminderPreferencesFragment();

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout_reminderPreferencesContainer, reminderPreferencesFragment)
                .commit();
    }
}
