package com.mehmetakiftutuncu.muezzin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mehmetakiftutuncu.interfaces.OnPrayerTimesDownloadedListener;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import java.util.ArrayList;

/**
 * Created by akif on 08/05/16.
 */
public class PrayerTimesFragment extends Fragment implements OnPrayerTimesDownloadedListener {
    private static final String EXTRA_COUNTRY_ID  = "countryId";
    private static final String EXTRA_CITY_ID     = "cityId";
    private static final String EXTRA_DISTRICT_ID = "districtId";

    private TextView text;

    private int countryId;
    private int cityId;
    private Optional<Integer> districtId;
    private ArrayList<PrayerTimes> prayerTimes;

    public PrayerTimesFragment() {}

    public static PrayerTimesFragment with(int countryId, int cityId, Optional<Integer> districtId) {
        PrayerTimesFragment prayerTimesFragment = new PrayerTimesFragment();
        Bundle arguments = new Bundle();

        arguments.putInt(EXTRA_COUNTRY_ID, countryId);
        arguments.putInt(EXTRA_CITY_ID, cityId);

        if (districtId.isDefined) {
            arguments.putInt(EXTRA_DISTRICT_ID, districtId.get());
        }

        prayerTimesFragment.setArguments(arguments);

        return prayerTimesFragment;
    }

    @Override public void onStart() {
        super.onStart();

        loadPrayerTimes();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_prayertimes, container, false);

        text = (TextView) layout.findViewById(R.id.text);

        Bundle arguments = getArguments();

        countryId  = arguments.getInt(EXTRA_COUNTRY_ID);
        cityId     = arguments.getInt(EXTRA_CITY_ID);
        districtId = arguments.containsKey(EXTRA_DISTRICT_ID) ? new Some<>(arguments.getInt(EXTRA_DISTRICT_ID)) : new None<Integer>();

        return layout;
    }

    @Override public void onPrayerTimesDownloaded(@NonNull ArrayList<PrayerTimes> prayerTimes) {
        Log.debug(getClass(), "Saving prayer times for country '%d', city '%d' and district '%s' to database...", countryId, cityId, districtId);

        if (!PrayerTimes.savePrayerTimes(getContext(), countryId, cityId, districtId, prayerTimes)) {
            // Snackbar.make(recyclerViewCountrySelection, "Failed to save countries to database!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
        }

        this.prayerTimes = prayerTimes;

        updateUI();
    }

    @Override public void onPrayerTimesDownloadFailed() {
        Log.error(getClass(), "Failed to download prayer times for country '%d', city '%d' and district '%s'!", countryId, cityId, districtId);
    }

    private void loadPrayerTimes() {
        Log.debug(getClass(), "Loading prayer times for country '%d', city '%d' and district '%s'...", countryId, cityId, districtId);

        Optional<ArrayList<PrayerTimes>> maybePrayerTimesFromDatabase = PrayerTimes.getPrayerTimes(getContext(), countryId, cityId, districtId);

        if (maybePrayerTimesFromDatabase.isEmpty) {
            // Snackbar.make(recyclerViewCountrySelection, "Failed to load countries from database!", Snackbar.LENGTH_INDEFINITE).setAction("OK", null).show();
        } else {
            ArrayList<PrayerTimes> prayerTimesFromDatabase = maybePrayerTimesFromDatabase.get();

            if (prayerTimesFromDatabase.isEmpty()) {
                Log.debug(getClass(), "No prayer times for country '%d', city '%d' and district '%s' were found on database! Downloading...", countryId, cityId, districtId);

                MuezzinAPIClient.getPrayerTimes(countryId, cityId, districtId, this);
            } else {
                Log.debug(getClass(), "Loaded prayer times for country '%d', city '%d' and district '%s' from database!", countryId, cityId, districtId);

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
