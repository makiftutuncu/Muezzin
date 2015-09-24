package com.mehmetakiftutuncu.muezzin.fragment;

import android.view.View;

import com.mehmetakiftutuncu.muezzin.adapters.CountriesAdapter;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCountrySelectedListener;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.utilities.Conf;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.Web;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class CountriesFragment extends LocationsFragment<Country> {
    private OnCountrySelectedListener onCountrySelectedListener;

    public static CountriesFragment newInstance(OnCountrySelectedListener onCountrySelectedListener) {
        CountriesFragment countriesFragment = new CountriesFragment();
        countriesFragment.setOnCountrySelectedListener(onCountrySelectedListener);

        return countriesFragment;
    }

    @Override
    public void setItems(ArrayList<Country> countries, boolean saveData) {
        items = countries;

        CountriesAdapter countriesAdapter = new CountriesAdapter(countries, this);
        recyclerView.setAdapter(countriesAdapter);

        if (items == null) {
            changeStateTo(ContentStates.ERROR);
        } else if (items.isEmpty()) {
            changeStateTo(ContentStates.NO_CONTENT);
        } else {
            if (saveData) {
                boolean successful = Country.saveAll(countries);

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
            Option<ArrayList<Country>> countriesFromDisk = Country.loadAll();

            if (countriesFromDisk.isDefined) {
                setItems(countriesFromDisk.get(), false);
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
            Log.error(this, "Failed to download countries, there is no internet connection!");

            changeStateTo(ContentStates.ERROR);
        } else {
            Web.instance().get(Conf.Url.countries(), this, this);
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.error(this, "Failed to get countries from Web!", e);

        changeStateTo(ContentStates.ERROR);
    }

    @Override
    public void onResponse(Response response) {
        if (!Web.isResponseSuccessfulAndJson(response)) {
            Log.error(this, "Failed to process Web response to get countries, response is not a Json response!");

            changeStateTo(ContentStates.ERROR);
        } else {
            try {
                String countriesJsonString = response.body().string();

                JSONObject countriesJson = new JSONObject(countriesJsonString);
                JSONArray countriesJsonArray = countriesJson.getJSONArray("countries");

                final Option<ArrayList<Country>> countries = Country.fromJsonArray(countriesJsonArray);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setItems(countries.get(), true);
                    }
                });
            } catch (IOException e) {
                Log.error(this, "Failed to process Web response to get countries!", e);

                changeStateTo(ContentStates.ERROR);
            } catch (JSONException e) {
                Log.error(this, "Failed to parse Web response to get countries!", e);

                changeStateTo(ContentStates.ERROR);
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
