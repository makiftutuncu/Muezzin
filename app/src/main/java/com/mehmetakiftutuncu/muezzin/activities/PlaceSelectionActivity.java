package com.mehmetakiftutuncu.muezzin.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.github.mehmetakiftutuncu.toolbelt.Log;
import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragments.CitySelectionFragment;
import com.mehmetakiftutuncu.muezzin.fragments.CountrySelectionFragment;
import com.mehmetakiftutuncu.muezzin.fragments.DistrictSelectionFragment;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;

public class PlaceSelectionActivity extends MuezzinActivity implements CountrySelectionFragment.OnCountrySelectedListener, CitySelectionFragment.OnCitySelectedListener, DistrictSelectionFragment.OnDistrictSelectedListener {
    public static final String EXTRA_STARTED_FROM_PREFERENCES = "startedFromPreferences";

    private int countryId = 0;
    private int cityId = 0;
    private Optional<Integer> districtId = Optional.empty();

    private boolean startedFromPreferences;

    private CountrySelectionFragment countrySelectionFragment;
    private CitySelectionFragment citySelectionFragment;
    private DistrictSelectionFragment districtSelectionFragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeselection);

        FragmentManager supportFragmentManager = getSupportFragmentManager();

        if (savedInstanceState != null) {
            countryId  = savedInstanceState.getInt("countryId");
            cityId     = savedInstanceState.getInt("cityId");
            districtId = savedInstanceState.containsKey("districtId") ? Optional.with(savedInstanceState.getInt("districtId")) : Optional.<Integer>empty();

            startedFromPreferences = savedInstanceState.getBoolean("startedFromPreferences");

            countrySelectionFragment  = (CountrySelectionFragment)  supportFragmentManager.findFragmentByTag("CountrySelectionFragment");
            citySelectionFragment     = (CitySelectionFragment)     supportFragmentManager.findFragmentByTag("CitySelectionFragment");
            districtSelectionFragment = (DistrictSelectionFragment) supportFragmentManager.findFragmentByTag("DistrictSelectionFragment");
        }

        Bundle extras = getIntent().getExtras();
        startedFromPreferences = extras != null && extras.getBoolean(EXTRA_STARTED_FROM_PREFERENCES, false);

        if (countrySelectionFragment == null) {
            countrySelectionFragment = CountrySelectionFragment.with(this);
        } else {
            countrySelectionFragment.setOnCountrySelectedListener(this);
        }

        if (districtSelectionFragment != null) {
            districtSelectionFragment.setOnDistrictSelectedListener(this);

            replaceFragment(districtSelectionFragment, "DistrictSelectionFragment", false);
        } else if (citySelectionFragment != null) {
            citySelectionFragment.setOnCitySelectedListener(this);

            replaceFragment(citySelectionFragment, "CitySelectionFragment", false);
        } else {
            replaceFragment(countrySelectionFragment, "CountrySelectionFragment", false);
        }
    }

    @Override protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("countryId", countryId);
        outState.putInt("cityId", cityId);

        if (districtId.isDefined()) {
            outState.putInt("districtId", districtId.get());
        }

        outState.putBoolean("startedFromPreferences", startedFromPreferences);

        super.onSaveInstanceState(outState);
    }

    @Override public void onCountrySelected(Country country) {
        countryId = country.id;

        CitySelectionFragment citySelectionFragment = CitySelectionFragment.with(countryId, this);
        citySelectionFragment.setOnCitySelectedListener(this);

        replaceFragment(citySelectionFragment, "CitySelectionFragment", true);
    }

    @Override public void onCitySelected(City city) {
        cityId = city.id;

        DistrictSelectionFragment districtSelectionFragment = DistrictSelectionFragment.with(countryId, cityId, this);
        districtSelectionFragment.setOnDistrictSelectedListener(this);

        replaceFragment(districtSelectionFragment, "DistrictSelectionFragment", true);
    }

    @Override public void onDistrictSelected(District district) {
        districtId = Optional.with(district.id);

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

        fragmentTransaction.commitAllowingStateLoss();
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
