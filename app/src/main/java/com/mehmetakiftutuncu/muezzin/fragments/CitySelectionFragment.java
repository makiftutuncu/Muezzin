package com.mehmetakiftutuncu.muezzin.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.github.mehmetakiftutuncu.toolbelt.Log;
import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.kennyc.view.MultiStateView;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.MuezzinActivity;
import com.mehmetakiftutuncu.muezzin.adapters.CitiesAdapter;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPI;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akif on 08/05/16.
 */
public class CitySelectionFragment extends StatefulFragment implements FloatingSearchView.OnQueryChangeListener, MuezzinAPI.OnCitiesDownloadedListener {
    private FloatingSearchView floatingSearchView;
    private RecyclerView recyclerViewCitySelection;

    private Context context;
    private MuezzinActivity muezzinActivity;
    private OnCitySelectedListener onCitySelectedListener;

    private int countryId;

    private List<City> cities;
    private CitiesAdapter citiesAdapter;

    public interface OnCitySelectedListener {
        void onCitySelected(City city);
    }

    public CitySelectionFragment() {}

    public static CitySelectionFragment with(int countryId, OnCitySelectedListener onCitySelectedListener) {
        CitySelectionFragment citySelectionFragment = new CitySelectionFragment();
        Bundle arguments = new Bundle();

        arguments.putInt("countryId", countryId);
        citySelectionFragment.setArguments(arguments);
        citySelectionFragment.setOnCitySelectedListener(onCitySelectedListener);

        return citySelectionFragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        countryId = getArguments().getInt("countryId");

        if (savedInstanceState != null) {
            ArrayList<City> cityParcelables = savedInstanceState.getParcelableArrayList("cities");

            this.cities = cityParcelables != null ? cityParcelables : new ArrayList<>();
        }

        setRetainInstance(true);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("cities", new ArrayList<>(cities));
        outState.putParcelable("recyclerViewCitySelection", recyclerViewCitySelection.getLayoutManager().onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }

    @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            recyclerViewCitySelection.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable("recyclerViewCitySelection"));
        }
    }

    @Override public void onStart() {
        super.onStart();

        if (cities == null) {
            loadCities();
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
        View layout = inflater.inflate(R.layout.fragment_cityselection, container, false);

        multiStateViewLayout      = (MultiStateView) layout.findViewById(R.id.multiStateView_citySelection);
        floatingSearchView        = (FloatingSearchView) layout.findViewById(R.id.floatingSearchView_citySearch);
        recyclerViewCitySelection = (RecyclerView) layout.findViewById(R.id.recyclerView_citySelection);

        floatingSearchView.setOnQueryChangeListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerViewCitySelection.setLayoutManager(linearLayoutManager);

        return layout;
    }

    @Override public void onCitiesDownloaded(@NonNull List<City> cities) {
        this.cities = cities;

        if (!City.saveCities(context, countryId, cities)) {
            changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);

            return;
        }

        updateUI();
    }

    @Override public void onDownloadCitiesFailed(Exception e) {
        changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
    }

    public void setOnCitySelectedListener(OnCitySelectedListener onCitySelectedListener) {
        this.onCitySelectedListener = onCitySelectedListener;
    }

    private void loadCities() {
        Optional<List<City>> maybeCitiesFromDatabase = City.getCities(context, countryId);

        if (maybeCitiesFromDatabase.isEmpty()) {
            changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
        } else {
            cities = maybeCitiesFromDatabase.get();

            if (cities.isEmpty()) {
                Log.debug(getClass(), "No cities for country '%d' were found on database!", countryId);

                MuezzinAPI.get().getCities(context, countryId, this);
            } else {
                Log.debug(getClass(), "Loaded cities for country '%d' from database!", countryId);

                updateUI();
            }
        }
    }

    private void updateUI() {
        if (cities.isEmpty()) {
            changeStateTo(MultiStateView.VIEW_STATE_EMPTY, RETRY_ACTION_DOWNLOAD);

            return;
        }

        changeStateTo(MultiStateView.VIEW_STATE_CONTENT, 0);

        citiesAdapter = new CitiesAdapter(cities, onCitySelectedListener);
        recyclerViewCitySelection.setAdapter(citiesAdapter);

        if (muezzinActivity != null) {
            muezzinActivity.setTitle(R.string.placeSelection_city);
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
                            fab.setOnClickListener(v -> retry(retryAction));
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
                MuezzinAPI.get().getCities(context, countryId, this);
                break;
        }
    }

    @Override public void onSearchTextChanged(String oldQuery, String newQuery) {
        Log.debug(getClass(), "Searching for city with query '%s'", newQuery);

        citiesAdapter.search(newQuery);
    }
}
