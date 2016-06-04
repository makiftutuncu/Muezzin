package com.mehmetakiftutuncu.muezzin.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kennyc.view.MultiStateView;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.MuezzinActivity;
import com.mehmetakiftutuncu.muezzin.interfaces.OnPrayerTimesDownloadedListener;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by akif on 08/05/16.
 */
public class PrayerTimesFragment extends StatefulFragment implements OnPrayerTimesDownloadedListener {
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

    private Context context;
    private MuezzinActivity muezzinActivity;

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

    @Override public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.context    = context;
            muezzinActivity = (MuezzinActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context + " must extend MuezzinActivity!");
        }
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_prayertimes, container, false);

        multiStateViewLayout      = (MultiStateView) layout.findViewById(R.id.multiStateView_prayerTimes);
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
        if (!PrayerTimes.saveAllPrayerTimes(context, place, prayerTimes)) {
            changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
            return;
        }

        DateTime today = DateTime.now().withTimeAtStartOfDay().withZoneRetainFields(DateTimeZone.UTC);
        Optional<PrayerTimes> maybeTodaysPrayerTimes = new None<>();

        for (int i = 0, size = prayerTimes.size(); i < size; i++) {
            if (prayerTimes.get(i).day.getMillis() == today.getMillis()) {
                maybeTodaysPrayerTimes = new Some<>(prayerTimes.get(i));
                break;
            }
        }

        if (maybeTodaysPrayerTimes.isEmpty) {
            Log.error(getClass(), "Did not find today's prayer times in downloaded prayer times!");

            changeStateTo(MultiStateView.VIEW_STATE_EMPTY, RETRY_ACTION_DOWNLOAD);
        } else {
            this.prayerTimes = maybeTodaysPrayerTimes.get();

            initializeUI();
        }
    }

    @Override public void onPrayerTimesDownloadFailed() {
        Log.error(getClass(), "Failed to download prayer times for place '%s'!", place);
        changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
    }

    private void loadTodaysPrayerTimes() {
        changeStateTo(MultiStateView.VIEW_STATE_LOADING, 0);

        Optional<PrayerTimes> maybePrayerTimesFromDatabase = PrayerTimes.getPrayerTimesForToday(context, place);

        if (maybePrayerTimesFromDatabase.isEmpty) {
            Log.debug(getClass(), "Today's prayer times for place '%s' wasn't found on database!", place);

            MuezzinAPIClient.getPrayerTimes(place, this);
        } else {
            Log.debug(getClass(), "Loaded today's prayer times for place '%s' from database!", place);

            this.prayerTimes = maybePrayerTimesFromDatabase.get();

            initializeUI();
        }
    }

    private void initializeUI() {
        changeStateTo(MultiStateView.VIEW_STATE_CONTENT, 0);

        Optional<String> maybePlaceName = place.getPlaceName(context);

        if (maybePlaceName.isDefined) {
            if (muezzinActivity != null) {
                muezzinActivity.setTitle(maybePlaceName.get());
                muezzinActivity.setSubtitle(DateTime.now().withZoneRetainFields(DateTimeZone.UTC).toString(PrayerTimes.dateFormat));
            }

            Optional<Place> maybeLastPlace = Pref.Places.getLastPlace(context);

            if (maybeLastPlace.isDefined && !maybeLastPlace.get().equals(place)) {
                PrayerTimeReminder.reschedulePrayerTimeReminders(context);
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
            String nextPrayerTimeName = PrayerTimes.prayerTimeLocalizedName(context, prayerTimes.nextPrayerTimeName());

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

    @Override protected void changeStateTo(int newState, final int retryAction) {
        if (multiStateViewLayout != null) {
            switch (newState) {
                case MultiStateView.VIEW_STATE_CONTENT:
                    multiStateViewLayout.setViewState(newState);
                    break;

                case MultiStateView.VIEW_STATE_LOADING:
                case MultiStateView.VIEW_STATE_EMPTY:
                case MultiStateView.VIEW_STATE_ERROR:
                    multiStateViewLayout.setViewState(newState);

                    if (muezzinActivity != null) {
                        muezzinActivity.setTitle(R.string.applicationName);
                        muezzinActivity.setSubtitle("");
                    }

                    View layout = multiStateViewLayout.getView(newState);

                    if (layout != null) {
                        View fab = layout.findViewById(R.id.fab_retry);

                        if (fab != null) {
                            fab.setOnClickListener(new View.OnClickListener() {
                                @Override public void onClick(View v) {
                                    retry(retryAction);
                                }
                            });
                        }
                    }
                    break;
            }
        }
    }

    @Override protected void retry(int action) {
        switch (action) {
            case RETRY_ACTION_DOWNLOAD:
                changeStateTo(MultiStateView.VIEW_STATE_LOADING, 0);
                MuezzinAPIClient.getPrayerTimes(place, this);
                break;
        }
    }
}
