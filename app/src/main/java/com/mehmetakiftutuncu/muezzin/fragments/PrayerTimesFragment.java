package com.mehmetakiftutuncu.muezzin.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by akif on 08/05/16.
 */
public class PrayerTimesFragment extends Fragment implements OnPrayerTimesDownloadedListener {
    private TextView textViewRemainingTimeInfo;
    private TextView textViewRemainingTime;
    private TextView textViewFajr;
    private TextView textViewDhuhr;
    private TextView textViewAsr;
    private TextView textViewMaghrib;
    private TextView textViewIsha;
    private TextView textViewShuruq;
    private TextView textViewQibla;

    private Place place;
    private PrayerTimes prayerTimes;

    private Timer timer;
    private TimerTask timerTask;

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

    @Override public void onResume() {
        super.onResume();

        scheduleRemainingTimeCounter();
    }

    @Override public void onPause() {
        super.onPause();

        cancelRemainingTimeCounter();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_prayertimes, container, false);

        textViewRemainingTimeInfo = (TextView) layout.findViewById(R.id.textView_prayerTimes_remainingTimeInfo);
        textViewRemainingTime     = (TextView) layout.findViewById(R.id.textView_prayerTimes_remainingTime);
        textViewFajr              = (TextView) layout.findViewById(R.id.textView_prayerTimes_fajrTime);
        textViewShuruq            = (TextView) layout.findViewById(R.id.textView_prayerTimes_shuruqTime);
        textViewDhuhr             = (TextView) layout.findViewById(R.id.textView_prayerTimes_dhuhrTime);
        textViewAsr               = (TextView) layout.findViewById(R.id.textView_prayerTimes_asrTime);
        textViewMaghrib           = (TextView) layout.findViewById(R.id.textView_prayerTimes_maghribTime);
        textViewIsha              = (TextView) layout.findViewById(R.id.textView_prayerTimes_ishaTime);
        textViewQibla             = (TextView) layout.findViewById(R.id.textView_prayerTimes_qiblaTime);

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

        initializeUI();
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
            initializeUI();
        }
    }

    private void initializeUI() {
        Optional<String> maybePlaceName = place.getPlaceName(getContext());

        if (maybePlaceName.isDefined) {
            ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (supportActionBar != null) {
                supportActionBar.setTitle(maybePlaceName.get());
                supportActionBar.setSubtitle(DateTime.now().withZoneRetainFields(DateTimeZone.UTC).toString(PrayerTimes.dateFormat));
            }
        }

        textViewFajr.setText(prayerTimes.fajr.toString(PrayerTimes.timeFormat));
        textViewDhuhr.setText(prayerTimes.dhuhr.toString(PrayerTimes.timeFormat));
        textViewAsr.setText(prayerTimes.asr.toString(PrayerTimes.timeFormat));
        textViewMaghrib.setText(prayerTimes.maghrib.toString(PrayerTimes.timeFormat));
        textViewIsha.setText(prayerTimes.isha.toString(PrayerTimes.timeFormat));
        textViewShuruq.setText(prayerTimes.shuruq.toString(PrayerTimes.timeFormat));
        textViewQibla.setText(prayerTimes.qibla.toString(PrayerTimes.timeFormat));
    }

    private void updateRemainingTime() {
        if (prayerTimes != null) {
            DateTime now              = DateTime.now().withZoneRetainFields(DateTimeZone.UTC);
            DateTime nextPrayerTime   = prayerTimes.nextPrayerTime();
            String nextPrayerTimeName = prayerTimes.nextPrayerTimeName(getContext());

            DateTime remaining   = nextPrayerTime.minus(now.getMillis());
            String remainingTime = remaining.toString(PrayerTimes.remainingTimeFormat);

            textViewRemainingTimeInfo.setText(getString(R.string.prayerTimes_cardTitle_remainingTime, nextPrayerTimeName));
            textViewRemainingTime.setText(remainingTime);
        }
    }

    private void scheduleRemainingTimeCounter() {
        timer = new Timer();

        timerTask = new TimerTask() {
            @Override public void run() {
                FragmentActivity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override public void run() {
                            updateRemainingTime();
                        }
                    });
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private void cancelRemainingTimeCounter() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        if (timerTask != null && timerTask.scheduledExecutionTime() > 0) {
            timerTask.cancel();
        }
    }
}
