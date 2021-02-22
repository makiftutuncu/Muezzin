package com.mehmetakiftutuncu.muezzin.models

import android.content.Context
import android.os.Bundle
import com.mehmetakiftutuncu.muezzin.repositories.PrayerTimesOfDayRepository
import com.mehmetakiftutuncu.muezzin.services.PrayerTimeReminderService
import com.mehmetakiftutuncu.muezzin.services.PrayerTimesUpdaterService
import com.mehmetakiftutuncu.muezzin.utilities.Pref
import org.joda.time.LocalTime

data class PrayerTimeReminder(val type: PrayerTimeType,
                              val time: LocalTime,
                              val isOnPrayerTime: Boolean) {
    constructor(bundle: Bundle): this(
        PrayerTimeType.from(bundle.getString(extraType, null)),
        LocalTime.parse(bundle.getString(extraTime), PrayerTimesOfDay.timeFormatter),
        bundle.getBoolean(extraIsOnPrayerTime)
    )

    val index: Int = (if (!isOnPrayerTime) 100 else 200) + type.ordinal

    fun toBundle(): Bundle =
        Bundle().apply {
            putString(extraType, type.key)
            putString(extraTime, time.toString(PrayerTimesOfDay.timeFormatter))
            putBoolean(extraIsOnPrayerTime, isOnPrayerTime)
        }

    override fun toString(): String =
        """{"type":"${type.key}","time":"${time.toString(PrayerTimesOfDay.timeFormatter)}","isOnPrayerTime":$isOnPrayerTime}"""

    companion object {
        private const val extraType           = "type"
        private const val extraTime           = "time"
        private const val extraIsOnPrayerTime = "isOnPrayerTime"

        fun nextReminderTimes(ctx: Context): List<PrayerTimeReminder> {
            val place = Pref.Places.getCurrentPlace(ctx) ?: return emptyList()

            val prayerTimes = PrayerTimesOfDayRepository.getForToday(ctx, place)

            if (prayerTimes == null) {
                /* No prayer times are found for today. Most probably, we ran out of data. Update prayer times!
                 * P.S. This is just a best effort since it will happen while trying to schedule reminders.
                 * If user has no reminders enabled, then he/she needs to launch the app to see that there's no more data. */
                PrayerTimesUpdaterService.setUpdater(ctx)
                return emptyList()
            }

            val now = LocalTime.now()

            return PrayerTimeType.values().flatMap { type ->
                val enabled = Pref.Reminders.isEnabled(ctx, type)
                val remindBefore = Pref.Reminders.remindBeforePrayerTime(ctx, type)
                val remindOnTime = Pref.Reminders.remindOnPrayerTime(ctx, type)

                if (!enabled) {
                    return@flatMap emptyList()
                }

                val time = prayerTimes.prayerTimeByType(type)

                val reminderBuffer = Pref.Reminders.timeToRemind(ctx, type)

                val remindersBefore =
                    if (now.isBefore(time.minusMinutes(reminderBuffer)) && remindBefore) {
                        listOf(PrayerTimeReminder(type, time, isOnPrayerTime = false))
                    } else {
                        emptyList()
                    }

                val remindersOnTime =
                    if (now.isBefore(time) && remindOnTime) {
                        listOf(PrayerTimeReminder(type, time, isOnPrayerTime = true))
                    } else {
                        emptyList()
                    }

                remindersBefore + remindersOnTime
            }
        }

        fun rescheduleReminders(ctx: Context): Unit {
            nextReminderTimes(ctx).forEach { reminder ->
                PrayerTimeReminderService.setReminderAlarm(ctx, reminder)
            }

            val atLeastOneReminderEnabled =
                PrayerTimeType.values().indexOfFirst { Pref.Reminders.isEnabled(ctx, it) } >= 0

            if (atLeastOneReminderEnabled) {
                PrayerTimeReminderService.setSchedulerAlarm(ctx)
            }
        }
    }
}