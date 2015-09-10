package com.mehmetakiftutuncu.muezzin.fragment;

import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mehmetakiftutuncu.muezzin.adapters.CountriesAdapter;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCountrySelectedListener;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.mehmetakiftutuncu.muezzin.models.Country;
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

public class CountriesFragment extends LocationsFragment<Country> {
    private OnCountrySelectedListener onCountrySelectedListener;

    public static CountriesFragment newInstance(OnCountrySelectedListener onCountrySelectedListener) {
        CountriesFragment countriesFragment = new CountriesFragment();
        countriesFragment.setOnCountrySelectedListener(onCountrySelectedListener);

        return countriesFragment;
    }

    @Override
    public void setItems(List<Country> countries, boolean saveData) {
        items = countries;

        if (countries == null || countries.isEmpty()) {
            Log.error(this, "Failed to set countries, countries object is empty!");

            changeStateTo(ContentStates.ERROR);
        } else {
            if (saveData) {
                Data.saveCountries(countries);
            }

            CountriesAdapter countriesAdapter = new CountriesAdapter(countries, this);
            recyclerView.setAdapter(countriesAdapter);

            changeStateTo(ContentStates.CONTENT);
        }
    }

    @Override
    public void loadItems(boolean forceDownload) {
        changeStateTo(ContentStates.LOADING);

        if (!forceDownload) {
            List<Country> countriesFromDisk = Data.loadCountries();

            if (countriesFromDisk != null && !countriesFromDisk.isEmpty()) {
                setItems(countriesFromDisk, false);
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
            Log.error(this, "Failed to load countries, there is no internet connection!");

            changeStateTo(ContentStates.ERROR);
        } else {
            Web.instance().get(Conf.Url.countries, this, this);
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.error(this, "Failed to get countries from Web!", e);

        setItems(null, false);
    }

    @Override
    public void onResponse(Response response) {
        if (!Web.isResponseSuccessfulAndJson(response)) {
            Log.error(this, "Failed to process Web response to get countries, response is not a Json response!");
        } else {
            try {
                String countriesJsonString = response.body().string();

                JSONObject countriesJson = new JSONObject(countriesJsonString);
                JSONArray countriesJsonArray = countriesJson.getJSONArray("countries");

                Gson gson = new Gson();
                final List<Country> countries = gson.fromJson(countriesJsonArray.toString(), new TypeToken<ArrayList<Country>>(){}.getType());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setItems(countries, true);
                    }
                });
            } catch (IOException e) {
                Log.error(this, "Failed to process Web response to get countries!", e);
            } catch (JSONException e) {
                Log.error(this, "Failed to parse Web response to get countries!", e);
            }
        }
    }

    @Override
    public void onItemClicked(View itemLayout, int position) {
        Country country = items.get(position);

        onCountrySelectedListener.onCountrySelected(country);
    }

    public void setOnCountrySelectedListener(OnCountrySelectedListener onCountrySelectedListener) {
        this.onCountrySelectedListener = onCountrySelectedListener;
    }
}
