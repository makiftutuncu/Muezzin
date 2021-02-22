package com.mehmetakiftutuncu.muezzin.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder.Companion.rescheduleReminders

class BootCompletedBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            rescheduleReminders(context)
        }
    }
}