package com.mehmetakiftutuncu.muezzin.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
@SuppressLint("DefaultLocale")
public class CitySelectionFragment extends Fragment implements MuezzinAPIClient.OnCitiesDownloadedListener {
    private TextView text;

    public CitySelectionFragment() {}

    @Override public void onStart() {
        super.onStart();

        MuezzinAPIClient.getCities(2, this);
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_cityselection, container, false);

        text = (TextView) layout.findViewById(R.id.text);

        return layout;
    }

    @Override public void onCitiesDownloaded(@NonNull ArrayList<City> cities) {
        StringBuilder builder = new StringBuilder();

        for (City c : cities) {
            builder.append(c.id).append(": ").append(c.name).append("\n");
        }

        text.setText(builder.toString());
    }

    @Override public void onCitiesDownloadFailed() {
        Log.error(getClass(), "Failed to download cities for country '%d'!", 2);
    }
}
