package com.mehmetakiftutuncu.muezzin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragment.PrayerTimesFragment;
import com.mehmetakiftutuncu.muezzin.interfaces.WithToolbar;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.StringUtils;
import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.mehmetakiftutuncu.muezzin.utilities.option.Some;

public class PrayerTimesActivity extends AppCompatActivity implements WithToolbar {
    private Toolbar toolbar;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayertimes);

        initializeToolbar();

        fragmentManager = getSupportFragmentManager();

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
            String[] split = currentLocation.split("\\.");

            int countryId              = Integer.parseInt(split[0]);
            int cityId                 = Integer.parseInt(split[1]);
            Option<Integer> districtId = split[2].equals("None") ? new None<Integer>() : new Some<>(Integer.parseInt(split[2]));

            PrayerTimesFragment prayerTimesFragment = PrayerTimesFragment.newInstance(countryId, cityId, districtId);
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.frameLayout_prayerTimesContainer, prayerTimesFragment)
                    .commit();
        } else {
            finish();
            startActivity(new Intent(this, WelcomeActivity.class));
        }
    }
}
