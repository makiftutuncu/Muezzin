package com.mehmetakiftutuncu.muezzin.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.interfaces.OnPrayerTimesDownloadedListener;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPIClient;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import java.util.ArrayList;

/**
 * Created by akif on 23/05/16.
 */
public class PrayerTimesUpdaterService extends IntentService implements OnPrayerTimesDownloadedListener {
    public PrayerTimesUpdaterService() {
        super("PrayerTimesUpdaterService");
    }

    private Optional<Place> maybeCurrentPlace;

    public static void setUpdater(Context context) {
        Intent intent = new Intent(context, PrayerTimesUpdaterService.class);

        context.startService(intent);
    }

    @Override protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            update();
        }
    }

    private void update() {
        maybeCurrentPlace = Pref.Places.getCurrentPlace(this);

        if (maybeCurrentPlace.isEmpty) {
            return;
        }

        Place place = maybeCurrentPlace.get();

        Log.debug(getClass(), "Updating prayer times for '%s'...", place);

        MuezzinAPIClient.getPrayerTimes(place, this);
    }

    @Override public void onPrayerTimesDownloaded(@NonNull ArrayList<PrayerTimes> prayerTimes) {
        Place place = maybeCurrentPlace.get();

        Log.debug(getClass(), "Saving prayer times for place '%s' to database...", place);

        if (PrayerTimes.saveAllPrayerTimes(this, place, prayerTimes)) {
            // Updated prayer times and saved them successfully, now try to reschedule prayer time reminders.
            PrayerTimeReminder.reschedulePrayerTimeReminders(this);
        }
    }

    @Override public void onPrayerTimesDownloadFailed() {
        Log.error(getClass(), "Failed to download prayer times for place '%s'!", maybeCurrentPlace.get());
    }
}
