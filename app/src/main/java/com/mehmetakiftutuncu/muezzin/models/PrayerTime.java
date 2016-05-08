package com.mehmetakiftutuncu.muezzin.models;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

/**
 * Created by akif on 08/05/16.
 */
@SuppressLint("DefaultLocale")
public class PrayerTime {
    public final int countryId;
    public final int cityId;
    public final Optional<Integer> districtId;
    public final DateTime day;
    public final DateTime fajr;
    public final DateTime shuruq;
    public final DateTime dhuhr;
    public final DateTime asr;
    public final DateTime maghrib;
    public final DateTime isha;
    public final DateTime qibla;

    private static final String dateFormat = "YYYY.MM.dd";
    private static final String timeFormat = "HH:mm";

    public PrayerTime(int countryId, int cityId, Optional<Integer> districtId, long day, long fajr, long shuruq, long dhuhr, long asr, long maghrib, long isha, long qibla) {
        this.countryId = countryId;
        this.cityId = cityId;
        this.districtId = districtId;
        this.day = new DateTime(day, DateTimeZone.UTC);
        this.fajr = new DateTime(fajr, DateTimeZone.UTC);
        this.shuruq = new DateTime(shuruq, DateTimeZone.UTC);
        this.dhuhr = new DateTime(dhuhr, DateTimeZone.UTC);
        this.asr = new DateTime(asr, DateTimeZone.UTC);
        this.maghrib = new DateTime(maghrib, DateTimeZone.UTC);
        this.isha = new DateTime(isha, DateTimeZone.UTC);
        this.qibla = new DateTime(qibla, DateTimeZone.UTC);
    }

    @NonNull public String toJson() {
        String districtIdString = districtId.isDefined ? districtId.toString() : null;

        return String.format(
                "{\"countryId\":%d,\"cityId\":%d, \"districtId\":%s, \"day\":\"%s\", \"fajr\":\"%s\", \"shuruq\":\"%s\", \"dhuhr\":\"%s\",\"asr\":\"%s\",\"maghrib\":\"%s\",\"isha\":\"%s\",\"qibla\":\"%s\"}",
                countryId,
                cityId,
                districtIdString,
                day.toString(dateFormat),
                fajr.toString(timeFormat),
                shuruq.toString(timeFormat),
                dhuhr.toString(timeFormat),
                asr.toString(timeFormat),
                maghrib.toString(timeFormat),
                isha.toString(timeFormat),
                qibla.toString(timeFormat)
        );
    }

    @NonNull public static Optional<PrayerTime> fromJson(int countryId, int cityId, Optional<Integer> districtId, JSONObject json) {
        try {
            long day = json.getLong("dayDate");
            long fajr = json.getLong("fajr");
            long shuruq = json.getLong("shuruq");
            long dhuhr = json.getLong("dhuhr");
            long asr = json.getLong("asr");
            long maghrib = json.getLong("maghrib");
            long isha = json.getLong("isha");
            long qibla = json.getLong("qibla");

            return new Some<>(new PrayerTime(countryId, cityId, districtId, day, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla));
        } catch (Throwable t) {
            Log.error(String.format("Failed to generate prayer times for country '%d', city '%d' and district '%s' from Json '%s'!", countryId, cityId, districtId, json), t, PrayerTime.class, "fromJson");

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }
}
