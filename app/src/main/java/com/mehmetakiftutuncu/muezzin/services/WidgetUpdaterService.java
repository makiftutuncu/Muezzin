package com.mehmetakiftutuncu.muezzin.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;

import com.github.mehmetakiftutuncu.toolbelt.Log;
import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.activities.PrayerTimesActivity;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimesOfDay;
import com.mehmetakiftutuncu.muezzin.models.WidgetType;
import com.mehmetakiftutuncu.muezzin.utilities.Pref;
import com.mehmetakiftutuncu.muezzin.utilities.RemainingTime;
import com.mehmetakiftutuncu.muezzin.widgetproviders.PrayerTimesWidgetBase;

import org.joda.time.LocalTime;

/**
 * Created by akif on 08/07/16.
 */
public class WidgetUpdaterService extends IntentService {
    public WidgetUpdaterService() {
        super("WidgetUpdaterService");
    }

    public static void startServiceToUpdateWidget(Context context, WidgetType widgetType, int widgetId) {
        Intent intent = new Intent(context, WidgetUpdaterService.class);
        intent.putExtra("widgetType", widgetType.ordinal()).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);

        context.startService(intent);
    }

    @Override protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Bundle extras         = intent.getExtras();
            WidgetType widgetType = WidgetType.values()[extras.getInt("widgetType", 0)];
            int widgetId          = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

            update(widgetType, widgetId);
        }
    }

    private void update(WidgetType widgetType, int widgetId) {
        Log.debug(getClass(), "Updating widget '%s' with id '%d'...", widgetType, widgetId);

        // Get RemoteViews and AppWidgetManager to work with
        RemoteViews remoteViews           = new RemoteViews(getPackageName(), widgetType.layoutId);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        // A pending intent to launch prayer times activity
        Intent intent               = new Intent(this, PrayerTimesActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Optional<Place> maybePlace = Pref.Places.getCurrentPlace(this);

        Optional<PrayerTimesOfDay> maybePrayerTimesForToday = maybePlace.isDefined() ? PrayerTimesOfDay.getPrayerTimesForToday(this, maybePlace.get()) : Optional.empty();

        // When clicked on the root item in widget layout, which is basically anywhere on the widget, launch the pending intent
        remoteViews.setOnClickPendingIntent(R.id.linearLayout_widgetLayout, pendingIntent);

        // Initialize prayer times texts
        if (maybePlace.isDefined()) {
            Place place = maybePlace.get();

            if (maybePrayerTimesForToday.isDefined()) {
                PrayerTimesOfDay prayerTimes = maybePrayerTimesForToday.get();

                switch (widgetType) {
                    case PRAYER_TIMES_HORIZONTAL:
                        updatePlaceName(remoteViews, place, true);
                        updatePrayerTimes(remoteViews, prayerTimes);
                        break;

                    case PRAYER_TIMES_VERTICAL:
                        updatePlaceName(remoteViews, place, false);
                        updatePrayerTimes(remoteViews, prayerTimes);
                        break;

                    case PRAYER_TIMES_BIG:
                        updatePlaceName(remoteViews, place, true);
                        updatePrayerTimes(remoteViews, prayerTimes);
                        updateRemainingTime(remoteViews, prayerTimes);
                        break;
                }
            } else {
                PrayerTimesWidgetBase.cancelUpdates(this, widgetType);
            }
        } else {
            PrayerTimesWidgetBase.cancelUpdates(this, widgetType);
        }

        // Apply all these updates above to the widget
        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }

    private void updatePlaceName(RemoteViews remoteViews, Place place, boolean isFullNameRequired) {
        remoteViews.setTextViewText(R.id.textView_prayerTimes_placeName, place.getPlaceName(getBaseContext(), isFullNameRequired).getOrElse(getString(R.string.applicationName)));
    }

    private void updatePrayerTimes(RemoteViews remoteViews, PrayerTimesOfDay prayerTimes) {
        int defaultTextColor = ContextCompat.getColor(this, android.R.color.secondary_text_light);
        int greenTextColor   = ContextCompat.getColor(this, R.color.colorPrimary);
        int redTextColor     = ContextCompat.getColor(this, R.color.red);

        LocalTime nextPrayerTime = prayerTimes.nextPrayerTime();
        LocalTime remaining  = RemainingTime.to(nextPrayerTime);

        boolean isRemainingLessThan45Minutes = remaining.getHourOfDay() == 0 && remaining.getMinuteOfHour() < 45;

        remoteViews.setTextViewText(R.id.textView_prayerTimes_fajrTime,    prayerTimes.fajr.toString(PrayerTimesOfDay.TIME_FORMATTER));
        remoteViews.setTextViewText(R.id.textView_prayerTimes_shuruqTime,  prayerTimes.shuruq.toString(PrayerTimesOfDay.TIME_FORMATTER));
        remoteViews.setTextViewText(R.id.textView_prayerTimes_dhuhrTime,   prayerTimes.dhuhr.toString(PrayerTimesOfDay.TIME_FORMATTER));
        remoteViews.setTextViewText(R.id.textView_prayerTimes_asrTime,     prayerTimes.asr.toString(PrayerTimesOfDay.TIME_FORMATTER));
        remoteViews.setTextViewText(R.id.textView_prayerTimes_maghribTime, prayerTimes.maghrib.toString(PrayerTimesOfDay.TIME_FORMATTER));
        remoteViews.setTextViewText(R.id.textView_prayerTimes_ishaTime,    prayerTimes.isha.toString(PrayerTimesOfDay.TIME_FORMATTER));

        remoteViews.setTextColor(R.id.textView_prayerTimes_fajrTimeName,    defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_shuruqTimeName,  defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_dhuhrTimeName,   defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_asrTimeName,     defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_maghribTimeName, defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_ishaTimeName,    defaultTextColor);

        remoteViews.setTextColor(R.id.textView_prayerTimes_fajrTime,    defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_shuruqTime,  defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_dhuhrTime,   defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_asrTime,     defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_maghribTime, defaultTextColor);
        remoteViews.setTextColor(R.id.textView_prayerTimes_ishaTime,    defaultTextColor);

        int color = isRemainingLessThan45Minutes ? redTextColor : greenTextColor;

        switch (prayerTimes.nextPrayerTimeType()) {
            case Fajr:
                remoteViews.setTextColor(R.id.textView_prayerTimes_fajrTimeName, color);
                remoteViews.setTextColor(R.id.textView_prayerTimes_fajrTime, color);
                break;

            case Shuruq:
                remoteViews.setTextColor(R.id.textView_prayerTimes_shuruqTimeName, color);
                remoteViews.setTextColor(R.id.textView_prayerTimes_shuruqTime, color);
                break;

            case Dhuhr:
                remoteViews.setTextColor(R.id.textView_prayerTimes_dhuhrTimeName, color);
                remoteViews.setTextColor(R.id.textView_prayerTimes_dhuhrTime, color);
                break;

            case Asr:
                remoteViews.setTextColor(R.id.textView_prayerTimes_asrTimeName, color);
                remoteViews.setTextColor(R.id.textView_prayerTimes_asrTime, color);
                break;

            case Maghrib:
                remoteViews.setTextColor(R.id.textView_prayerTimes_maghribTimeName, color);
                remoteViews.setTextColor(R.id.textView_prayerTimes_maghribTime, color);
                break;

            case Isha:
                remoteViews.setTextColor(R.id.textView_prayerTimes_ishaTimeName, color);
                remoteViews.setTextColor(R.id.textView_prayerTimes_ishaTime, color);
                break;
        }
    }

    private void updateRemainingTime(RemoteViews remoteViews, PrayerTimesOfDay prayerTimes) {
        int defaultTextColor = ContextCompat.getColor(this, android.R.color.secondary_text_light);
        int redTextColor     = ContextCompat.getColor(this, R.color.red);

        LocalTime nextPrayerTime  = prayerTimes.nextPrayerTime();
        String nextPrayerTimeName = PrayerTimesOfDay.prayerTimeLocalizedName(this, prayerTimes.nextPrayerTimeType());

        LocalTime remaining  = RemainingTime.to(nextPrayerTime);
        String remainingTime = remaining.toString(RemainingTime.FORMATTER_WITHOUT_SECONDS);

        boolean isRemainingLessThan45Minutes = remaining.getHourOfDay() == 0 && remaining.getMinuteOfHour() < 45;

        int color = isRemainingLessThan45Minutes ? redTextColor : defaultTextColor;

        remoteViews.setViewVisibility(R.id.linearLayout_prayerTimes_remainingTime, View.VISIBLE);

        remoteViews.setTextViewText(R.id.textView_prayerTimes_remainingTimeInfo, getString(R.string.prayerTimes_cardTitle_remainingTime, nextPrayerTimeName));
        remoteViews.setTextViewText(R.id.textView_prayerTimes_remainingTime, remainingTime);

        remoteViews.setTextColor(R.id.textView_prayerTimes_remainingTimeInfo, color);
        remoteViews.setTextColor(R.id.textView_prayerTimes_remainingTime, color);
    }
}
