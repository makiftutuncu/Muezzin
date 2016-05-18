package com.mehmetakiftutuncu.muezzin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.interfaces.OnDistrictSelectedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnDistrictsDownloadedListener;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.DistrictsAdapter;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
public class DistrictSelectionFragment extends Fragment implements OnDistrictsDownloadedListener {
    private RecyclerView recyclerViewDistrictSelection;

    private LinearLayoutManager linearLayoutManager;
    private DistrictsAdapter districtsAdapter;

    private OnDistrictSelectedListener onDistrictSelectedListener;

    private int cityId;

    public DistrictSelectionFragment() {}

    public static DistrictSelectionFragment with(int cityId, OnDistrictSelectedListener onDistrictSelectedListener) {
        DistrictSelectionFragment districtSelectionFragment = new DistrictSelectionFragment();
        Bundle arguments = new Bundle();

        arguments.putInt("cityId", cityId);
        districtSelectionFragment.setArguments(arguments);
        districtSelectionFragment.setOnDistrictSelectedListener(onDistrictSelectedListener);

        return districtSelectionFragment;
    }

    @Override public void onStart() {
        super.onStart();

        Bundle arguments = getArguments();

        cityId = arguments.getInt("cityId");

        linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerViewDistrictSelection.setLayoutManager(linearLayoutManager);

        loadDistricts();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_districtselection, container, false);

        recyclerViewDistrictSelection = (RecyclerView) layout.findViewById(R.id.recyclerView_districtSelection);

        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.placeSelection_district));
        }

        return layout;
    }

    @Override public void onDistrictsDownloaded(@NonNull ArrayList<District> districts) {
        if (districts.isEmpty()) {
            Log.debug(getClass(), "No districts for city '%d' were found on the server!", cityId);

            onDistrictSelectedListener.onNoDistrictsFound();
        } else {
            Log.debug(getClass(), "Saving districts for city '%d' to database...", cityId);

            if (!District.saveDistricts(getContext(), cityId, districts)) {
                Snackbar.make(recyclerViewDistrictSelection, "Failed to save districts to database!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
            }

            setDistricts(districts);
        }
    }

    @Override public void onDistrictsDownloadFailed() {
        Snackbar.make(recyclerViewDistrictSelection, "Failed to download districts!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
    }

    public void setOnDistrictSelectedListener(OnDistrictSelectedListener onDistrictSelectedListener) {
        this.onDistrictSelectedListener = onDistrictSelectedListener;
    }

    private void loadDistricts() {
        Log.debug(getClass(), "Loading districts for city '%d' from database...", cityId);

        Optional<ArrayList<District>> maybeDistrictsFromDatabase = District.getDistricts(getContext(), cityId);

        if (maybeDistrictsFromDatabase.isEmpty) {
            Snackbar.make(recyclerViewDistrictSelection, "Failed to load districts from database!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
        } else {
            ArrayList<District> districtsFromDatabase = maybeDistrictsFromDatabase.get();

            if (districtsFromDatabase.isEmpty()) {
                Log.debug(getClass(), "No districts for city '%d' were found on database! Downloading...", cityId);

                MuezzinAPIClient.getDistricts(cityId, this);
            } else {
                Log.debug(getClass(), "Loaded districts for city '%d' from database!", cityId);

                setDistricts(districtsFromDatabase);
            }
        }
    }

    private void setDistricts(@NonNull ArrayList<District> districts) {
        districtsAdapter = new DistrictsAdapter(districts, onDistrictSelectedListener);
        recyclerViewDistrictSelection.setAdapter(districtsAdapter);
    }
}
