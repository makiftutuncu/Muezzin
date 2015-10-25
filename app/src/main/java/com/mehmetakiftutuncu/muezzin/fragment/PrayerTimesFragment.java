package com.mehmetakiftutuncu.muezzin.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Timer;
import java.util.TimerTask;

public class PrayerTimesFragment extends Fragment {
    public static final String TAG = "PrayerTimesFragment";

    private ScrollView mLayout;
    private CardView mRemainingTimeLayout;
    private TextView mRemainingTimeInfo;
    private TextView mRemainingTime;
    private TextView mFajr;
    private TextView mDhuhr;
    private TextView mAsr;
    private TextView mMaghrib;
    private TextView mIsha;
    private TextView mShuruq;
    private TextView mQibla;

    private PrayerTimes mPrayerTimes;

    private boolean mActive;

    private Timer mTimer;
    private TimerTask mTimerTask;

    private static final String sTodayIndicator         = "\u26AB ";
    private static final String sShortDateFormatter     = "dd MMMM";
    private static final String sFullDateFormatter      = "dd MMMM yyyy, EEEE";
    private static final String sTimeFormatter          = "HH:mm";
    private static final String sRemainingTimeFormatter = "HH:mm:ss";

    public static PrayerTimesFragment getWith(PrayerTimes prayerTimes) {
        PrayerTimesFragment prayerTimesFragment = new PrayerTimesFragment();

        prayerTimesFragment.setPrayerTimes(prayerTimes);

        return prayerTimesFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = (ScrollView) inflater.inflate(R.layout.fragment_prayertimes, container, false);

        initializeViews();

        return mLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mPrayerTimes = (PrayerTimes) savedInstanceState.getSerializable("prayerTimes");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActive) {
            scheduleAutoRefresh();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        cancelAutoRefresh();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("prayerTimes", mPrayerTimes);

        super.onSaveInstanceState(outState);
    }

    public String getShortTitle() {
        return (isToday() ? sTodayIndicator : "") + mPrayerTimes.dayDate.toString(sShortDateFormatter);
    }

    public String getFullTitle() {
        return (isToday() ? sTodayIndicator : "") + mPrayerTimes.dayDate.toString(sFullDateFormatter);
    }

    private boolean isToday() {
        DateTime now = now();
        DateTime nowDayDate = now.withTime(0, 0, 0, 0);

        return nowDayDate.equals(mPrayerTimes.dayDate);
    }

    private void updateUI() {
        DateTime now = now();

        mActive = isToday();

        if (!mActive) {
            // Day is not today.
            mRemainingTimeLayout.setVisibility(View.GONE);
        } else {
            mRemainingTimeLayout.setVisibility(View.VISIBLE);

            DateTime nextPrayerTime   = mPrayerTimes.getNextPrayerTime();
            String nextPrayerTimeName = mPrayerTimes.getNextPrayerTimeName(getContext());

            DateTime remaining   = nextPrayerTime.minus(now.getMillis());
            String remainingTime = remaining.toString(sRemainingTimeFormatter);

            mRemainingTimeInfo.setText(getString(R.string.prayerTimes_cardTitle_remainingTime, nextPrayerTimeName));
            mRemainingTime.setText(remainingTime);
        }
    }

    private DateTime now() {
        return DateTime.now().withZoneRetainFields(DateTimeZone.UTC);
    }

    private void scheduleAutoRefresh() {
        mTimer = new Timer();

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                FragmentActivity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUI();
                        }
                    });
                }
            }
        };

        mTimer.scheduleAtFixedRate(mTimerTask, 0, 1000);
    }

    private void cancelAutoRefresh() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }

        if (mTimerTask != null && mTimerTask.scheduledExecutionTime() > 0) {
            mTimerTask.cancel();
        }
    }

    private void bindViews() {
        mRemainingTimeLayout = (CardView) mLayout.findViewById(R.id.cardView_remainingTime);
        mRemainingTimeInfo   = (TextView) mLayout.findViewById(R.id.textView_prayerTimes_remainingTimeInfo);
        mRemainingTime       = (TextView) mLayout.findViewById(R.id.textView_prayerTimes_remainingTime);
        mFajr                = (TextView) mLayout.findViewById(R.id.textView_prayerTimes_fajrTime);
        mShuruq              = (TextView) mLayout.findViewById(R.id.textView_prayerTimes_shuruqTime);
        mDhuhr               = (TextView) mLayout.findViewById(R.id.textView_prayerTimes_dhuhrTime);
        mAsr                 = (TextView) mLayout.findViewById(R.id.textView_prayerTimes_asrTime);
        mMaghrib             = (TextView) mLayout.findViewById(R.id.textView_prayerTimes_maghribTime);
        mIsha                = (TextView) mLayout.findViewById(R.id.textView_prayerTimes_ishaTime);
        mQibla               = (TextView) mLayout.findViewById(R.id.textView_prayerTimes_qiblaTime);
    }

    private void initializeViews() {
        bindViews();

        mFajr.setText(mPrayerTimes.fajr.toString(sTimeFormatter));
        mDhuhr.setText(mPrayerTimes.dhuhr.toString(sTimeFormatter));
        mAsr.setText(mPrayerTimes.asr.toString(sTimeFormatter));
        mMaghrib.setText(mPrayerTimes.maghrib.toString(sTimeFormatter));
        mIsha.setText(mPrayerTimes.isha.toString(sTimeFormatter));
        mShuruq.setText(mPrayerTimes.shuruq.toString(sTimeFormatter));
        mQibla.setText(mPrayerTimes.qibla.toString(sTimeFormatter));

        updateUI();
    }

    public void setPrayerTimes(PrayerTimes prayerTimes) {
        mPrayerTimes = prayerTimes;
    }
}
