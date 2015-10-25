package com.mehmetakiftutuncu.muezzin.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.PrayerTimesPagerAdapter;
import com.mehmetakiftutuncu.muezzin.fragment.PrayerTimesFragment;
import com.mehmetakiftutuncu.muezzin.interfaces.WithContentStates;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.ContentStates;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
import com.mehmetakiftutuncu.muezzin.utilities.Conf;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.StringUtils;
import com.mehmetakiftutuncu.muezzin.utilities.Web;
import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.mehmetakiftutuncu.muezzin.utilities.option.Some;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import ru.vang.progressswitcher.ProgressWidget;

public class MainActivity extends AppCompatActivity implements WithContentStates, Callback {
    public static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ProgressWidget mProgressWidget;
    private ViewPager mViewPager;

    private PrayerTimesPagerAdapter mPrayerTimesPagerAdapter;

    private ContentStates mState;

    private int mCountryId;
    private int mCityId;
    private Option<Integer> mDistrictId;

    private ArrayList<PrayerTimes> mPrayerTimesList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        initialize();
    }

    private void initialize() {
        setSupportActionBar(mToolbar);

        String currentLocation = Pref.CurrentLocation.get();

        if (!StringUtils.isEmpty(currentLocation)) {
            String[] split = currentLocation.split("\\.");

            mCountryId  = Integer.parseInt(split[0]);
            mCityId     = Integer.parseInt(split[1]);
            mDistrictId = split[2].equals("None") ? new None<Integer>() : new Some<>(Integer.parseInt(split[2]));

            loadPrayerTimes();
        } else {
            finish();
            startActivity(new Intent(this, WelcomeActivity.class));
        }
    }

    private void bindViews() {
        mToolbar        = (Toolbar)        findViewById(R.id.toolbar);
        mTabLayout      = (TabLayout)      findViewById(R.id.tabLayout);
        mProgressWidget = (ProgressWidget) findViewById(R.id.progressWidget);
        mViewPager      = (ViewPager)      findViewById(R.id.viewPager);
    }

    public void loadPrayerTimes() {
        changeStateTo(ContentStates.LOADING);

        updateTitle();

        Option<ArrayList<PrayerTimes>> prayerTimesListFromDisk = PrayerTimes.load(mCountryId, mCityId, mDistrictId);

        if (prayerTimesListFromDisk.isDefined) {
            setPrayerTimes(prayerTimesListFromDisk.get(), false);
        } else {
            downloadPrayerTimes();
        }
    }

    public void downloadPrayerTimes() {
        Log.info(TAG, "Downloading prayer times for country " + mCountryId + ", city " + mCityId + " and district " + mDistrictId + "...");

        if (!Web.hasInternet(this)) {
            Log.error(TAG, "Failed to download prayer times for country " + mCountryId + ", city " + mCityId + " and district " + mDistrictId + ", there is no internet connection!");

            changeStateTo(ContentStates.ERROR);

            Snackbar.make(mProgressWidget, R.string.common_noInternet, Snackbar.LENGTH_LONG).show();
        } else {
            Web.instance().get(Conf.Url.prayerTimes(mCountryId, mCityId, mDistrictId, true), this);
        }
    }

    public void setPrayerTimes(ArrayList<PrayerTimes> prayerTimesList, boolean saveData) {
        mPrayerTimesList = prayerTimesList;

        if (prayerTimesList == null) {
            changeStateTo(ContentStates.ERROR);
        } else if (prayerTimesList.isEmpty()) {
            changeStateTo(ContentStates.NO_CONTENT);
        } else {
            DateTime now        = DateTime.now().withZoneRetainFields(DateTimeZone.UTC);
            DateTime nowDayDate = now.withTime(0, 0, 0, 0);
            boolean missingPrayerTimesData = nowDayDate.isBefore(prayerTimesList.get(0).dayDate) || now.isAfter(prayerTimesList.get(prayerTimesList.size() - 1).isha);

            if (saveData) {
                boolean successful = PrayerTimes.save(prayerTimesList, mCountryId, mCityId, mDistrictId);

                if (!successful) {
                    changeStateTo(ContentStates.ERROR);
                } else {
                    if (missingPrayerTimesData) {
                        changeStateTo(ContentStates.NO_CONTENT);
                    } else {
                        initializeViewPager();
                    }
                }
            } else {
                if (missingPrayerTimesData) {
                    changeStateTo(ContentStates.NO_CONTENT);
                } else {
                    initializeViewPager();
                }
            }
        }
    }

    private void updateTitle() {
        Option<Country> countryOption   = Country.get(mCountryId);
        Option<City> cityOption         = City.get(mCountryId, mCityId);
        Option<District> districtOption = mDistrictId.isDefined ? District.get(mCountryId, mCityId, mDistrictId.get()) : new None<District>();

        if (countryOption.isDefined && cityOption.isDefined && districtOption.isDefined) {
            String title    = districtOption.get().name;
            String subtitle = cityOption.get().name + ", " + countryOption.get().localizedName();

            setTitle(title);
            mToolbar.setSubtitle(subtitle);
        } else if (countryOption.isDefined && cityOption.isDefined) {
            String title    = cityOption.get().name;
            String subtitle = countryOption.get().localizedName();

            setTitle(title);
            mToolbar.setSubtitle(subtitle);
        } else if (countryOption.isDefined) {
            String title = countryOption.get().localizedName();

            setTitle(title);
        }
    }

    private void initializeViewPager() {
        mPrayerTimesPagerAdapter = new PrayerTimesPagerAdapter(getSupportFragmentManager());

        DateTime today = DateTime.now().withZoneRetainFields(DateTimeZone.UTC).withTime(0, 0, 0, 0);

        for (int i = 0, size = mPrayerTimesList.size(); i < size; i++) {
            PrayerTimes prayerTimes = mPrayerTimesList.get(i);

            if (!prayerTimes.dayDate.isBefore(today)) {
                PrayerTimesFragment prayerTimesFragment = PrayerTimesFragment.getWith(prayerTimes);
                mPrayerTimesPagerAdapter.add(prayerTimesFragment);
            }
        }

        mViewPager.setAdapter(mPrayerTimesPagerAdapter);

        mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mTabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);

        // Set a tab changed listener to set the first tab's text correctly, tab changed listener will be overridden below anyway.
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                PrayerTimesFragment prayerTimesFragment = (PrayerTimesFragment) mPrayerTimesPagerAdapter.getItem(position);
                tab.setText(prayerTimesFragment.getFullTitle());
                mViewPager.setCurrentItem(position, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Setup tabs and bind view pager to tabs.
        mTabLayout.setupWithViewPager(mViewPager);

        // Because setupWithViewPager method changes the tab listener, update it here again after tabs are ready.
        mTabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                PrayerTimesFragment prayerTimesFragment = (PrayerTimesFragment) mPrayerTimesPagerAdapter.getItem(position);
                tab.setText(prayerTimesFragment.getFullTitle());
                mViewPager.setCurrentItem(position, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                PrayerTimesFragment prayerTimesFragment = (PrayerTimesFragment) mPrayerTimesPagerAdapter.getItem(position);
                tab.setText(prayerTimesFragment.getShortTitle());
            }
        });

        changeStateTo(ContentStates.CONTENT);
    }

    @Override
    public void changeStateTo(final ContentStates newState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mState == null || !mState.equals(newState)) {
                    mState = newState;

                    switch (newState) {
                        case LOADING:
                            mProgressWidget.showProgress(true);
                            break;
                        case ERROR:
                            mProgressWidget.showError(true);
                            break;
                        case CONTENT:
                            mProgressWidget.showContent(true);
                            break;
                        case NO_CONTENT:
                            mProgressWidget.showEmpty(true);
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void retryOnError(View retryButton) {
        loadPrayerTimes();
    }

    @Override
    public void onFailure(Request request, IOException e) {
        Log.error(TAG, "Failed to get prayer times for country " + mCountryId + ", city " + mCityId + " and district " + mDistrictId + " from Web!");

        changeStateTo(ContentStates.ERROR);
    }

    @Override
    public void onResponse(Response response) throws IOException {
        if (!Web.isResponseSuccessfulAndJson(response)) {
            Log.error(TAG, "Failed to process Web response to get prayer times for country " + mCountryId + ", city " + mCityId + " and district " + mDistrictId + ", response is not a Json response!");

            changeStateTo(ContentStates.ERROR);
        } else {
            try {
                String prayerTimesListJsonString = response.body().string();

                JSONObject prayerTimesListJson = new JSONObject(prayerTimesListJsonString);
                JSONArray prayerTimesJsonArray = prayerTimesListJson.getJSONArray("times");

                final Option<ArrayList<PrayerTimes>> prayerTimesList = PrayerTimes.fromJsonArray(prayerTimesJsonArray);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setPrayerTimes(prayerTimesList.get(), true);
                    }
                });
            } catch (Throwable t) {
                Log.error(TAG, "Failed to process Web response to get prayer times for country " + mCountryId + ", city " + mCityId + " and district " + mDistrictId + "!", t);

                changeStateTo(ContentStates.ERROR);
            }
        }
    }
}
