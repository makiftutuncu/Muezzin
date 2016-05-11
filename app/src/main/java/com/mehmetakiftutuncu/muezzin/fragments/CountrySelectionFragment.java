package com.mehmetakiftutuncu.muezzin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.CountriesAdapter;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
public class CountrySelectionFragment extends Fragment implements MuezzinAPIClient.OnCountriesDownloadedListener {
    private RecyclerView recyclerViewCountrySelection;

    private LinearLayoutManager linearLayoutManager;
    private CountriesAdapter countriesAdapter;

    public CountrySelectionFragment() {}

    @Override public void onStart() {
        super.onStart();

        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerViewCountrySelection.setLayoutManager(linearLayoutManager);

        loadCountries();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_countryselection, container, false);

        recyclerViewCountrySelection = (RecyclerView) layout.findViewById(R.id.recyclerView_countrySelection);

        return layout;
    }

    @Override public void onCountriesDownloaded(@NonNull ArrayList<Country> countries) {
        Log.debug(getClass(), "Saving countries to database...");

        if (!Country.saveCountries(getContext(), countries)) {
            Snackbar.make(recyclerViewCountrySelection, "Failed to save countries to database!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
        }

        setCountries(countries);
    }

    @Override public void onCountriesDownloadFailed() {
        Snackbar.make(recyclerViewCountrySelection, "Failed to download countries!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
    }

    private void loadCountries() {
        Log.debug(getClass(), "Loading countries from database...");

        Optional<ArrayList<Country>> maybeCountriesFromDatabase = Country.getCountries(getContext());

        if (maybeCountriesFromDatabase.isEmpty) {
            Snackbar.make(recyclerViewCountrySelection, "Failed to load countries from database!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
        } else {
            ArrayList<Country> countriesFromDatabase = maybeCountriesFromDatabase.get();

            if (countriesFromDatabase.isEmpty()) {
                Log.debug(getClass(), "No countries were found on database! Downloading...");

                MuezzinAPIClient.getCountries(this);
            } else {
                Log.debug(getClass(), "Loaded countries from database!");

                setCountries(countriesFromDatabase);
            }
        }
    }

    private void setCountries(@NonNull ArrayList<Country> countries) {
        countriesAdapter = new CountriesAdapter(countries);
        recyclerViewCountrySelection.setAdapter(countriesAdapter);
    }
}
