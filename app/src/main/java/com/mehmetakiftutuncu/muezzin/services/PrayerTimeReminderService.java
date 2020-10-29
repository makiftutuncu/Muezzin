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
import androidx.core.app.NotificationCompat;

import com.github.mehmetakiftutuncu.toolbelt.Log;
import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.PrayerTimesActivity;
import com.mehmetakiftutuncu.muezzin.broadcastreceivers.PrayerTimeReminderBroadcastReceiver;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimesOfDay;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.widgetproviders.PrayerTimesWidgetBase;

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

            switch (action) {
                case ACTION_REMIND:
                    showNotification(intent);
                    break;

                case ACTION_SCHEDULE:
                    schedule();
                    break;
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

        PendingIntent operation = PendingIntent.getBroadcast(context, prayerTimeReminder.index, intent, 0);

        alarmManager.set(AlarmManager.RTC_WAKEUP, prayerTimeReminder.reminderTime.toDateTimeToday().getMillis(), operation);
    }

    private void showNotification(Intent intent) {
        Bundle extras = intent.getExtras();

        Optional<PrayerTimeReminder> maybePrayerTimeReminder = PrayerTimeReminder.fromBundle(extras);

        if (maybePrayerTimeReminder.isDefined()) {
            PrayerTimeReminder prayerTimeReminder = maybePrayerTimeReminder.get();

            Log.debug(getClass(), "Showing notification for '%s'...", prayerTimeReminder);

            String localizedPrayerTimeName = PrayerTimesOfDay.prayerTimeLocalizedName(this, prayerTimeReminder.prayerTimeType);

            int remainingMinutes = Pref.Reminders.timeToRemind(this, prayerTimeReminder.prayerTimeType);

            String title = prayerTimeReminder.isOnPrayerTime ? getString(R.string.reminders_notificationOnPrayerTime_title) : getString(R.string.reminders_notification_title);
            String body = prayerTimeReminder.isOnPrayerTime ? getString(R.string.reminders_notificationOnPrayerTime_summary, localizedPrayerTimeName) : getString(R.string.reminders_notification_summary, remainingMinutes, localizedPrayerTimeName);

            String sound    = Pref.Reminders.sound(this, prayerTimeReminder.prayerTimeType);
            boolean vibrate = Pref.Reminders.vibrate(this, prayerTimeReminder.prayerTimeType);

            Intent launchIntent = new Intent(this, PrayerTimesActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setTicker(title)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_mosque)
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
        PrayerTimesWidgetBase.updateAllWidgets(this);
    }
}
