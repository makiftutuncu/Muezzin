package com.mehmetakiftutuncu.muezzin.fragment;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.mehmetakiftutuncu.muezzin.R;
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
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class CountriesFragment extends LocationsFragment<Country> {
    private static final String TAG = "CountriesFragment";

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
        Log.info(TAG, "Downloading countries...");

        if (!Web.hasInternet(getContext())) {
            Log.error(TAG, "Failed to download countries, there is no internet connection!");

            changeStateTo(ContentStates.ERROR);

            Snackbar.make(progressWidget, R.string.common_noInternet, Snackbar.LENGTH_LONG).show();
        } else {
            Web.instance().get(Conf.Url.countries(), this);
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.error(TAG, "Failed to get countries from Web!", e);

        changeStateTo(ContentStates.ERROR);
    }

    @Override
    public void onResponse(Response response) {
        if (!Web.isResponseSuccessfulAndJson(response)) {
            Log.error(TAG, "Failed to process Web response to get countries, response is not a Json response!");

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
            } catch (Throwable t) {
                Log.error(TAG, "Failed to parse Web response to get countries!", t);

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
