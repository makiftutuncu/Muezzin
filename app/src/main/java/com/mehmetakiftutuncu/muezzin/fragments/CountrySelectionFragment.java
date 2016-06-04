package com.mehmetakiftutuncu.muezzin.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.kennyc.view.MultiStateView;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.MuezzinActivity;
import com.mehmetakiftutuncu.muezzin.adapters.CountriesAdapter;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCountriesDownloadedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCountrySelectedListener;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
public class CountrySelectionFragment extends StatefulFragment implements OnCountriesDownloadedListener, FloatingSearchView.OnQueryChangeListener {
    private FloatingSearchView floatingSearchView;
    private RecyclerView recyclerViewCountrySelection;

    private Context context;
    private MuezzinActivity muezzinActivity;
    private OnCountrySelectedListener onCountrySelectedListener;

    private ArrayList<Country> countries;
    private CountriesAdapter countriesAdapter;

    public CountrySelectionFragment() {}

    public static CountrySelectionFragment with(OnCountrySelectedListener onCountrySelectedListener) {
        CountrySelectionFragment countrySelectionFragment = new CountrySelectionFragment();
        countrySelectionFragment.setOnCountrySelectedListener(onCountrySelectedListener);

        return countrySelectionFragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            ArrayList<String> countryJsons = savedInstanceState.getStringArrayList("countries");
            countries = new ArrayList<>();

            for (int i = 0, size = countryJsons != null ? countryJsons.size() : 0; i < size; i++) {
                countries.add(Country.fromJson(countryJsons.get(i)).get());
            }
        }

        setRetainInstance(true);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        if (countries != null) {
            ArrayList<String> countryJsons = new ArrayList<>();
            for (int i = 0, size = countries.size(); i < size; i++) {
                countryJsons.add(countries.get(i).toJson());
            }

            outState.putStringArrayList("countries", countryJsons);
        }

        outState.putParcelable("recyclerViewCountrySelection", recyclerViewCountrySelection.getLayoutManager().onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }

    @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            recyclerViewCountrySelection.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable("recyclerViewCountrySelection"));
        }
    }

    @Override public void onStart() {
        super.onStart();

        if (countries == null) {
            loadCountries();
        } else {
            updateUI();
        }
    }

    @Override public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.context    = context;
            muezzinActivity = (MuezzinActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must extend MuezzinActivity!");
        }
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_countryselection, container, false);

        multiStateViewLayout         = (MultiStateView) layout.findViewById(R.id.multiStateView_countrySelection);
        floatingSearchView           = (FloatingSearchView) layout.findViewById(R.id.floatingSearchView_countrySearch);
        recyclerViewCountrySelection = (RecyclerView) layout.findViewById(R.id.recyclerView_countrySelection);

        floatingSearchView.setOnQueryChangeListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerViewCountrySelection.setLayoutManager(linearLayoutManager);

        return layout;
    }

    @Override public void onCountriesDownloaded(@NonNull ArrayList<Country> countries) {
        this.countries = countries;

        if (!Country.saveCountries(context, countries)) {
            changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);

            return;
        }

        updateUI();
    }

    @Override public void onCountriesDownloadFailed() {
        changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
    }

    public void setOnCountrySelectedListener(OnCountrySelectedListener onCountrySelectedListener) {
        this.onCountrySelectedListener = onCountrySelectedListener;
    }

    private void loadCountries() {
        changeStateTo(MultiStateView.VIEW_STATE_LOADING, 0);

        Optional<ArrayList<Country>> maybeCountriesFromDatabase = Country.getCountries(context);

        if (maybeCountriesFromDatabase.isEmpty) {
            changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
        } else {
            countries = maybeCountriesFromDatabase.get();

            if (countries.isEmpty()) {
                Log.debug(getClass(), "No countries were found on database!");

                MuezzinAPIClient.getCountries(this);
            } else {
                Log.debug(getClass(), "Loaded countries from database!");

                updateUI();
            }
        }
    }

    private void updateUI() {
        if (countries.isEmpty()) {
            changeStateTo(MultiStateView.VIEW_STATE_EMPTY, RETRY_ACTION_DOWNLOAD);

            return;
        }

        changeStateTo(MultiStateView.VIEW_STATE_CONTENT, 0);

        countriesAdapter = new CountriesAdapter(countries, onCountrySelectedListener);
        recyclerViewCountrySelection.setAdapter(countriesAdapter);

        if (muezzinActivity != null) {
            muezzinActivity.setTitle(R.string.placeSelection_country);
        }
    }

    @Override protected void changeStateTo(int newState, final int retryAction) {
        if (multiStateViewLayout != null) {
            switch (newState) {
                case MultiStateView.VIEW_STATE_CONTENT:
                    multiStateViewLayout.setViewState(newState);
                    break;

                case MultiStateView.VIEW_STATE_LOADING:
                case MultiStateView.VIEW_STATE_EMPTY:
                case MultiStateView.VIEW_STATE_ERROR:
                    multiStateViewLayout.setViewState(newState);

                    if (muezzinActivity != null) {
                        muezzinActivity.setTitle(R.string.applicationName);
                    }

                    View layout = multiStateViewLayout.getView(newState);

                    if (layout != null) {
                        View fab = layout.findViewById(R.id.fab_retry);

                        if (fab != null) {
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override public void onClick(View v) {
                                    retry(retryAction);
                                }
                            });
                        }
                    }
                    break;
            }
        }
    }

    @Override protected void retry(int action) {
        switch (action) {
            case RETRY_ACTION_DOWNLOAD:
                changeStateTo(MultiStateView.VIEW_STATE_LOADING, 0);
                MuezzinAPIClient.getCountries(this);
                break;
        }
    }

    @Override public void onSearchTextChanged(String oldQuery, String newQuery) {
        Log.debug(getClass(), "Searching for country with query '%s'", newQuery);

        countriesAdapter.search(newQuery);
    }
}
