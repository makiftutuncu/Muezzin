package com.mehmetakiftutuncu.muezzin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.preferences.PreferencesActivity;
import com.mehmetakiftutuncu.muezzin.fragments.NoPlacesFoundFragment;
import com.mehmetakiftutuncu.muezzin.fragments.PrayerTimesFragment;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.stephentuso.welcome.WelcomeScreenHelper;

public class PrayerTimesActivity extends MuezzinActivity {
    private WelcomeScreenHelper welcomeScreenHelper;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prayertimes);

        welcomeScreenHelper = new WelcomeScreenHelper(this, WelcomeActivity.class);
        welcomeScreenHelper.show(savedInstanceState);
    }

    @Override protected void onResume() {
        super.onResume();

        Optional<Place> maybeCurrentPlace = Pref.Places.getCurrentPlace(this);

        if (maybeCurrentPlace.isEmpty) {
            showNoPlacesFound();
        } else {
            Place currentPlace = maybeCurrentPlace.get();

            showPrayerTimes(currentPlace.toBundle());
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preferences:
                startActivity(new Intent(this, PreferencesActivity.class));

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        welcomeScreenHelper.onSaveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    private void showNoPlacesFound() {
        NoPlacesFoundFragment noPlacesFoundFragment = new NoPlacesFoundFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout_prayerTimesContainer, noPlacesFoundFragment, "NoPlacesFoundFragment")
                .commit();
    }

    private void showPrayerTimes(Bundle bundle) {
        PrayerTimesFragment prayerTimesFragment = PrayerTimesFragment.with(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout_prayerTimesContainer, prayerTimesFragment, "PrayerTimesFragment")
                .commit();
    }
}
