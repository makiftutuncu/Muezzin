package com.mehmetakiftutuncu.muezzin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragments.CitySelectionFragment;
import com.mehmetakiftutuncu.muezzin.fragments.CountrySelectionFragment;
import com.mehmetakiftutuncu.muezzin.fragments.DistrictSelectionFragment;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCitySelectedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCountrySelectedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnDistrictSelectedListener;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

public class PlaceSelectionActivity extends AppCompatActivity implements OnCountrySelectedListener, OnCitySelectedListener, OnDistrictSelectedListener {
    public static final String EXTRA_STARTED_FROM_PREFERENCES = "startedFromPreferences";

    private int countryId = 0;
    private int cityId = 0;
    private Optional<Integer> districtId = new None<>();

    private boolean startedFromPreferences;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeselection);

        Bundle extras = getIntent().getExtras();
        startedFromPreferences = extras != null && extras.getBoolean(EXTRA_STARTED_FROM_PREFERENCES, false);

        CountrySelectionFragment countrySelectionFragment = CountrySelectionFragment.with(this);

        replaceFragment(countrySelectionFragment, "CountrySelectionFragment", false);
    }

    @Override public void onCountrySelected(Country country) {
        countryId = country.id;

        CitySelectionFragment citySelectionFragment = CitySelectionFragment.with(countryId, this);
        citySelectionFragment.setOnCitySelectedListener(this);

        replaceFragment(citySelectionFragment, "CitySelectionFragment", true);
    }

    @Override public void onCitySelected(City city) {
        cityId = city.id;

        DistrictSelectionFragment districtSelectionFragment = DistrictSelectionFragment.with(cityId, this);
        districtSelectionFragment.setOnDistrictSelectedListener(this);

        replaceFragment(districtSelectionFragment, "DistrictSelectionFragment", true);
    }

    @Override public void onDistrictSelected(District district) {
        districtId = new Some<>(district.id);

        launchPrayerTimesActivity();
    }

    @Override public void onNoDistrictsFound() {
        launchPrayerTimesActivity();
    }

    @SuppressLint("CommitTransaction")
    private void replaceFragment(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout_placeSelectionContainer, fragment, tag);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(tag);
        }

        fragmentTransaction.commit();
    }

    private void launchPrayerTimesActivity() {
        Place place = new Place(countryId, cityId, districtId);

        Pref.Places.setCurrentPlace(this, place);
        Log.debug(getClass(), "Place '%s' is selected!", place);

        Intent intent = new Intent(this, PrayerTimesActivity.class);
        intent.putExtras(place.toBundle());

        finish();

        if (!startedFromPreferences) {
            startActivity(intent);
        }
    }
}
