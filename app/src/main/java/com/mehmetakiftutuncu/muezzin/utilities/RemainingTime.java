package com.mehmetakiftutuncu.muezzin.utilities;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Created by akif on 17/09/16.
 */
public final class RemainingTime {
    public static final DateTimeFormatter FORMATTER                 = DateTimeFormat.forPattern("HH:mm:ss");
    public static final DateTimeFormatter FORMATTER_WITHOUT_SECONDS = DateTimeFormat.forPattern("HH:mm");

    private static final int HOURS_IN_DAY      = 24;
    private static final int MINUTES_IN_HOUR   = 60;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int SECONDS_IN_HOUR   = 3600;

    public static LocalTime to(LocalTime to) {
        LocalTime now = LocalTime.now();

        int toSeconds            = (to.getHourOfDay() * SECONDS_IN_HOUR)  + (to.getMinuteOfHour() * 60)  + to.getSecondOfMinute();
        int nowSeconds           = (now.getHourOfDay() * SECONDS_IN_HOUR) + (now.getMinuteOfHour() * 60) + now.getSecondOfMinute();
        int nowToMidnightSeconds = ((HOURS_IN_DAY - now.getHourOfDay() - 1) * SECONDS_IN_HOUR) + ((MINUTES_IN_HOUR - now.getMinuteOfHour() - 1) * 60) + (SECONDS_IN_MINUTE - now.getSecondOfMinute() - 1);

        int diffSeconds = (toSeconds > nowSeconds) ? (toSeconds - nowSeconds) : (toSeconds + nowToMidnightSeconds);

        int seconds = diffSeconds % SECONDS_IN_MINUTE;
        int minutes = ((diffSeconds - seconds) / SECONDS_IN_MINUTE) % SECONDS_IN_MINUTE;
        int hours   = ((diffSeconds - (SECONDS_IN_MINUTE * minutes) - seconds) / SECONDS_IN_HOUR) % HOURS_IN_DAY;

        return new LocalTime(hours, minutes, seconds, 0);
    }
}
