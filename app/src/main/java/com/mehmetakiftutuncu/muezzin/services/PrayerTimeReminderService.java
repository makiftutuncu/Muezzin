package com.mehmetakiftutuncu.muezzin.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.PrayerTimesActivity;
import com.mehmetakiftutuncu.muezzin.broadcastreceivers.PrayerTimeReminderBroadcastReceiver;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import org.joda.time.DateTime;

/**
 * Created by akif on 23/05/16.
 */
public class PrayerTimeReminderService extends IntentService {
    public static final String ACTION_REMIND   = "com.mehmetakiftutuncu.muezzin.PrayerTimeReminderService.REMIND";
    public static final String ACTION_SCHEDULE = "com.mehmetakiftutuncu.muezzin.PrayerTimeReminderService.SCHEDULE";

    public PrayerTimeReminderService() {
        super("PrayerTimeReminderService");
    }

    public static void setReminder(Context context, Bundle prayerTimeReminderExtras) {
        Intent intent = new Intent(context, PrayerTimeReminderService.class);
        intent.putExtras(prayerTimeReminderExtras);
        intent.setAction(ACTION_REMIND);

        context.startService(intent);
    }

    public static void setScheduler(Context context) {
        Intent intent = new Intent(context, PrayerTimeReminderService.class);
        intent.setAction(ACTION_SCHEDULE);

        context.startService(intent);
    }

    @Override protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();

            if (action.equals(ACTION_REMIND)) {
                showReminder(intent);
            } else if (action.equals(ACTION_SCHEDULE)) {
                schedule();
            }
        }
    }

    public static void setSchedulerAlarm(Context context) {
        Log.debug(PrayerTimeReminder.class, "Scheduling an alarm to reschedule prayer time reminders tomorrow...");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, PrayerTimeReminderBroadcastReceiver.class);
        intent.setAction(PrayerTimeReminderService.ACTION_SCHEDULE);

        PendingIntent operation = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        DateTime schedulerTime = DateTime.now().withTimeAtStartOfDay().plusDays(1).plusSeconds(1);

        alarmManager.set(AlarmManager.RTC_WAKEUP, schedulerTime.getMillis(), operation);
    }

    public static void setPrayerTimeReminderAlarm(Context context, PrayerTimeReminder prayerTimeReminder) {
        Log.debug(PrayerTimeReminder.class, "Scheduling prayer time reminder for '%s'...", prayerTimeReminder);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, PrayerTimeReminderBroadcastReceiver.class);
        intent.putExtras(prayerTimeReminder.toBundle());
        intent.setAction(PrayerTimeReminderService.ACTION_REMIND);

        PendingIntent operation = PendingIntent.getBroadcast(context, prayerTimeReminder.index, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, prayerTimeReminder.reminderTime.getMillis(), operation);
    }

    private void showReminder(Intent intent) {
        Bundle extras = intent.getExtras();

        Optional<PrayerTimeReminder> maybePrayerTimeReminder = PrayerTimeReminder.fromBundle(extras);

        if (maybePrayerTimeReminder.isDefined) {
            PrayerTimeReminder prayerTimeReminder = maybePrayerTimeReminder.get();

            Log.debug(getClass(), "Showing prayer time for '%s'...", prayerTimeReminder);

            String localizedPrayerTimeName = PrayerTimes.prayerTimeLocalizedName(this, prayerTimeReminder.prayerTimeName);

            int remainingMinutes = Pref.Reminders.timeToRemind(this, prayerTimeReminder.prayerTimeName);
            String sound         = Pref.Reminders.sound(this, prayerTimeReminder.prayerTimeName);
            boolean vibrate      = Pref.Reminders.vibrate(this, prayerTimeReminder.prayerTimeName);

            Intent launchIntent = new Intent(this, PrayerTimesActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.reminders_notification_title))
                    .setContentText(getResources().getString(R.string.reminders_notification_summary, remainingMinutes, localizedPrayerTimeName))
                    .setTicker(getString(R.string.reminders_notification_title))
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                    .setContentIntent(pendingIntent)
                    .setDefaults(Notification.DEFAULT_LIGHTS);

            Notification notification = notificationBuilder.build();

            if (!sound.isEmpty()) {
                notification.sound = Uri.parse(sound);
            }

            if (vibrate) {
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, notification);
        }
    }

    private void schedule() {
        Log.debug(getClass(), "Rescheduling prayer time reminders...");

        PrayerTimeReminder.reschedulePrayerTimeReminders(this);
    }
}
