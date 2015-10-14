package com.mehmetakiftutuncu.muezzin.fragment;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.WithContentStates;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
import com.mehmetakiftutuncu.muezzin.utilities.Conf;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.Web;
import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import ru.vang.progressswitcher.ProgressWidget;

public class PrayerTimesFragment extends Fragment implements WithContentStates,
                                                             Callback,
                                                             Runnable {
    private static final String TAG = "PrayerTimesFragment";

    private int countryId;
    private int cityId;
    private Option<Integer> districtId;

    private ProgressWidget progressWidget;
    private TextView dateTextView;
    private TextView weekdayTextView;
    private TextView hijriDateTextView;
    private TextView remainingTimeInfoTextView;
    private TextView remainingTimeTextView;
    private TextView fajrTextView;
    private TextView shuruqTextView;
    private TextView dhuhrTextView;
    private TextView asrTextView;
    private TextView maghribTextView;
    private TextView ishaTextView;
    private TextView qiblaTextView;
    private FloatingActionButton pickDateFloatingActionButton;

    private ContentStates state;
    private ArrayList<PrayerTimes> prayerTimesList;

    private static final String dateFormatter          = "dd MMMM yyyy";
    private static final String weekdayFormatter       = "EEEE";
    private static final String timeFormatter          = "HH:mm";
    private static final String remainingTimeFormatter = "HH:mm:ss";

    private PrayerTimes todaysPrayerTimes;
    private Option<DateTime> nextPrayerTime = new None<>();
    private Option<String> nextPrayerTimeName = new None<>();

    private boolean shouldRefreshEverything = true;

    private Timer timer;
    private TimerTask timerTask;

    public static PrayerTimesFragment newInstance(int countryId, int cityId, Option<Integer> districtId) {
        PrayerTimesFragment districtsFragment = new PrayerTimesFragment();

        districtsFragment.setCountryId(countryId);
        districtsFragment.setCityId(cityId);
        districtsFragment.setDistrictId(districtId);

        return districtsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_prayertimes, container, false);

        progressWidget               = (ProgressWidget) layout.findViewById(R.id.progressWidget_prayertimes);
        dateTextView                 = (TextView) layout.findViewById(R.id.textView_prayerTimes_date);
        weekdayTextView              = (TextView) layout.findViewById(R.id.textView_prayerTimes_weekday);
        hijriDateTextView            = (TextView) layout.findViewById(R.id.textView_prayerTimes_hijriDate);
        remainingTimeInfoTextView    = (TextView) layout.findViewById(R.id.textView_prayerTimes_remainingTimeInfo);
        remainingTimeTextView        = (TextView) layout.findViewById(R.id.textView_prayerTimes_remainingTime);
        fajrTextView                 = (TextView) layout.findViewById(R.id.textView_prayerTimes_fajrTime);
        shuruqTextView               = (TextView) layout.findViewById(R.id.textView_prayerTimes_shuruqTime);
        dhuhrTextView                = (TextView) layout.findViewById(R.id.textView_prayerTimes_dhuhrTime);
        asrTextView                  = (TextView) layout.findViewById(R.id.textView_prayerTimes_asrTime);
        maghribTextView              = (TextView) layout.findViewById(R.id.textView_prayerTimes_maghribTime);
        ishaTextView                 = (TextView) layout.findViewById(R.id.textView_prayerTimes_ishaTime);
        qiblaTextView                = (TextView) layout.findViewById(R.id.textView_prayerTimes_qiblaTime);
        pickDateFloatingActionButton = (FloatingActionButton) layout.findViewById(R.id.fab_pickDate);

        ((Button) layout.findViewById(R.id.button_error_retry)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retryOnError(v);
            }
        });

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        loadPrayerTimes(false);
    }

    @Override
    public void changeStateTo(final ContentStates newState) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (state == null || !state.equals(newState)) {
                    state = newState;

                    switch (newState) {
                        case LOADING:
                            progressWidget.showProgress(true);
                            break;
                        case ERROR:
                            progressWidget.showError(true);
                            break;
                        case CONTENT:
                            progressWidget.showContent(true);
                            break;
                        case NO_CONTENT:
                            progressWidget.showEmpty(true);
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void retryOnError(View retryButton) {
        loadPrayerTimes(false);
    }

    public void setPrayerTimes(ArrayList<PrayerTimes> prayerTimesList, boolean saveData) {
        this.prayerTimesList = prayerTimesList;

        if (prayerTimesList == null) {
            changeStateTo(ContentStates.ERROR);
        } else if (prayerTimesList.isEmpty()) {
            changeStateTo(ContentStates.NO_CONTENT);
        } else {
            DateTime now = DateTime.now(DateTimeZone.UTC).withTime(0, 0, 0, 0);

            for (int i = 0, size = prayerTimesList.size(); i < size; i++) {
                todaysPrayerTimes = prayerTimesList.get(i);

                if (todaysPrayerTimes.dayDate.equals(now)) {
                    break;
                }
            }

            if (saveData) {
                boolean successful = PrayerTimes.save(prayerTimesList, countryId, cityId, districtId);

                if (!successful) {
                    changeStateTo(ContentStates.ERROR);
                } else {
                    changeStateTo(ContentStates.CONTENT);

                    scheduleAutoRefresh();
                }
            } else {
                changeStateTo(ContentStates.CONTENT);

                scheduleAutoRefresh();
            }
        }
    }

    public void loadPrayerTimes(boolean forceDownload) {
        changeStateTo(ContentStates.LOADING);

        updateTitle();

        if (!forceDownload) {
            Option<ArrayList<PrayerTimes>> prayerTimesListFromDisk = PrayerTimes.load(countryId, cityId, districtId);

            if (prayerTimesListFromDisk.isDefined) {
                setPrayerTimes(prayerTimesListFromDisk.get(), false);
            } else {
                downloadPrayerTimes();
            }
        } else {
            downloadPrayerTimes();
        }
    }

    public void downloadPrayerTimes() {
        Log.info(TAG, "Downloading prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + "...");

        if (!Web.hasInternet(getContext())) {
            Log.error(TAG, "Failed to download prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + ", there is no internet connection!");

            changeStateTo(ContentStates.ERROR);

            Snackbar.make(progressWidget, R.string.common_noInternet, Snackbar.LENGTH_LONG).show();
        } else {
            Web.instance().get(Conf.Url.prayerTimes(countryId, cityId, districtId, true), this);
        }
    }

    private void scheduleAutoRefresh() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        timer = new Timer();

        if (timerTask != null && timerTask.scheduledExecutionTime() > 0) {
            timerTask.cancel();
        }

        timerTask = new TimerTask() {
            @Override
            public void run() {
                FragmentActivity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(PrayerTimesFragment.this);
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private void refreshData() {
        DateTime now = DateTime.now().withZoneRetainFields(DateTimeZone.UTC);

        if (nextPrayerTime.isEmpty && nextPrayerTimeName.isEmpty) {
            nextPrayerTime     = todaysPrayerTimes.getNextPrayerTime();
            nextPrayerTimeName = todaysPrayerTimes.getNextPrayerTimeName(getContext());
        }

        if (!shouldRefreshEverything) {
            updateRemainingTime(now);
        } else {
            dateTextView.setText(todaysPrayerTimes.dayDate.toString(dateFormatter, Locale.getDefault()));
            weekdayTextView.setText(todaysPrayerTimes.dayDate.toString(weekdayFormatter, Locale.getDefault()));
            hijriDateTextView.setText(getHijriDate());

            updateRemainingTime(now);

            fajrTextView.setText(todaysPrayerTimes.fajr.toString(timeFormatter, Locale.getDefault()));
            shuruqTextView.setText(todaysPrayerTimes.shuruq.toString(timeFormatter, Locale.getDefault()));
            dhuhrTextView.setText(todaysPrayerTimes.dhuhr.toString(timeFormatter, Locale.getDefault()));
            asrTextView.setText(todaysPrayerTimes.asr.toString(timeFormatter, Locale.getDefault()));
            maghribTextView.setText(todaysPrayerTimes.maghrib.toString(timeFormatter, Locale.getDefault()));
            ishaTextView.setText(todaysPrayerTimes.isha.toString(timeFormatter, Locale.getDefault()));
            qiblaTextView.setText(todaysPrayerTimes.qibla.toString(timeFormatter, Locale.getDefault()));

            shouldRefreshEverything = false;
        }
    }

    private void updateRemainingTime(DateTime now) {
        if (nextPrayerTime.isDefined && nextPrayerTimeName.isDefined) {
            DateTime remaining = nextPrayerTime.get().minus(now.getMillis());

            String remainingTime = remaining.toString(remainingTimeFormatter, Locale.getDefault());

            remainingTimeInfoTextView.setText(getString(R.string.prayerTimes_remainingTimeInfo, nextPrayerTimeName));
            remainingTimeTextView.setText(remainingTime);

            if (remaining.getMillis() < 2000) {
                shouldRefreshEverything = true;
            }
        } else {
            // TODO It is after isha, need next day's data!
        }
    }

    private String getHijriDate() {
        String originalHijriDate = todaysPrayerTimes.hijriDayDate.toString(dateFormatter, Locale.getDefault());
        String hijriMonthName = getString(
            getResources().getIdentifier(
                "hijriMonth_" + todaysPrayerTimes.hijriDayDate.getMonthOfYear(),
                "string",
                getContext().getApplicationInfo().packageName
            )
        );

        return originalHijriDate.replaceAll("^(.+) (.+) (.+)$", "$1 " + hijriMonthName + " $3");
    }

    private void updateTitle() {
        Option<Country> countryOption   = Country.get(countryId);
        Option<City> cityOption         = City.get(countryId, cityId);
        Option<District> districtOption = districtId.isDefined ? District.get(countryId, cityId, districtId.get()) : new None<District>();

        ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (countryOption.isDefined && cityOption.isDefined && districtOption.isDefined) {
            String title    = districtOption.get().name;
            String subtitle = cityOption.get().name + ", " + countryOption.get().localizedName();

            if (supportActionBar != null) {
                supportActionBar.setTitle(title);
                supportActionBar.setSubtitle(subtitle);
            }
        } else if (countryOption.isDefined && cityOption.isDefined) {
            String title    = cityOption.get().name;
            String subtitle = countryOption.get().localizedName();

            if (supportActionBar != null) {
                supportActionBar.setTitle(title);
                supportActionBar.setSubtitle(subtitle);
            }
        } else if (countryOption.isDefined) {
            String title = countryOption.get().localizedName();

            if (supportActionBar != null) {
                supportActionBar.setTitle(title);
            }
        }
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.error(TAG, "Failed to get prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + " from Web!");

        changeStateTo(ContentStates.ERROR);
    }

    @Override
    public void onResponse(Response response) {
        if (!Web.isResponseSuccessfulAndJson(response)) {
            Log.error(TAG, "Failed to process Web response to get prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + ", response is not a Json response!");

            changeStateTo(ContentStates.ERROR);
        } else {
            try {
                String prayerTimesListJsonString = response.body().string();

                JSONObject prayerTimesListJson = new JSONObject(prayerTimesListJsonString);
                JSONArray prayerTimesJsonArray = prayerTimesListJson.getJSONArray("times");

                final Option<ArrayList<PrayerTimes>> prayerTimesList = PrayerTimes.fromJsonArray(prayerTimesJsonArray);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPrayerTimes(prayerTimesList.get(), true);
                    }
                });
            } catch (Throwable t) {
                Log.error(TAG, "Failed to process Web response to get prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + "!", t);

                changeStateTo(ContentStates.ERROR);
            }
        }
    }

    @Override
    public void run() {
        refreshData();
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public void setDistrictId(Option<Integer> districtId) {
        this.districtId = districtId;
    }
}
