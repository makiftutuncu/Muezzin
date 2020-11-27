package com.mehmetakiftutuncu.muezzin.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.models.WidgetType
import com.mehmetakiftutuncu.muezzin.services.WidgetUpdaterService

sealed class PrayerTimesWidget: AppWidgetProvider() {
    override fun onReceive(ctx: Context, intent: Intent?) {
        val type =
            intent?.action?.takeIf { it.startsWith(updateAction) }?.split("\\.".toRegex())?.lastOrNull()?.let {
                WidgetType.valueOf(it)
            }

        if (type == null) {
            super.onReceive(ctx, intent)
        } else {
            update(ctx, type)
        }
    }

    override fun onEnabled(ctx: Context) {
        super.onEnabled(ctx)
        updateAllWidgets(ctx)
    }

    override fun onDisabled(ctx: Context) {
        super.onDisabled(ctx)
        cancelAllWidgets(ctx)
    }

    companion object {
        private const val updateAction = "com.mehmetakiftutuncu.muezzin.services.PrayerTimesWidget.UPDATE"

        fun updateAllWidgets(ctx: Context) =
            WidgetType.values().forEach { scheduleUpdates(ctx, it) }

        fun cancelUpdates(ctx: Context, type: WidgetType) {
            val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val updatePendingIntent = getPendingIntent(ctx, type)
            alarmManager.cancel(updatePendingIntent)
        }

        private fun cancelAllWidgets(ctx: Context) =
            WidgetType.values().forEach { cancelUpdates(ctx, it) }

        private fun update(ctx: Context, type: WidgetType) =
            getWidgetIds(ctx, type).forEach {
                WidgetUpdaterService.start(ctx, type, it)
            }

        private fun scheduleUpdates(ctx: Context, type: WidgetType) {
            if (hasAnyWidget(ctx, type)) {
                val interval = updateIntervalInSeconds(type) * 1000L
                Log.debug(PrayerTimesWidget::class.java, "scheduleUpdates() for widget '$this' repeating every '$interval' ms")

                val alarmManager = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val updatePendingIntent = getPendingIntent(ctx, type)

                alarmManager.cancel(updatePendingIntent)
                alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), interval, updatePendingIntent)
            }
        }

        private fun classOf(type: WidgetType): Class<*> =
            when (type) {
                WidgetType.Big        -> BigPrayerTimesWidget::class.java
                WidgetType.Horizontal -> HorizontalPrayerTimesWidget::class.java
                WidgetType.Vertical   -> VerticalPrayerTimesWidget::class.java
            }

        private fun updateIntervalInSeconds(type: WidgetType): Int =
            when (type) {
                WidgetType.Big        -> 3
                WidgetType.Horizontal -> 10
                WidgetType.Vertical   -> 10
            }

        private fun getWidgetIds(ctx: Context, type: WidgetType): Array<Int> =
            AppWidgetManager.getInstance(ctx)
                            .getAppWidgetIds(ComponentName(ctx.packageName, classOf(type).name))
                            .toTypedArray()

        private fun hasAnyWidget(ctx: Context, type: WidgetType): Boolean =
            getWidgetIds(ctx, type).isNotEmpty()

        private fun getPendingIntent(ctx: Context, type: WidgetType) =
            PendingIntent.getBroadcast(
                ctx,
                0,
                Intent(ctx, classOf(type)).apply { action = "$updateAction.$type" },
                0
            )
    }
}

class BigPrayerTimesWidget: PrayerTimesWidget()

class HorizontalPrayerTimesWidget: PrayerTimesWidget()

class VerticalPrayerTimesWidget: PrayerTimesWidget()
