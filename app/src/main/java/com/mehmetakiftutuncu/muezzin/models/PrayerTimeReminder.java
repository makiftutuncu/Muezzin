package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.services.PrayerTimeReminderService;
import com.mehmetakiftutuncu.muezzin.services.PrayerTimesUpdaterService;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by akif on 24/05/16.
 */
public class PrayerTimeReminder {
    private static final String EXTRA_INDEX            = "index";
    private static final String EXTRA_PRAYER_TIME_NAME = "prayerTimeName";
    private static final String EXTRA_REMINDER_TIME    = "reminderTime";

    public final int index;
    public final String prayerTimeName;
    public final DateTime reminderTime;

    public PrayerTimeReminder(int index, String prayerTimeName, DateTime reminderTime) {
        this.index          = index;
        this.prayerTimeName = prayerTimeName;
        this.reminderTime   = reminderTime;
    }

    public static Optional<ArrayList<PrayerTimeReminder>> getNextReminderTimes(Context context) {
        Optional<Place> maybePlace = Pref.Places.getCurrentPlace(context);

        if (maybePlace.isEmpty) {
            return new None<>();
        }

        Place place = maybePlace.get();

        Optional<PrayerTimes> maybePrayerTimes = PrayerTimes.getPrayerTimesForToday(context, place);

        if (maybePrayerTimes.isEmpty) {
            /* No prayer times are found for today. Most probably, we ran out of data. Update prayer times!
             *
             * P.S. This is just a best effort since it will happen while trying to schedule reminders.
             * If user has no reminders enabled, then he/she needs to launch the app to see that there's no more data. */
            PrayerTimesUpdaterService.setUpdater(context);

            return new None<>();
        }

        PrayerTimes prayerTimes = maybePrayerTimes.get();
        ArrayList<PrayerTimeReminder> prayerTimeReminders = new ArrayList<>();

        for (int i = 0, length = PrayerTimes.prayerTimeNames.length; i < length; i++) {
            String prayerTimeName = PrayerTimes.prayerTimeNames[i];
            boolean isEnabled     = Pref.Reminders.isEnabled(context, prayerTimeName);

            if (isEnabled) {
                DateTime prayerTime = prayerTimes.asArray[i];

                if (prayerTime.isAfterNow()) {
                    DateTime reminderTime = prayerTime.withZoneRetainFields(DateTimeZone.getDefault()).minusMinutes(Pref.Reminders.timeToRemind(context, prayerTimeName));

                    // Don't remind a prayer time with reminder time set to too close
                    // (e.g. when asr is at 17:15 and reminder buffer is 15 minutes, only set reminder if it is not yet 17:00)
                    if (reminderTime.isAfterNow()) {
                        PrayerTimeReminder prayerTimeReminder = new PrayerTimeReminder(i, prayerTimeName, reminderTime);

                        prayerTimeReminders.add(prayerTimeReminder);
                    }
                }
            }
        }

        return new Some<>(prayerTimeReminders);
    }

    public static void reschedulePrayerTimeReminders(Context context) {
        Optional<ArrayList<PrayerTimeReminder>> maybeReminderTimes = getNextReminderTimes(context);

        if (maybeReminderTimes.isEmpty) {
            return;
        }

        ArrayList<PrayerTimeReminder> prayerTimeReminders = maybeReminderTimes.get();

        if (prayerTimeReminders.isEmpty()) {
            // No more prayer time reminder for today
            return;
        }

        for (int i = 0, size = prayerTimeReminders.size(); i < size; i++) {
            PrayerTimeReminder prayerTimeReminder = prayerTimeReminders.get(i);

            PrayerTimeReminderService.setPrayerTimeReminderAlarm(context, prayerTimeReminder);
        }

        PrayerTimeReminderService.setSchedulerAlarm(context);
    }

    @NonNull public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putInt(EXTRA_INDEX, index);
        bundle.putString(EXTRA_PRAYER_TIME_NAME, prayerTimeName);
        bundle.putSerializable(EXTRA_REMINDER_TIME, reminderTime);

        return bundle;
    }

    @NonNull public static Optional<PrayerTimeReminder> fromBundle(Bundle bundle) {
        if (bundle == null) {
            return new None<>();
        }

        int index             = bundle.getInt(EXTRA_INDEX);
        String prayerTimeName = bundle.getString(EXTRA_PRAYER_TIME_NAME);
        DateTime reminderTime = (DateTime) bundle.getSerializable(EXTRA_REMINDER_TIME);

        return new Some<>(new PrayerTimeReminder(index, prayerTimeName, reminderTime));
    }

    @Override public String toString() {
        return String.format(Locale.ENGLISH, "{\"prayerTimeName\":\"%s\", \"reminderTime\":\"%s\"}", prayerTimeName, reminderTime.toString());
    }
}
