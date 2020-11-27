package com.mehmetakiftutuncu.muezzin.broadcastreceivers

import android.content.Context
import android.content.Intent
import androidx.legacy.content.WakefulBroadcastReceiver
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.services.PrayerTimeReminderService
import com.mehmetakiftutuncu.muezzin.services.PrayerTimeReminderService.Companion.setReminder
import com.mehmetakiftutuncu.muezzin.services.PrayerTimeReminderService.Companion.setScheduler

class PrayerTimeReminderBroadcastReceiver: WakefulBroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            PrayerTimeReminderService.actionRemind -> {
                Log.debug(javaClass, "Received prayer time reminder!")
                setReminder(context, intent.extras!!)
            }

            PrayerTimeReminderService.actionSchedule -> {
                Log.debug(javaClass, "Received prayer time scheduler!")
                setScheduler(context)
            }
        }
    }
}