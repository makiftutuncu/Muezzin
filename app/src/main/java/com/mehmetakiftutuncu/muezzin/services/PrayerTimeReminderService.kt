package com.mehmetakiftutuncu.muezzin.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.activities.PrayerTimesActivity
import com.mehmetakiftutuncu.muezzin.broadcastreceivers.PrayerTimeReminderBroadcastReceiver
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeType
import com.mehmetakiftutuncu.muezzin.models.PrayerTimesOfDay
import com.mehmetakiftutuncu.muezzin.utilities.Pref
import com.mehmetakiftutuncu.muezzin.widget.PrayerTimesWidget
import org.joda.time.DateTime

class PrayerTimeReminderService: IntentService("PrayerTimeReminderService") {
    private val manager: NotificationManagerCompat by lazy {
        val ctx: Context = this@PrayerTimeReminderService
        NotificationManagerCompat.from(ctx)
    }

    private val channels: Map<PrayerTimeType, NotificationChannel> by lazy {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            emptyMap()
        } else {
            PrayerTimeType.values().fold(emptyMap()) { map, type ->
                val id = "${notificationChannel}_$type"
                val name = getString(resources.getIdentifier("prayerTime_${type.key}", null ,null))

                // TODO: Default settings, sound, vibration etc.?
                val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)

                manager.createNotificationChannel(channel)

                map + (type to channel)
            }
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            actionRemind   -> showNotification(intent)
            actionSchedule -> schedule()
            else           -> {}
        }
    }

    private fun showNotification(intent: Intent) {
        intent.extras?.let { PrayerTimeReminder(it) }?.also { reminder ->
            Log.debug(javaClass, "Showing notification for '$reminder'...")

            val ctx: Context = this

            val localizedName = PrayerTimesOfDay.localizedName(ctx, reminder.type)
            val remainingMinutes = Pref.Reminders.timeToRemind(ctx, reminder.type)
            val title = if (reminder.isOnPrayerTime) getString(R.string.reminders_notificationOnPrayerTime_title) else getString(R.string.reminders_notification_title)
            val body = if (reminder.isOnPrayerTime) getString(R.string.reminders_notificationOnPrayerTime_summary, localizedName) else getString(R.string.reminders_notification_summary, remainingMinutes, localizedName)
            val sound = Pref.Reminders.sound(ctx, reminder.type)
            val vibrate = Pref.Reminders.vibrate(ctx, reminder.type)

            val launchIntent = Intent(ctx, PrayerTimesActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(ctx, 0, launchIntent, 0)

            val channel = channels[reminder.type]

            val builder =
                if (channel != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationCompat.Builder(ctx, channel.id)
                } else {
                    NotificationCompat.Builder(ctx)
                }

            val notification = builder
                        .setContentTitle(title)
                        .setContentText(body)
                        .setTicker(title)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_mosque)
                        .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_LIGHTS)
                        .build().apply {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                        if (sound.isNotEmpty()) {
                            this.sound = Uri.parse(sound)
                        }

                        if (vibrate) {
                            this.defaults = this.defaults or Notification.DEFAULT_VIBRATE
                        }
                    }
                }

            manager.notify(0, notification)
        }
    }

    private fun schedule() {
        Log.debug(javaClass, "Rescheduling prayer time reminders...")

        PrayerTimeReminder.rescheduleReminders(this)
        PrayerTimesWidget.updateAllWidgets(this)
    }

    companion object {
        const val actionRemind   = "com.mehmetakiftutuncu.muezzin.PrayerTimeReminderService.REMIND";
        const val actionSchedule = "com.mehmetakiftutuncu.muezzin.PrayerTimeReminderService.SCHEDULE";

        private const val notificationChannel = "reminders";

        fun setReminder(ctx: Context, prayerTimeReminderExtras: Bundle) {
            val intent = Intent(ctx, PrayerTimeReminderService::class.java).apply {
                putExtras(prayerTimeReminderExtras)
                action = actionRemind
            }

            ctx.startService(intent)
        }

        fun setReminderAlarm(ctx: Context, reminder: PrayerTimeReminder) {
            Log.debug(PrayerTimeReminderService::class.java, "Scheduling prayer time reminder for '$reminder'...")

            val alarmManager = ctx.getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(ctx, PrayerTimeReminderBroadcastReceiver::class.java).apply {
                putExtras(reminder.toBundle())
                action = PrayerTimeReminderService.actionRemind
            }
            val operation = PendingIntent.getBroadcast(ctx, reminder.index, intent, 0)

            alarmManager[AlarmManager.RTC_WAKEUP, reminder.time.toDateTimeToday().millis] = operation
        }

        fun setScheduler(ctx: Context) {
            val intent = Intent(ctx, PrayerTimeReminderService::class.java).apply {
                action = actionSchedule
            }

            ctx.startService(intent)
        }

        fun setSchedulerAlarm(ctx: Context) {
            Log.debug(PrayerTimeReminderService::class.java, "Scheduling an alarm to reschedule prayer time reminders tomorrow...")

            val alarmManager = ctx.getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(ctx, PrayerTimeReminderBroadcastReceiver::class.java).apply {
                action = actionSchedule
            }
            val operation = PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            val schedulerTime = DateTime.now().withTimeAtStartOfDay().plusDays(1).plusSeconds(1)

            alarmManager[AlarmManager.RTC_WAKEUP, schedulerTime.millis] = operation
        }
    }
}