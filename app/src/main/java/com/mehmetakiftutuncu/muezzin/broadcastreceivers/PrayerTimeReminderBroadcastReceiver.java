package com.mehmetakiftutuncu.muezzin.broadcastreceivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.mehmetakiftutuncu.muezzin.services.PrayerTimeReminderService;
import com.mehmetakiftutuncu.muezzin.utilities.Log;

public class PrayerTimeReminderBroadcastReceiver extends WakefulBroadcastReceiver {
    public PrayerTimeReminderBroadcastReceiver() {}

    @Override public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            String action = intent.getAction();

            if (action.equals(PrayerTimeReminderService.ACTION_REMIND)) {
                Log.debug(getClass(), "Received prayer time reminder!");

                PrayerTimeReminderService.setReminder(context, intent.getExtras());
            } else if (action.equals(PrayerTimeReminderService.ACTION_SCHEDULE)) {
                Log.debug(getClass(), "Received prayer time scheduler!");

                PrayerTimeReminderService.setScheduler(context);
            }
        }

    }
}
