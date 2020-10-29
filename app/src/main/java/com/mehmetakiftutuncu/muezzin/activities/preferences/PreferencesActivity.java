package com.mehmetakiftutuncu.muezzin.activities.preferences;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragments.preferences.PreferencesFragment;

public class PreferencesActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        PreferencesFragment preferencesFragment = new PreferencesFragment();

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout_preferencesContainer, preferencesFragment)
                .commit();
    }
}
