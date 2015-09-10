package com.mehmetakiftutuncu.muezzin.fragment;

import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mehmetakiftutuncu.muezzin.adapters.CitiesAdapter;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCitySelectedListener;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.mehmetakiftutuncu.muezzin.utilities.Conf;
import com.mehmetakiftutuncu.muezzin.utilities.Data;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.Web;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CitiesFragment extends LocationsFragment<City> {
    private int countryId;
    private OnCitySelectedListener onCitySelectedListener;

    public static CitiesFragment newInstance(int countryId, OnCitySelectedListener onCitySelectedListener) {
        CitiesFragment citiesFragment = new CitiesFragment();
        citiesFragment.setCountryId(countryId);
        citiesFragment.setOnCitySelectedListener(onCitySelectedListener);

        return citiesFragment;
    }

    @Override
    public void setItems(List<City> cities, boolean saveData) {
        items = cities;

        if (cities == null || cities.isEmpty()) {
            Log.error(this, "Failed to set cities for country " + countryId + ", cities object is empty!");

            changeStateTo(ContentStates.ERROR);
        } else {
            if (saveData) {
                Data.saveCities(cities, countryId);
            }

            CitiesAdapter citiesAdapter = new CitiesAdapter(cities, this);
            recyclerView.setAdapter(citiesAdapter);

            changeStateTo(ContentStates.CONTENT);
        }
    }

    @Override
    public void loadItems(boolean forceDownload) {
        changeStateTo(ContentStates.LOADING);

        if (!forceDownload) {
            List<City> citiesFromDisk = Data.loadCities(countryId);

            if (citiesFromDisk != null && !citiesFromDisk.isEmpty()) {
                setItems(citiesFromDisk, false);
            } else {
                downloadItems();
            }
        } else {
            downloadItems();
        }
    }

    @Override
    public void downloadItems() {
        if (!Web.hasInternet(getContext())) {
            Log.error(this, "Failed to load cities for country " + countryId + ", there is no internet connection!");

            changeStateTo(ContentStates.ERROR);
        } else {
            Web.instance().get(Conf.Url.cities(countryId), this, this);
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.error(this, "Failed to get cities for country " + countryId + " from Web!", e);

        setItems(null, false);
    }

    @Override
    public void onResponse(Response response) {
        if (!Web.isResponseSuccessfulAndJson(response)) {
            Log.error(this, "Failed to process Web response to get cities for country " + countryId + ", response is not a Json response!");
        } else {
            try {
                String citiesJsonString = response.body().string();

                JSONObject citiesJson = new JSONObject(citiesJsonString);
                JSONArray citiesJsonArray = citiesJson.getJSONArray("cities");

                Gson gson = new Gson();
                final List<City> cities = gson.fromJson(citiesJsonArray.toString(), new TypeToken<ArrayList<City>>() {
                }.getType());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setItems(cities, true);
                    }
                });
            } catch (IOException e) {
                Log.error(this, "Failed to process Web response to get cities for country " + countryId + "!", e);
            } catch (JSONException e) {
                Log.error(this, "Failed to parse Web response to get cities for country " + countryId + "!", e);
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
