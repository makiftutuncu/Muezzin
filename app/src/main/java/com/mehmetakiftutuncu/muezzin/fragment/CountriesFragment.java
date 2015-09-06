package com.mehmetakiftutuncu.muezzin.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mehmetakiftutuncu.indexedrecyclerview.IndexedRecyclerView;
import com.mehmetakiftutuncu.indexedrecyclerview.IndexedRecyclerViewDecoration;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.CountriesAdapter;
import com.mehmetakiftutuncu.muezzin.interfaces.WithContentStates;
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

import ru.vang.progressswitcher.ProgressWidget;

public class CountriesFragment extends Fragment implements WithContentStates, Web.OnRequestFailure, Web.OnResponse {
    private ProgressWidget progressWidget;
    private IndexedRecyclerView recyclerView;
    private ContentStates state;

    public static CountriesFragment newInstance() {
        return new CountriesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_countries, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        progressWidget = (ProgressWidget) layout.findViewById(R.id.progressWidget_countries);

        recyclerView = (IndexedRecyclerView) layout.findViewById(R.id.recyclerView_countries);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        IndexedRecyclerViewDecoration decoration = new IndexedRecyclerViewDecoration();
        recyclerView.addItemDecoration(decoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadCountries();
    }

    @Override
    public void changeStateTo(ContentStates newState) {
        if (state == null || !state.equals(newState)) {
            state = newState;

            switch (newState) {
                case LOADING:
                    progressWidget.showProgress(true);
                    break;
                case ERROR:
                    progressWidget.showError(true);
                    break;
                case CONTENT:
                    progressWidget.showContent(true);
                    break;
                case NO_CONTENT:
                    progressWidget.showEmpty(true);
                    break;
            }
        }
    }

    public void setCountries(List<Country> countries) {
        if (countries == null || countries.isEmpty()) {
            Log.error(this, "Failed to set countries, countries object is empty!");

            changeStateTo(ContentStates.ERROR);
        } else {
            Data.saveCountries(countries);
            CountriesAdapter countriesAdapter = new CountriesAdapter(countries);
            recyclerView.setAdapter(countriesAdapter);

            changeStateTo(ContentStates.CONTENT);
        }
    }

    private void loadCountries() {
        changeStateTo(ContentStates.LOADING);

        List<Country> countriesFromDisk = Data.loadCountries();

        if (countriesFromDisk != null && !countriesFromDisk.isEmpty()) {
            setCountries(countriesFromDisk);
        } else {
            if (!Web.hasInternet(getContext())) {
                Log.error(this, "Failed to load countries, there is no internet connection!");

                changeStateTo(ContentStates.ERROR);
            } else {
                Web.instance().get(Conf.Url.countries, this, this);
            }
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.error(this, "Failed to get countries from Web!", e);
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
                        setCountries(countries);
                    }
                });
            } catch (IOException e) {
                Log.error(this, "Failed to process Web response to get countries!", e);
            } catch (JSONException e) {
                Log.error(this, "Failed to parse Web response to get countries!", e);
            }
        }
    }
}
