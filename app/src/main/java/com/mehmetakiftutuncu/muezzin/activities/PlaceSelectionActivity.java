package com.mehmetakiftutuncu.muezzin.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.mehmetakiftutuncu.interfaces.OnCitySelectedListener;
import com.mehmetakiftutuncu.interfaces.OnCountrySelectedListener;
import com.mehmetakiftutuncu.interfaces.OnDistrictSelectedListener;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragments.CitySelectionFragment;
import com.mehmetakiftutuncu.muezzin.fragments.CountrySelectionFragment;
import com.mehmetakiftutuncu.muezzin.fragments.DistrictSelectionFragment;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

public class PlaceSelectionActivity extends AppCompatActivity implements OnCountrySelectedListener, OnCitySelectedListener, OnDistrictSelectedListener {
    private int countryId;
    private int cityId;
    private Optional<Integer> districtId;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeselection);

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

        Log.debug(getClass(), "Place selected for country '%d', city '%d' and district '%s'!", countryId, cityId, districtId);
    }

    private void replaceFragment(Fragment fragment, String tag, boolean addToBackStack) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frameLayout_placeSelectionContainer, fragment, tag);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(tag);
        }

        fragmentTransaction.commit();
    }
}
