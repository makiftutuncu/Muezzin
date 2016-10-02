package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.mehmetakiftutuncu.muezzin.services.PrayerTimeReminderService;
import com.mehmetakiftutuncu.muezzin.services.PrayerTimesUpdaterService;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by akif on 24/05/16.
 */
public class PrayerTimeReminder {
    private static final String EXTRA_INDEX             = "index";
    private static final String EXTRA_PRAYER_TIME_TYPE  = "prayerTimeType";
    private static final String EXTRA_REMINDER_TIME     = "reminderTime";
    private static final String EXTRA_IS_ON_PRAYER_TIME = "isOnPrayerTime";

    public final int index;
    public final PrayerTimeType prayerTimeType;
    public final LocalTime reminderTime;
    public final boolean isOnPrayerTime;

    public PrayerTimeReminder(int index, PrayerTimeType prayerTimeType, LocalTime reminderTime) {
        this.index          = 100 + index;
        this.prayerTimeType = prayerTimeType;
        this.reminderTime   = reminderTime;
        this.isOnPrayerTime = false;
    }

    public PrayerTimeReminder(int index, PrayerTimeType prayerTimeType, LocalTime reminderTime, boolean isOnPrayerTime) {
        this.index          = 200 + index;
        this.prayerTimeType = prayerTimeType;
        this.reminderTime   = reminderTime;
        this.isOnPrayerTime = isOnPrayerTime;
    }

    public static Optional<List<PrayerTimeReminder>> getNextReminderTimes(Context context) {
        Optional<Place> maybePlace = Pref.Places.getCurrentPlace(context);

        if (maybePlace.isEmpty()) {
            return Optional.empty();
        }

        Place place = maybePlace.get();

        Optional<PrayerTimesOfDay> maybePrayerTimes = PrayerTimesOfDay.getPrayerTimesForToday(context, place);

        if (maybePrayerTimes.isEmpty()) {
            /* No prayer times are found for today. Most probably, we ran out of data. Update prayer times!
             *
             * P.S. This is just a best effort since it will happen while trying to schedule reminders.
             * If user has no reminders enabled, then he/she needs to launch the app to see that there's no more data. */
            PrayerTimesUpdaterService.setUpdater(context);

            return Optional.empty();
        }

        PrayerTimesOfDay prayerTimes = maybePrayerTimes.get();
        List<PrayerTimeReminder> prayerTimeReminders = new ArrayList<>();
        LocalTime now = LocalTime.now();

        PrayerTimeType[] prayerTimeTypes = PrayerTimeType.values();
        for (int i = 0, length = prayerTimeTypes.length; i < length; i++) {
            PrayerTimeType prayerTimeType  = prayerTimeTypes[i];
            boolean isEnabled              = Pref.Reminders.isEnabled(context, prayerTimeType);
            boolean remindBeforePrayerTime = Pref.Reminders.remindBeforePrayerTime(context, prayerTimeType);
            boolean remindOnPrayerTime     = Pref.Reminders.remindOnPrayerTime(context, prayerTimeType);

            if (isEnabled) {
                LocalTime prayerTime = prayerTimes.getPrayerTimeByType(prayerTimeType);

                if (prayerTime.isAfter(now)) {
                    LocalTime reminderTime = prayerTime.minusMinutes(Pref.Reminders.timeToRemind(context, prayerTimeType));

                    if (remindOnPrayerTime) {
                        PrayerTimeReminder prayerTimeReminder = new PrayerTimeReminder(i, prayerTimeType, prayerTime, true);

                        prayerTimeReminders.add(prayerTimeReminder);
                    }

                    // Don't remind a prayer time with reminder time set to too close
                    // (e.g. when asr is at 17:15 and reminder buffer is 15 minutes, only set reminder if it is not yet 17:00)
                    if (remindBeforePrayerTime && reminderTime.isAfter(now)) {
                        PrayerTimeReminder prayerTimeReminder = new PrayerTimeReminder(i, prayerTimeType, reminderTime);

                        prayerTimeReminders.add(prayerTimeReminder);
                    }
                }
            }
        }

        return Optional.with(prayerTimeReminders);
    }

    public static void reschedulePrayerTimeReminders(Context context) {
        Optional<List<PrayerTimeReminder>> maybeReminderTimes = getNextReminderTimes(context);

        if (maybeReminderTimes.isEmpty()) {
            return;
        }

        List<PrayerTimeReminder> prayerTimeReminders = maybeReminderTimes.get();

        for (int i = 0, size = prayerTimeReminders.size(); i < size; i++) {
            PrayerTimeReminder prayerTimeReminder = prayerTimeReminders.get(i);

            PrayerTimeReminderService.setPrayerTimeReminderAlarm(context, prayerTimeReminder);
        }

        if (isAtLeastOneReminderEnabled(context)) {
            PrayerTimeReminderService.setSchedulerAlarm(context);
        }
    }

    public static boolean isAtLeastOneReminderEnabled(Context context) {
        PrayerTimeType[] prayerTimeTypes = PrayerTimeType.values();

        for (int i = 0, length = prayerTimeTypes.length; i < length; i++) {
            if (Pref.Reminders.isEnabled(context, prayerTimeTypes[i])) {
                return true;
            }
        }

        return false;
    }

    @NonNull public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putInt(EXTRA_INDEX, index);
        bundle.putString(EXTRA_PRAYER_TIME_TYPE, prayerTimeType.name);
        bundle.putString(EXTRA_REMINDER_TIME, reminderTime.toString(PrayerTimesOfDay.TIME_FORMATTER));
        bundle.putBoolean(EXTRA_IS_ON_PRAYER_TIME, isOnPrayerTime);

        return bundle;
    }

    @NonNull public static Optional<PrayerTimeReminder> fromBundle(Bundle bundle) {
        if (bundle == null) {
            return Optional.empty();
        }

        int index                     = bundle.getInt(EXTRA_INDEX);
        PrayerTimeType prayerTimeType = PrayerTimeType.from(bundle.getString(EXTRA_PRAYER_TIME_TYPE));
        LocalTime reminderTime        = LocalTime.parse(bundle.getString(EXTRA_REMINDER_TIME), PrayerTimesOfDay.TIME_FORMATTER);
        boolean isOnPrayerTime        = bundle.getBoolean(EXTRA_IS_ON_PRAYER_TIME, false);

        return Optional.with(new PrayerTimeReminder(index, prayerTimeType, reminderTime, isOnPrayerTime));
    }

    @Override public String toString() {
        return String.format(
                Locale.ENGLISH,
                "{\"prayerTimeType\":\"%s\",\"reminderTime\":\"%s\",\"isOnPrayerTime\":%s}",
                prayerTimeType.name,
                reminderTime.toString(PrayerTimesOfDay.TIME_FORMATTER),
                isOnPrayerTime
        );
    }
}
