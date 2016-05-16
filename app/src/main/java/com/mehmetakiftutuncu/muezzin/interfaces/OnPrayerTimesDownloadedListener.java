package com.mehmetakiftutuncu.muezzin.interfaces;

import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;

import java.util.ArrayList;

/**
 * Created by akif on 13/05/16.
 */
public interface OnPrayerTimesDownloadedListener {
    void onPrayerTimesDownloaded(@NonNull ArrayList<PrayerTimes> prayerTimes);
    void onPrayerTimesDownloadFailed();
}
