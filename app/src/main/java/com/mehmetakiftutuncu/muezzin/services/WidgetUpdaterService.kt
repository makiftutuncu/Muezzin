package com.mehmetakiftutuncu.muezzin.services

import android.app.IntentService
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.activities.PrayerTimesActivity
import com.mehmetakiftutuncu.muezzin.models.Place
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeType
import com.mehmetakiftutuncu.muezzin.models.PrayerTimesOfDay
import com.mehmetakiftutuncu.muezzin.models.WidgetType
import com.mehmetakiftutuncu.muezzin.repositories.PlaceRepository
import com.mehmetakiftutuncu.muezzin.repositories.PrayerTimesOfDayRepository
import com.mehmetakiftutuncu.muezzin.utilities.Pref
import com.mehmetakiftutuncu.muezzin.utilities.RemainingTime
import com.mehmetakiftutuncu.muezzin.widget.PrayerTimesWidget

class WidgetUpdaterService: IntentService("WidgetUpdaterService") {
    private val ctx: Context by lazy { this }

    override fun onHandleIntent(intent: Intent?) {
        intent?.extras?.also {
            update(
                WidgetType.values()[it.getInt("widgetType", 0)],
                it.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID)
            )
        }
    }

    private fun update(type: WidgetType, id: Int) {
        Log.debug(javaClass, "Updating widget '$type' with id '$id'...")

        // Get RemoteViews and AppWidgetManager to work with
        val remoteViews = RemoteViews(packageName, type.layoutId)
        val appWidgetManager = AppWidgetManager.getInstance(ctx)

        // A pending intent to launch prayer times activity
        val intent = Intent(ctx, PrayerTimesActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 0)

        // When clicked on the root item in widget layout,
        // which is basically anywhere on the widget, launch the pending intent
        remoteViews.setOnClickPendingIntent(R.id.linearLayout_widgetLayout, pendingIntent)

        val place = Pref.Places.getCurrentPlace(ctx)

        if (place == null) {
            PrayerTimesWidget.cancelUpdates(ctx, type)
        } else {
            val times = PrayerTimesOfDayRepository.getForToday(ctx, place)

            if (times == null) {
                PrayerTimesWidget.cancelUpdates(ctx, type)
            } else {
                // Initialize prayer times texts
                when (type) {
                    WidgetType.Horizontal -> {
                        updatePlaceName(remoteViews, place, true)
                        updatePrayerTimes(remoteViews, times)
                    }

                    WidgetType.Vertical -> {
                        updatePlaceName(remoteViews, place, false)
                        updatePrayerTimes(remoteViews, times)
                    }

                    WidgetType.Big -> {
                        updatePlaceName(remoteViews, place, true)
                        updatePrayerTimes(remoteViews, times)
                        updateRemainingTime(remoteViews, times)
                    }
                }
            }
        }

        // Apply all these updates above to the widget
        appWidgetManager.updateAppWidget(id, remoteViews)
    }

    private fun updatePlaceName(remoteViews: RemoteViews, place: Place, fullNameRequired: Boolean) {
        PlaceRepository.getName(baseContext, place, fullNameRequired)?.let {
            remoteViews.setTextViewText(R.id.textView_prayerTimes_placeName, it)
        }
    }

    private fun updatePrayerTimes(remoteViews: RemoteViews, prayerTimes: PrayerTimesOfDay) {
        val defaultTextColor = ContextCompat.getColor(ctx, android.R.color.secondary_text_light)
        val greenTextColor = ContextCompat.getColor(ctx, R.color.colorPrimary)
        val redTextColor = ContextCompat.getColor(ctx, R.color.red)
        val nextPrayerTime = prayerTimes.nextPrayerTime()
        val remaining = RemainingTime.to(nextPrayerTime)
        val lessThan45Minutes = remaining.hourOfDay == 0 && remaining.minuteOfHour < 45

        remoteViews.apply {
            setTextViewText(R.id.textView_prayerTimes_fajrTime, prayerTimes.fajr.toString(PrayerTimesOfDay.timeFormatter))
            setTextViewText(R.id.textView_prayerTimes_shuruqTime, prayerTimes.shuruq.toString(PrayerTimesOfDay.timeFormatter))
            setTextViewText(R.id.textView_prayerTimes_dhuhrTime, prayerTimes.dhuhr.toString(PrayerTimesOfDay.timeFormatter))
            setTextViewText(R.id.textView_prayerTimes_asrTime, prayerTimes.asr.toString(PrayerTimesOfDay.timeFormatter))
            setTextViewText(R.id.textView_prayerTimes_maghribTime, prayerTimes.maghrib.toString(PrayerTimesOfDay.timeFormatter))
            setTextViewText(R.id.textView_prayerTimes_ishaTime, prayerTimes.isha.toString(PrayerTimesOfDay.timeFormatter))

            setTextColor(R.id.textView_prayerTimes_fajrTimeName, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_shuruqTimeName, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_dhuhrTimeName, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_asrTimeName, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_maghribTimeName, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_ishaTimeName, defaultTextColor)

            setTextColor(R.id.textView_prayerTimes_fajrTime, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_shuruqTime, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_dhuhrTime, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_asrTime, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_maghribTime, defaultTextColor)
            setTextColor(R.id.textView_prayerTimes_ishaTime, defaultTextColor)

            val color = if (lessThan45Minutes) redTextColor else greenTextColor

            when (prayerTimes.nextPrayerTimeType()) {
                PrayerTimeType.Fajr -> {
                    setTextColor(R.id.textView_prayerTimes_fajrTimeName, color)
                    setTextColor(R.id.textView_prayerTimes_fajrTime, color)
                }

                PrayerTimeType.Shuruq -> {
                    setTextColor(R.id.textView_prayerTimes_shuruqTimeName, color)
                    setTextColor(R.id.textView_prayerTimes_shuruqTime, color)
                }

                PrayerTimeType.Dhuhr -> {
                    setTextColor(R.id.textView_prayerTimes_dhuhrTimeName, color)
                    setTextColor(R.id.textView_prayerTimes_dhuhrTime, color)
                }

                PrayerTimeType.Asr -> {
                    setTextColor(R.id.textView_prayerTimes_asrTimeName, color)
                    setTextColor(R.id.textView_prayerTimes_asrTime, color)
                }

                PrayerTimeType.Maghrib -> {
                    setTextColor(R.id.textView_prayerTimes_maghribTimeName, color)
                    setTextColor(R.id.textView_prayerTimes_maghribTime, color)
                }

                PrayerTimeType.Isha -> {
                    setTextColor(R.id.textView_prayerTimes_ishaTimeName, color)
                    setTextColor(R.id.textView_prayerTimes_ishaTime, color)
                }

                else -> {}
            }
        }
    }

    private fun updateRemainingTime(remoteViews: RemoteViews, prayerTimes: PrayerTimesOfDay) {
        val defaultTextColor = ContextCompat.getColor(ctx, android.R.color.secondary_text_light)
        val redTextColor = ContextCompat.getColor(ctx, R.color.red)
        val nextPrayerTime = prayerTimes.nextPrayerTime()
        val nextPrayerTimeName = PrayerTimesOfDay.localizedName(ctx, prayerTimes.nextPrayerTimeType())
        val remaining = RemainingTime.to(nextPrayerTime)
        val remainingTime = remaining.toString(RemainingTime.formatterWithoutSeconds)
        val lessThan45Minutes = remaining.hourOfDay == 0 && remaining.minuteOfHour < 45

        val color = if (lessThan45Minutes) redTextColor else defaultTextColor

        val remainingTimeInfo = getString(R.string.prayerTimes_cardTitle_remainingTime, nextPrayerTimeName)

        remoteViews.apply {
            setViewVisibility(R.id.linearLayout_prayerTimes_remainingTime, View.VISIBLE)
            setTextViewText(R.id.textView_prayerTimes_remainingTimeInfo, remainingTimeInfo)
            setTextViewText(R.id.textView_prayerTimes_remainingTime, remainingTime)
            setTextColor(R.id.textView_prayerTimes_remainingTimeInfo, color)
            setTextColor(R.id.textView_prayerTimes_remainingTime, color)
        }
    }

    companion object {
        fun start(ctx: Context, type: WidgetType, id: Int) {
            Intent(ctx, WidgetUpdaterService::class.java).apply {
                putExtra("widgetType", type.ordinal)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            }.also {
                ctx.startService(it)
            }
        }
    }
}