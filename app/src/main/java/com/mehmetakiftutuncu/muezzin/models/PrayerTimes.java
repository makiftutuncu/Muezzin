package com.mehmetakiftutuncu.muezzin.models;

import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.mehmetakiftutuncu.muezzin.utilities.option.Some;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PrayerTimes {
    private static final String TAG = "PrayerTimes";

    public final DateTime dayDate;
    public final DateTime fajr;
    public final DateTime shuruq;
    public final DateTime dhuhr;
    public final DateTime asr;
    public final DateTime maghrib;
    public final DateTime isha;
    public final DateTime qibla;

    public PrayerTimes(DateTime dayDate, DateTime fajr, DateTime shuruq, DateTime dhuhr, DateTime asr, DateTime maghrib, DateTime isha, DateTime qibla) {
        this.dayDate    = dayDate;
        this.fajr       = fajr;
        this.shuruq     = shuruq;
        this.dhuhr      = dhuhr;
        this.asr        = asr;
        this.maghrib    = maghrib;
        this.isha       = isha;
        this.qibla      = qibla;
    }

    public PrayerTimes(long dayDate, long fajr, long shuruq, long dhuhr, long asr, long maghrib, long isha, long qibla) {
        this.dayDate    = new DateTime(dayDate, DateTimeZone.UTC);
        this.fajr       = new DateTime(fajr,    DateTimeZone.UTC);
        this.shuruq     = new DateTime(shuruq,  DateTimeZone.UTC);
        this.dhuhr      = new DateTime(dhuhr,   DateTimeZone.UTC);
        this.asr        = new DateTime(asr,     DateTimeZone.UTC);
        this.maghrib    = new DateTime(maghrib, DateTimeZone.UTC);
        this.isha       = new DateTime(isha,    DateTimeZone.UTC);
        this.qibla      = new DateTime(qibla,   DateTimeZone.UTC);
    }

    public Option<JSONObject> toJson() {
        try {
            JSONObject result = new JSONObject();

            result.put("dayDate", dayDate);
            result.put("fajr",    fajr);
            result.put("shuruq",  shuruq);
            result.put("dhuhr",   dhuhr);
            result.put("asr",     asr);
            result.put("maghrib", maghrib);
            result.put("isha",    isha);
            result.put("qibla",   qibla);

            return new Some<>(result);
        } catch (Throwable t) {
            Log.error(TAG, "Failed to convert " + toString() + " to Json!", t);

            return new None<>();
        }
    }

    public static Option<PrayerTimes> fromJson(JSONObject json) {
        try {
            long dayDate = json.getLong("dayDate");
            long fajr    = json.getLong("fajr");
            long shuruq  = json.getLong("shuruq");
            long dhuhr   = json.getLong("dhuhr");
            long asr     = json.getLong("asr");
            long maghrib = json.getLong("maghrib");
            long isha    = json.getLong("isha");
            long qibla   = json.getLong("qibla");

            return new Some<>(new PrayerTimes(dayDate, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla));
        } catch (Throwable t) {
            Log.error(TAG, "Failed to create prayer times from Json " + json.toString() + "!", t);

            return new None<>();
        }
    }

    public static Option<ArrayList<PrayerTimes>> fromJsonArray(JSONArray jsonArray) {
        try {
            ArrayList<PrayerTimes> prayerTimesList = new ArrayList<>();

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Option<PrayerTimes> prayerTimes = fromJson(jsonObject);

                if (prayerTimes.isDefined) {
                    prayerTimesList.add(prayerTimes.get());
                }
            }

            return new Some<>(prayerTimesList);
        } catch (Throwable t) {
            Log.error(TAG, "Failed to create prayer times list from Json array " + jsonArray.toString() + "!", t);

            return new None<>();
        }
    }

    @Override public String toString() {
        return String.format(
            "{\"dayDate\": %d, \"fajr\": %d, \"shuruq\": %d, \"dhuhr\": %d, \"asr\": %d, \"maghrib\": %d, \"isha\": %d, \"qibla\": %d}",
            dayDate.getMillis(), fajr.getMillis(), shuruq.getMillis(), dhuhr.getMillis(), asr.getMillis(), maghrib.getMillis(), isha.getMillis(), qibla.getMillis()
        );
    }
}
