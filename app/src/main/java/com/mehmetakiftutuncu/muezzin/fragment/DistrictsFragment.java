package com.mehmetakiftutuncu.muezzin.fragment;

import android.view.View;

import com.mehmetakiftutuncu.muezzin.adapters.DistrictsAdapter;
import com.mehmetakiftutuncu.muezzin.interfaces.OnDistrictSelectedListener;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.mehmetakiftutuncu.muezzin.models.District;
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

public class DistrictsFragment extends LocationsFragment<District> {
    private int countryId;
    private int cityId;
    private OnDistrictSelectedListener onDistrictSelectedListener;

    public static DistrictsFragment newInstance(int countryId, int cityId, OnDistrictSelectedListener onDistrictSelectedListener) {
        DistrictsFragment districtsFragment = new DistrictsFragment();
        districtsFragment.setCountryId(countryId);
        districtsFragment.setCityId(cityId);
        districtsFragment.setOnDistrictSelectedListener(onDistrictSelectedListener);

        return districtsFragment;
    }

    @Override
    public void setItems(ArrayList<District> districts, boolean saveData) {
        items = districts;

        DistrictsAdapter districtsAdapter = new DistrictsAdapter(districts, this);
        recyclerView.setAdapter(districtsAdapter);

        if (items == null) {
            changeStateTo(ContentStates.ERROR);
        } else if (items.isEmpty()) {
            changeStateTo(ContentStates.NO_CONTENT);
        } else {
            if (saveData) {
                boolean successful = District.saveAll(districts, countryId, cityId);

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
            Option<ArrayList<District>> districtsFromDisk = District.loadAll(countryId, cityId);

            if (districtsFromDisk.isDefined) {
                setItems(districtsFromDisk.get(), false);
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
            Log.error(this, "Failed to download districts for country " + countryId + " and city " + cityId + ", there is no internet connection!");

            changeStateTo(ContentStates.ERROR);
        } else {
            Web.instance().get(Conf.Url.districts(cityId), this, this);
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.error(this, "Failed to get districts for country " + countryId + " and city " + cityId + " from Web!", e);

        changeStateTo(ContentStates.ERROR);
    }

    @Override
    public void onResponse(Response response) {
        if (!Web.isResponseSuccessfulAndJson(response)) {
            Log.error(this, "Failed to process Web response to get districts for country " + countryId + " and city " + cityId + ", response is not a Json response!");

            changeStateTo(ContentStates.ERROR);
        } else {
            try {
                String districtsJsonString = response.body().string();

                JSONObject districtsJson = new JSONObject(districtsJsonString);
                JSONArray districtsJsonArray = districtsJson.getJSONArray("districts");

                final Option<ArrayList<District>> districts = District.fromJsonArray(districtsJsonArray);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setItems(districts.get(), true);
                    }
                });
            } catch (IOException e) {
                Log.error(this, "Failed to process Web response to get districts for country " + countryId + " and city " + cityId + "!", e);

                changeStateTo(ContentStates.ERROR);
            } catch (JSONException e) {
                Log.error(this, "Failed to parse Web response to get districts for country " + countryId + " and city " + cityId + "!", e);

                changeStateTo(ContentStates.ERROR);
            }
        }
    }

    @Override
    public void onItemClicked(View itemLayout, int position) {
        District district = items.get(position);

        onDistrictSelectedListener.onDistrictSelected(district, countryId, cityId);
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public void setOnDistrictSelectedListener(OnDistrictSelectedListener onDistrictSelectedListener) {
        this.onDistrictSelectedListener = onDistrictSelectedListener;
    }
}
