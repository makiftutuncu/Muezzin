package com.mehmetakiftutuncu.muezzin.widgetproviders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.github.mehmetakiftutuncu.toolbelt.Log;
import com.mehmetakiftutuncu.muezzin.models.WidgetType;
import com.mehmetakiftutuncu.muezzin.services.WidgetUpdaterService;

import java.util.NoSuchElementException;

/**
 * Created by akif on 19/09/16.
 */
abstract public class PrayerTimesWidgetBase extends AppWidgetProvider {
    public static final String UPDATE_ACTION = "com.mehmetakiftutuncu.muezzin.services.WidgetUpdaterService.UPDATE";

    abstract protected WidgetType widgetType();
    abstract public int updateIntervalInSeconds();

    @NonNull public static Class<? extends PrayerTimesWidgetBase> getWidgetProviderByType(WidgetType widgetType) {
        switch (widgetType) {
            case PRAYER_TIMES_BIG:        return PrayerTimesBigWidget.class;
            case PRAYER_TIMES_HORIZONTAL: return PrayerTimesHorizontalWidget.class;
            case PRAYER_TIMES_VERTICAL:   return PrayerTimesVerticalWidget.class;
            default:                      throw new NoSuchElementException();
        }
    }

    public static void updateAllWidgets(Context context) {
        Log.debug(PrayerTimesWidgetBase.class, "updateAllWidgets()");

        try {
            scheduleUpdates(context, WidgetType.PRAYER_TIMES_BIG, PrayerTimesBigWidget.UPDATE_INTERVAL_IN_SECONDS);
            scheduleUpdates(context, WidgetType.PRAYER_TIMES_HORIZONTAL, PrayerTimesHorizontalWidget.UPDATE_INTERVAL_IN_SECONDS);
            scheduleUpdates(context, WidgetType.PRAYER_TIMES_VERTICAL, PrayerTimesVerticalWidget.UPDATE_INTERVAL_IN_SECONDS);
        } catch (Exception e) {
            Log.error(PrayerTimesWidgetBase.class, e, "Failed to update all widgets!");
        }
    }

    public static boolean hasAnyWidgetOf(Context context, WidgetType widgetType) {
        Log.debug(PrayerTimesWidgetBase.class, "hasAnyWidgetOf(%s)", widgetType);

        int[] appWidgetIds = getWidgetIds(context, widgetType);

        return appWidgetIds.length > 0;
    }

    public static int[] getWidgetIds(Context context, WidgetType widgetType) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        String widgetProviderName = getWidgetProviderByType(widgetType).getName();
        ComponentName componentName = new ComponentName(context.getPackageName(), widgetProviderName);

        return appWidgetManager.getAppWidgetIds(componentName);
    }

    protected void updateWidget(Context context, WidgetType widgetType, int widgetId) {
        WidgetUpdaterService.startServiceToUpdateWidget(context, widgetType, widgetId);
    }

    protected void onUpdate(Context context, WidgetType widgetType) {
        Log.debug(getClass(), "onUpdate(context) for widget '%s'", widgetType);

        int[] appWidgetIds = getWidgetIds(context, widgetType);

        for (int i = 0, length = appWidgetIds.length; i < length; i++) {
            updateWidget(context, widgetType, appWidgetIds[i]);
        }
    }

    protected static void scheduleUpdates(Context context, WidgetType widgetType, int updateIntervalInSeconds) {
        if (hasAnyWidgetOf(context, widgetType)) {
            int interval = updateIntervalInSeconds * 1000;

            Log.debug(PrayerTimesWidgetBase.class, "scheduleUpdates() for widget '%s' repeating every '%d' ms", widgetType, interval);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent updatePendingIntent = getUpdatePendingIntent(context, widgetType);

            alarmManager.cancel(updatePendingIntent);
            alarmManager.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(), interval, updatePendingIntent);
        }
    }

    public static void cancelUpdates(Context context, WidgetType widgetType) {
        Log.debug(PrayerTimesWidgetBase.class, "cancelUpdates() for widget '%s'", widgetType);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent updatePendingIntent = getUpdatePendingIntent(context, widgetType);

        alarmManager.cancel(updatePendingIntent);
    }

    protected static PendingIntent getUpdatePendingIntent(Context context, WidgetType widgetType) {
        Log.debug(PrayerTimesWidgetBase.class, "getUpdatePendingIntent() for widget '%s'", widgetType);

        Intent intent = new Intent(context, getWidgetProviderByType(widgetType));
        intent.setAction(UPDATE_ACTION + "." + widgetType.toString());

        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override public void onReceive(Context context, Intent intent) {
        Log.debug(getClass(), "onReceive() for widget '%s'", widgetType());

        if (intent != null && intent.getAction() != null && intent.getAction().startsWith(UPDATE_ACTION)) {
            String[] split = intent.getAction().split("\\.");
            String widgetTypeString = split[split.length - 1];

            WidgetType widgetType = WidgetType.valueOf(widgetTypeString);

            onUpdate(context, widgetType);
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override public void onEnabled(Context context) {
        Log.debug(getClass(), "onEnabled() for widget '%s'", widgetType());

        super.onEnabled(context);

        int[] appWidgetIds = getWidgetIds(context, widgetType());

        for (int i = 0, length = appWidgetIds.length; i < length; i++) {
            updateWidget(context, widgetType(), appWidgetIds[i]);
        }

        scheduleUpdates(context, widgetType(), updateIntervalInSeconds());
    }

    @Override public void onDisabled(Context context) {
        Log.debug(getClass(), "onDisabled() for widget '%s'", widgetType());

        super.onDisabled(context);

        cancelUpdates(context, widgetType());
    }
}
