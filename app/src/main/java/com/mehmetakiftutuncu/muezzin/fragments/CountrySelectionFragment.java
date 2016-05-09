package com.mehmetakiftutuncu.muezzin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
public class CountrySelectionFragment extends Fragment implements MuezzinAPIClient.OnCountriesDownloadedListener {
    private TextView text;

    public CountrySelectionFragment() {}

    @Override public void onStart() {
        super.onStart();

        MuezzinAPIClient.getCountries(this);
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_countryselection, container, false);

        text = (TextView) layout.findViewById(R.id.text);

        return layout;
    }

    @Override public void onCountriesDownloaded(@NonNull ArrayList<Country> countries) {
        StringBuilder builder = new StringBuilder();

        for (Country c : countries) {
            builder.append(c.id).append(": ").append(c.nameEnglish).append(" (").append(c.nameNative).append(")\n");
        }

        text.setText(builder.toString());
    }

    @Override public void onCountriesDownloadFailed() {
        Log.error(getClass(), "Failed to download countries!");
    }
}
