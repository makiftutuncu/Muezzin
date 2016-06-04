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

import com.kennyc.view.MultiStateView;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.MuezzinActivity;
import com.mehmetakiftutuncu.muezzin.adapters.DistrictsAdapter;
import com.mehmetakiftutuncu.muezzin.interfaces.OnDistrictSelectedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnDistrictsDownloadedListener;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
public class DistrictSelectionFragment extends StatefulFragment implements OnDistrictsDownloadedListener {
    private RecyclerView recyclerViewDistrictSelection;

    private Context context;
    private MuezzinActivity muezzinActivity;
    private OnDistrictSelectedListener onDistrictSelectedListener;

    private int cityId;

    private ArrayList<District> districts;

    public DistrictSelectionFragment() {}

    public static DistrictSelectionFragment with(int cityId, OnDistrictSelectedListener onDistrictSelectedListener) {
        DistrictSelectionFragment districtSelectionFragment = new DistrictSelectionFragment();
        Bundle arguments = new Bundle();

        arguments.putInt("cityId", cityId);
        districtSelectionFragment.setArguments(arguments);
        districtSelectionFragment.setOnDistrictSelectedListener(onDistrictSelectedListener);

        return districtSelectionFragment;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        cityId = getArguments().getInt("cityId");

        if (savedInstanceState != null) {
            ArrayList<String> districtJsons = savedInstanceState.getStringArrayList("districts");
            districts = new ArrayList<>();

            for (int i = 0, size = districtJsons != null ? districtJsons.size() : 0; i < size; i++) {
                districts.add(District.fromJson(cityId, districtJsons.get(i)).get());
            }
        }

        setRetainInstance(true);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        if (districts != null) {
            ArrayList<String> districtJsons = new ArrayList<>();
            for (int i = 0, size = districts.size(); i < size; i++) {
                districtJsons.add(districts.get(i).toJson());
            }

            outState.putStringArrayList("districts", districtJsons);
        }

        outState.putParcelable("recyclerViewDistrictSelection", recyclerViewDistrictSelection.getLayoutManager().onSaveInstanceState());

        super.onSaveInstanceState(outState);
    }

    @Override public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            recyclerViewDistrictSelection.getLayoutManager().onRestoreInstanceState(savedInstanceState.getParcelable("recyclerViewDistrictSelection"));
        }
    }

    @Override public void onStart() {
        super.onStart();

        if (districts == null) {
            loadDistricts();
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
        View layout = inflater.inflate(R.layout.fragment_districtselection, container, false);

        multiStateViewLayout          = (MultiStateView) layout.findViewById(R.id.multiStateView_districtSelection);
        recyclerViewDistrictSelection = (RecyclerView) layout.findViewById(R.id.recyclerView_districtSelection);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerViewDistrictSelection.setLayoutManager(linearLayoutManager);

        return layout;
    }

    @Override public void onDistrictsDownloaded(@NonNull ArrayList<District> districts) {
        this.districts = districts;

        if (districts.isEmpty()) {
            Log.debug(getClass(), "No districts for city '%d' were found on the server!", cityId);

            onDistrictSelectedListener.onNoDistrictsFound();
        } else {
            if (!District.saveDistricts(context, cityId, districts)) {
                changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);

                return;
            }

            updateUI();
        }
    }

    @Override public void onDistrictsDownloadFailed() {
        changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
    }

    public void setOnDistrictSelectedListener(OnDistrictSelectedListener onDistrictSelectedListener) {
        this.onDistrictSelectedListener = onDistrictSelectedListener;
    }

    private void loadDistricts() {
        Optional<ArrayList<District>> maybeDistrictsFromDatabase = District.getDistricts(context, cityId);

        if (maybeDistrictsFromDatabase.isEmpty) {
            changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
        } else {
            districts = maybeDistrictsFromDatabase.get();

            if (districts.isEmpty()) {
                Log.debug(getClass(), "No districts for city '%d' were found on database!", cityId);

                MuezzinAPIClient.getDistricts(cityId, this);
            } else {
                Log.debug(getClass(), "Loaded districts for city '%d' from database!", cityId);

                updateUI();
            }
        }
    }

    private void updateUI() {
        changeStateTo(MultiStateView.VIEW_STATE_CONTENT, 0);

        DistrictsAdapter districtsAdapter = new DistrictsAdapter(districts, onDistrictSelectedListener);
        recyclerViewDistrictSelection.setAdapter(districtsAdapter);

        if (muezzinActivity != null) {
            muezzinActivity.setTitle(R.string.placeSelection_district);
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
                MuezzinAPIClient.getDistricts(cityId, this);
                break;
        }
    }
}
