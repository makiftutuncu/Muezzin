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
import com.mehmetakiftutuncu.muezzin.interfaces.OnPrayerTimesDownloadedListener;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
public class PrayerTimesFragment extends Fragment implements OnPrayerTimesDownloadedListener {
    private TextView text;

    private Place place;
    private ArrayList<PrayerTimes> prayerTimes;

    public PrayerTimesFragment() {}

    public static PrayerTimesFragment with(Bundle bundle) {
        PrayerTimesFragment prayerTimesFragment = new PrayerTimesFragment();
        prayerTimesFragment.setArguments(bundle);

        return prayerTimesFragment;
    }

    @Override public void onStart() {
        super.onStart();

        loadPrayerTimes();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_prayertimes, container, false);

        text = (TextView) layout.findViewById(R.id.text);

        Optional<Place> maybePlace = Place.fromBundle(getArguments());

        if (maybePlace.isDefined) {
            place = maybePlace.get();
        }

        return layout;
    }

    @Override public void onPrayerTimesDownloaded(@NonNull ArrayList<PrayerTimes> prayerTimes) {
        Log.debug(getClass(), "Saving prayer times for place '%s' to database...", place);

        if (!PrayerTimes.savePrayerTimes(getContext(), place, prayerTimes)) {
            // Snackbar.make(recyclerViewCountrySelection, "Failed to save countries to database!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
        }

        this.prayerTimes = prayerTimes;

        updateUI();
    }

    @Override public void onPrayerTimesDownloadFailed() {
        Log.error(getClass(), "Failed to download prayer times for place '%s'!", place);
    }

    private void loadPrayerTimes() {
        Log.debug(getClass(), "Loading prayer times for place '%s'...", place);

        Optional<ArrayList<PrayerTimes>> maybePrayerTimesFromDatabase = PrayerTimes.getPrayerTimes(getContext(), place);

        if (maybePrayerTimesFromDatabase.isEmpty) {
            // Snackbar.make(recyclerViewCountrySelection, "Failed to load countries from database!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
        } else {
            ArrayList<PrayerTimes> prayerTimesFromDatabase = maybePrayerTimesFromDatabase.get();

            if (prayerTimesFromDatabase.isEmpty()) {
                Log.debug(getClass(), "No prayer times for place '%s' were found on database! Downloading...", place);

                MuezzinAPIClient.getPrayerTimes(place, this);
            } else {
                Log.debug(getClass(), "Loaded prayer times for place '%s' from database!", place);

                this.prayerTimes = prayerTimesFromDatabase;
                updateUI();
            }
        }
    }

    private void updateUI() {
        StringBuilder builder = new StringBuilder();

        for (PrayerTimes p : prayerTimes) {
            builder.append(p.toString()).append("\n");
        }

        text.setText(builder.toString());
    }
}
