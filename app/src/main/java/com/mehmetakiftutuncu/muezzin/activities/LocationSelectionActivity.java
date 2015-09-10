package com.mehmetakiftutuncu.muezzin.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragment.CitiesFragment;
import com.mehmetakiftutuncu.muezzin.fragment.CountriesFragment;
import com.mehmetakiftutuncu.muezzin.fragment.DistrictsFragment;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCitySelectedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCountrySelectedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnDistrictSelectedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.WithToolbar;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;

public class LocationSelectionActivity extends AppCompatActivity implements WithToolbar,
                                                                            OnCountrySelectedListener,
                                                                            OnCitySelectedListener,
                                                                            OnDistrictSelectedListener {
    private Toolbar toolbar;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selection);

        initializeToolbar();

        fragmentManager = getSupportFragmentManager();

        CountriesFragment countriesFragment = CountriesFragment.newInstance(this);
        fragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout_locationContainer, countriesFragment)
                .commit();
    }

    @Override
    public void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);
    }

    @Override
    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void onCountrySelected(Country country) {
        CitiesFragment citiesFragment = CitiesFragment.newInstance(country.id(), this);
        fragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout_locationContainer, citiesFragment)
                .addToBackStack("countryToCity")
                .commit();
    }

    @Override
    public void onCitySelected(City city, int countryId) {
        DistrictsFragment districtsFragment = DistrictsFragment.newInstance(countryId, city.id(), this);
        fragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout_locationContainer, districtsFragment)
                .addToBackStack("cityToDistrict")
                .commit();
    }

    @Override
    public void onDistrictSelected(District district, int countryId, int cityId) {

    }
}
