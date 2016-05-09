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
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
@SuppressLint("DefaultLocale")
public class PrayerTimesFragment extends Fragment implements MuezzinAPIClient.OnPrayerTimesDownloadedListener {
    private TextView text;

    public PrayerTimesFragment() {}

    @Override public void onStart() {
        super.onStart();

        MuezzinAPIClient.getPrayerTimes(2, 574, new Some<>(9901), this);
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_prayertimes, container, false);

        text = (TextView) layout.findViewById(R.id.text);

        return layout;
    }

    @Override public void onPrayerTimesDownloaded(@NonNull ArrayList<PrayerTimes> prayerTimes) {
        StringBuilder builder = new StringBuilder();

        for (PrayerTimes p : prayerTimes) {
            builder.append(p.toString()).append("\n");
        }

        text.setText(builder.toString());
    }

    @Override public void onPrayerTimesDownloadFailed() {
        Log.error(getClass(), "Failed to download prayer times for country '%d', city '%d' and district '%s'!", 2, 574, 9901);
    }
}
