package com.mehmetakiftutuncu.muezzin.widgetproviders;

import com.mehmetakiftutuncu.muezzin.models.WidgetType;

/**
 * Created by akif on 12/06/16.
 */
public class PrayerTimesHorizontalWidget extends PrayerTimesWidgetBase {
    public static final int UPDATE_INTERVAL_IN_SECONDS = 10;

    @Override protected WidgetType widgetType() {
        return WidgetType.PRAYER_TIMES_HORIZONTAL;
    }

    @Override public int updateIntervalInSeconds() {
        return UPDATE_INTERVAL_IN_SECONDS;
    }
}
