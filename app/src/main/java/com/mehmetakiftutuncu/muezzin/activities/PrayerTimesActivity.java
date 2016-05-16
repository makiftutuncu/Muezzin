package com.mehmetakiftutuncu.muezzin.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragments.PrayerTimesFragment;

public class PrayerTimesActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayertimes);

        PrayerTimesFragment prayerTimesFragment = PrayerTimesFragment.with(getIntent().getExtras());

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout_prayerTimesContainer, prayerTimesFragment, "PrayerTimesFragment")
                .commit();
    }
}
