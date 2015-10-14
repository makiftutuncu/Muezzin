package com.mehmetakiftutuncu.muezzin.fragment;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.CitiesAdapter;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCitySelectedListener;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.mehmetakiftutuncu.muezzin.utilities.Conf;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.Web;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class CitiesFragment extends LocationsFragment<City> {
    private static final String TAG = "CitiesFragment";

    private int countryId;
    private OnCitySelectedListener onCitySelectedListener;

    public static CitiesFragment newInstance(int countryId, OnCitySelectedListener onCitySelectedListener) {
        CitiesFragment citiesFragment = new CitiesFragment();
        citiesFragment.setCountryId(countryId);
        citiesFragment.setOnCitySelectedListener(onCitySelectedListener);

        return citiesFragment;
    }

    @Override
    public void setItems(ArrayList<City> cities, boolean saveData) {
        items = cities;

        CitiesAdapter citiesAdapter = new CitiesAdapter(cities, this);
        recyclerView.setAdapter(citiesAdapter);

        if (items == null) {
            changeStateTo(ContentStates.ERROR);
        } else if (items.isEmpty()) {
            changeStateTo(ContentStates.NO_CONTENT);
        } else {
            if (saveData) {
                boolean successful = City.saveAll(cities, countryId);

                if (!successful) {
                    changeStateTo(ContentStates.ERROR);
                } else {
                    changeStateTo(ContentStates.CONTENT);
                }
            } else {
                changeStateTo(ContentStates.CONTENT);
            }
        }
    }

    @Override
    public void loadItems(boolean forceDownload) {
        changeStateTo(ContentStates.LOADING);

        if (!forceDownload) {
            Option<ArrayList<City>> citiesFromDisk = City.loadAll(countryId);

            if (citiesFromDisk.isDefined) {
                setItems(citiesFromDisk.get(), false);
            } else {
                downloadItems();
            }
        } else {
            downloadItems();
        }
    }

    @Override
    public void downloadItems() {
        Log.info(TAG, "Downloading cities for country " + countryId + "...");

        if (!Web.hasInternet(getContext())) {
            Log.error(TAG, "Failed to download cities for country " + countryId + ", there is no internet connection!");

            changeStateTo(ContentStates.ERROR);

            Snackbar.make(progressWidget, R.string.common_noInternet, Snackbar.LENGTH_LONG).show();
        } else {
            Web.instance().get(Conf.Url.cities(countryId), this);
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.error(TAG, "Failed to get cities for country " + countryId + " from Web!", e);

        changeStateTo(ContentStates.ERROR);
    }

    @Override
    public void onResponse(Response response) {
        if (!Web.isResponseSuccessfulAndJson(response)) {
            Log.error(TAG, "Failed to process Web response to get cities for country " + countryId + ", response is not a Json response!");

            changeStateTo(ContentStates.ERROR);
        } else {
            try {
                String citiesJsonString = response.body().string();

                JSONObject citiesJson = new JSONObject(citiesJsonString);
                JSONArray citiesJsonArray = citiesJson.getJSONArray("cities");

                final Option<ArrayList<City>> cities = City.fromJsonArray(citiesJsonArray);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setItems(cities.get(), true);
                    }
                });
            } catch (Throwable t) {
                Log.error(TAG, "Failed to parse Web response to get cities for country " + countryId + "!", t);

                changeStateTo(ContentStates.ERROR);
            }
        }
    }

    @Override
    public void onItemClicked(View itemLayout, int position) {
        City city = items.get(position);

        onCitySelectedListener.onCitySelected(city, countryId);
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public void setOnCitySelectedListener(OnCitySelectedListener onCitySelectedListener) {
        this.onCitySelectedListener = onCitySelectedListener;
    }
}
