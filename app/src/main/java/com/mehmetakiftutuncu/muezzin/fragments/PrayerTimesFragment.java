package com.mehmetakiftutuncu.muezzin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import org.joda.time.DateTime;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
public class PrayerTimesFragment extends Fragment implements OnPrayerTimesDownloadedListener {
    private TextView text;

    private Place place;
    private PrayerTimes prayerTimes;

    public PrayerTimesFragment() {}

    public static PrayerTimesFragment with(Bundle bundle) {
        PrayerTimesFragment prayerTimesFragment = new PrayerTimesFragment();
        prayerTimesFragment.setArguments(bundle);

        return prayerTimesFragment;
    }

    @Override public void onStart() {
        super.onStart();

        loadTodaysPrayerTimes();
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

        if (!PrayerTimes.saveAllPrayerTimes(getContext(), place, prayerTimes)) {
            // Snackbar.make(recyclerViewCountrySelection, "Failed to save countries to database!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
        }

        this.prayerTimes = prayerTimes.get(0);

        updateUI();
    }

    @Override public void onPrayerTimesDownloadFailed() {
        Log.error(getClass(), "Failed to download prayer times for place '%s'!", place);
    }

    private void loadTodaysPrayerTimes() {
        Log.debug(getClass(), "Loading today's prayer times for place '%s'...", place);

        Optional<PrayerTimes> maybePrayerTimesFromDatabase = PrayerTimes.getPrayerTimesForToday(getContext(), place);

        if (maybePrayerTimesFromDatabase.isEmpty) {
            Log.debug(getClass(), "Today's prayer times for place '%s' wasn't found on database! Downloading...", place);

            MuezzinAPIClient.getPrayerTimes(place, this);
        } else {
            Log.debug(getClass(), "Loaded today's prayer times for place '%s' from database!", place);

            this.prayerTimes = maybePrayerTimesFromDatabase.get();
            updateUI();
        }
    }

    private void updateUI() {
        text.setText(prayerTimes.toString());

        Optional<String> maybePlaceName = place.getPlaceName(getContext());

        if (maybePlaceName.isDefined) {
            ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (supportActionBar != null) {
                supportActionBar.setTitle(maybePlaceName.get());
                supportActionBar.setSubtitle(DateTime.now().toString(PrayerTimes.fullDateFormat));
            }
        }
    }
}
