package com.mehmetakiftutuncu.muezzin.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mehmetakiftutuncu.toolbelt.Log;
import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.kennyc.view.MultiStateView;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.MuezzinActivity;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimesOfDay;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPI;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.RemainingTime;
import com.mehmetakiftutuncu.muezzin.widgetproviders.PrayerTimesWidgetBase;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.chrono.IslamicChronology;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by akif on 08/05/16.
 */
public class PrayerTimesFragment extends StatefulFragment implements MuezzinAPI.OnPrayerTimesDownloadedListener {
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
    private Optional<PrayerTimesOfDay> maybePrayerTimesOfDay;

    private Timer timer;
    private TimerTask timerTask;

    private Context context;
    private MuezzinActivity muezzinActivity;

    private int defaultTextColor;
    private int redTextColor;

    private static final String FULL_DATE_PATTERN = "dd MMMM YYYY";
    private static final DateTimeFormatter FULL_DATE_FORMATTTER = DateTimeFormat.forPattern(FULL_DATE_PATTERN);

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

            TypedValue typedValue = new TypedValue();
            muezzinActivity.getTheme().resolveAttribute(android.R.attr.textColorSecondary, typedValue, true);
            TypedArray typedArray = getActivity().obtainStyledAttributes(typedValue.data, new int[] {android.R.attr.textColorSecondary});
            defaultTextColor = typedArray.getColor(0, -1);
            typedArray.recycle();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                redTextColor = getResources().getColor(R.color.red);
            } else {
                redTextColor = getResources().getColor(R.color.red, muezzinActivity.getTheme());
            }
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

        if (maybePlace.isDefined()) {
            place = maybePlace.get();
        }

        return layout;
    }

    @Override public void onPrayerTimesDownloaded(@NonNull List<PrayerTimesOfDay> prayerTimes) {
        if (!PrayerTimesOfDay.saveAllPrayerTimes(context, place, prayerTimes)) {
            changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
            return;
        }

        LocalDate today = LocalDate.now();

        for (int i = 0, size = prayerTimes.size(); i < size; i++) {
            if (prayerTimes.get(i).date.equals(today)) {
                maybePrayerTimesOfDay = Optional.with(prayerTimes.get(i));
                break;
            }
        }

        if (maybePrayerTimesOfDay.isEmpty()) {
            Log.error(getClass(), "Did not find today's prayer times in downloaded prayer times!");

            changeStateTo(MultiStateView.VIEW_STATE_EMPTY, RETRY_ACTION_DOWNLOAD);
        } else {
            initializeUI();
        }
    }

    @Override public void onDownloadPrayerTimesFailed(Exception e) {
        Log.error(getClass(), e, "Failed to download prayer times for place '%s'!", place);
        changeStateTo(MultiStateView.VIEW_STATE_ERROR, RETRY_ACTION_DOWNLOAD);
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
                            fab.setOnClickListener(v -> retry(retryAction));
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
                MuezzinAPI.get().getPrayerTimes(place, this);
                break;
        }
    }

    private void loadTodaysPrayerTimes() {
        changeStateTo(MultiStateView.VIEW_STATE_LOADING, 0);

        maybePrayerTimesOfDay = PrayerTimesOfDay.getPrayerTimesForToday(context, place);

        if (maybePrayerTimesOfDay.isEmpty()) {
            Log.debug(getClass(), "Today's prayer times for place '%s' wasn't found on database!", place);

            MuezzinAPI.get().getPrayerTimes(place, this);
        } else {
            Log.debug(getClass(), "Loaded today's prayer times for place '%s' from database!", place);

            initializeUI();
        }
    }

    private void initializeUI() {
        changeStateTo(MultiStateView.VIEW_STATE_CONTENT, 0);

        Optional<String> maybePlaceName = place.getPlaceName(context);

        if (maybePlaceName.isDefined()) {
            if (muezzinActivity != null) {
                String gregorianDate = LocalDate.now().toString(FULL_DATE_FORMATTTER);
                String hijriDate = getHijriDate();

                muezzinActivity.setTitle(maybePlaceName.get());
                muezzinActivity.setSubtitle(gregorianDate + " / " + hijriDate);
            }

            Optional<Place> maybeLastPlace = Pref.Places.getLastPlace(context);

            if (maybeLastPlace.isDefined() && !maybeLastPlace.get().equals(place)) {
                PrayerTimeReminder.reschedulePrayerTimeReminders(context);
            }
        }

        if (maybePrayerTimesOfDay.isDefined()) {
            PrayerTimesOfDay prayerTimes = maybePrayerTimesOfDay.get();

            textViewFajr.setText(prayerTimes.fajr.toString(PrayerTimesOfDay.TIME_FORMATTER));
            textViewDhuhr.setText(prayerTimes.dhuhr.toString(PrayerTimesOfDay.TIME_FORMATTER));
            textViewAsr.setText(prayerTimes.asr.toString(PrayerTimesOfDay.TIME_FORMATTER));
            textViewMaghrib.setText(prayerTimes.maghrib.toString(PrayerTimesOfDay.TIME_FORMATTER));
            textViewIsha.setText(prayerTimes.isha.toString(PrayerTimesOfDay.TIME_FORMATTER));
            textViewShuruq.setText(prayerTimes.shuruq.toString(PrayerTimesOfDay.TIME_FORMATTER));
            textViewQibla.setText(prayerTimes.qibla.toString(PrayerTimesOfDay.TIME_FORMATTER));
        }

        PrayerTimesWidgetBase.updateAllWidgets(context);
    }

    private void updateRemainingTime() {
        if (maybePrayerTimesOfDay.isDefined()) {
            PrayerTimesOfDay prayerTimes = maybePrayerTimesOfDay.get();

            LocalTime nextPrayerTime  = prayerTimes.nextPrayerTime();
            String nextPrayerTimeName = PrayerTimesOfDay.prayerTimeLocalizedName(context, prayerTimes.nextPrayerTimeType());

            LocalTime remaining  = RemainingTime.to(nextPrayerTime);
            String remainingTime = remaining.toString(RemainingTime.FORMATTER);

            boolean isRemainingLessThan45Minutes = remaining.getHourOfDay() == 0 && remaining.getMinuteOfHour() < 45;
            int color = isRemainingLessThan45Minutes ? redTextColor : defaultTextColor;

            if (isAdded()) {
                textViewRemainingTimeInfo.setText(getString(R.string.prayerTimes_cardTitle_remainingTime, nextPrayerTimeName));
                textViewRemainingTime.setText(remainingTime);

                textViewRemainingTimeInfo.setTextColor(color);
                textViewRemainingTime.setTextColor(color);
            }
        }
    }

    private void scheduleRemainingTimeCounter() {
        timer = new Timer();

        timerTask = new TimerTask() {
            @Override public void run() {
                FragmentActivity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(() -> updateRemainingTime());
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

    private String getHijriDate() {
        LocalDateTime hijriNow = LocalDateTime.now(IslamicChronology.getInstance());
        String originalHijriDate = hijriNow.toString(FULL_DATE_PATTERN, Locale.getDefault());

        String hijriMonthName = getString(
            getResources().getIdentifier(
                "hijriMonth" + hijriNow.getMonthOfYear(),
                "string",
                getContext().getApplicationInfo().packageName
            )
        );

        return originalHijriDate.replaceAll("^(.+) (.+) (.+)$", "$1 " + hijriMonthName + " $3");
    }
}
