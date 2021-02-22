package com.mehmetakiftutuncu.muezzin.services

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder
import com.mehmetakiftutuncu.muezzin.repositories.PrayerTimesOfDayRepository
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPI
import com.mehmetakiftutuncu.muezzin.utilities.Pref
import com.mehmetakiftutuncu.muezzin.widget.PrayerTimesWidget

class PrayerTimesUpdaterService: IntentService("PrayerTimesUpdaterService") {
    override fun onHandleIntent(intent: Intent?) {
        intent?.also {
            val ctx: Context = this

            Pref.Places.getCurrentPlace(ctx)?.also { currentPlace ->
                Log.debug(javaClass, "Updating prayer times for '$currentPlace'...")

                MuezzinAPI.getPrayerTimes(currentPlace, { e ->
                    Log.error(javaClass, e, "Failed to download prayer times for place '$currentPlace'!")
                }) { prayerTimes ->
                    val saved = PrayerTimesOfDayRepository.save(ctx, currentPlace, prayerTimes)

                    if (saved) {
                        // Updated prayer times and saved them successfully
                        // Now try to reschedule prayer time reminders.
                        PrayerTimeReminder.rescheduleReminders(ctx)
                        PrayerTimesWidget.updateAllWidgets(ctx)
                    }
                }
            }
        }
    }

    companion object {
        fun setUpdater(ctx: Context) {
            val intent = Intent(ctx, PrayerTimesUpdaterService::class.java)
            ctx.startService(intent)
        }
    }
}