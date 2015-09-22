package com.mehmetakiftutuncu.muezzin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.WithToolbar;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.StringUtils;

public class PrayerTimesActivity extends AppCompatActivity implements WithToolbar {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayertimes);

        initializeToolbar();

        initialize();
    }

    @Override
    public void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    private void initialize() {
        String currentLocation = Pref.CurrentLocation.get();

        if (!StringUtils.isEmpty(currentLocation)) {
            // Continue here by picking today and updating layout!

            /*String[] split = currentLocation.split("\\.");

            int countryId      = Integer.parseInt(split[0]);
            int cityId         = Integer.parseInt(split[1]);
            Integer districtId = split[2].equals("None") ? null : Integer.parseInt(split[2]);

            Data.loadPrayerTimes(countryId, cityId, districtId);*/
        } else {
            finish();
            startActivity(new Intent(this, WelcomeActivity.class));
        }
    }
}
