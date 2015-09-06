package com.mehmetakiftutuncu.muezzin.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.fragment.CountriesFragment;
import com.mehmetakiftutuncu.muezzin.interfaces.WithToolbar;

public class LocationSelectionActivity extends AppCompatActivity implements WithToolbar {
    private Toolbar toolbar;
    private FrameLayout countriesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selection);

        initializeToolbar();

        countriesContainer = (FrameLayout) findViewById(R.id.frameLayout_countryListContainer);

        CountriesFragment countriesFragment = CountriesFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout_countryListContainer, countriesFragment)
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
}
