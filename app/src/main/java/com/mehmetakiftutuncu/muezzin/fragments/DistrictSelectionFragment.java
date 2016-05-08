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
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
@SuppressLint("DefaultLocale")
public class DistrictSelectionFragment extends Fragment implements MuezzinAPIClient.OnDistrictsDownloadedListener {
    private TextView text;

    public DistrictSelectionFragment() {}

    @Override public void onStart() {
        super.onStart();

        MuezzinAPIClient.getDistricts(574, this);
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_districtselection, container, false);

        text = (TextView) layout.findViewById(R.id.text);

        return layout;
    }

    @Override public void onDistrictsDownloaded(@NonNull ArrayList<District> districts) {
        StringBuilder builder = new StringBuilder();

        for (District d : districts) {
            builder.append(d.id).append(": ").append(d.name).append("\n");
        }

        text.setText(builder.toString());
    }

    @Override public void onDistrictsDownloadFailed() {
        Log.error(String.format("Failed to download districts for city '%d'!", 574), getClass(), "onDistrictsDownloadFailed");
    }
}
