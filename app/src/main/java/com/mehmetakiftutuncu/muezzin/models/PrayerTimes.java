package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.utilities.Cache;
import com.mehmetakiftutuncu.muezzin.utilities.FileUtils;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.StringUtils;
import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.mehmetakiftutuncu.muezzin.utilities.option.Some;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.chrono.IslamicChronology;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PrayerTimes {
    public static final String TAG = "PrayerTimes";

    public final DateTime dayDate;
    public final DateTime hijriDayDate;
    public final DateTime fajr;
    public final DateTime shuruq;
    public final DateTime dhuhr;
    public final DateTime asr;
    public final DateTime maghrib;
    public final DateTime isha;
    public final DateTime qibla;

    public static String fileName(int countryId, int cityId, Option<Integer> districtId) {
        return TAG + "." + countryId + "." + cityId + "." + (districtId.isDefined ? districtId.get() : "None");
    }

    public PrayerTimes(DateTime dayDate, DateTime fajr, DateTime shuruq, DateTime dhuhr, DateTime asr, DateTime maghrib, DateTime isha, DateTime qibla) {
        this.dayDate    = dayDate;
        this.fajr       = fajr;
        this.shuruq     = shuruq;
        this.dhuhr      = dhuhr;
        this.asr        = asr;
        this.maghrib    = maghrib;
        this.isha       = isha;
        this.qibla      = qibla;

        this.hijriDayDate = this.dayDate.withChronology(IslamicChronology.getInstance(DateTimeZone.UTC));
    }

    public PrayerTimes(long dayDate, long fajr, long shuruq, long dhuhr, long asr, long maghrib, long isha, long qibla) {
        this(new DateTime(dayDate, DateTimeZone.UTC), new DateTime(fajr, DateTimeZone.UTC), new DateTime(shuruq, DateTimeZone.UTC), new DateTime(dhuhr, DateTimeZone.UTC), new DateTime(asr, DateTimeZone.UTC), new DateTime(maghrib, DateTimeZone.UTC), new DateTime(isha, DateTimeZone.UTC), new DateTime(qibla, DateTimeZone.UTC));
    }

    public static Option<ArrayList<PrayerTimes>> load(int countryId, int cityId, Option<Integer> districtId) {
        Log.info(TAG, "Loading prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + "...");

        Option<ArrayList<PrayerTimes>> fromCache = Cache.PrayerTimes.getList(countryId, cityId, districtId);

        if (fromCache.isDefined) {
            return fromCache;
        }

        if (FileUtils.dataPath.isEmpty) {
            Log.error(TAG, "Failed to load prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + ", data path is None!");

            return new None<>();
        }

        Option<String> data = FileUtils.readFile(fileName(countryId, cityId, districtId));

        if (data.isEmpty) {
            return new None<>();
        }

        if (StringUtils.isEmpty(data.get())) {
            Log.error(TAG, "Failed to load prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + ", loaded data are None or empty!");

            return new None<>();
        }

        try {
            ArrayList<PrayerTimes> prayerTimesList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(data.get());

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Option<PrayerTimes> prayerTimes = fromJson(jsonObject);

                if (prayerTimes.isDefined) {
                    prayerTimesList.add(prayerTimes.get());
                }
            }

            Cache.PrayerTimes.setList(countryId, cityId, districtId, prayerTimesList);

            return new Some<>(prayerTimesList);
        } catch (Throwable t) {
            Log.error(TAG, "Failed to load prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + ", cannot construct prayer times from " + data.get() + "!", t);

            return new None<>();
        }
    }

    public static boolean save(ArrayList<PrayerTimes> prayerTimesList, int countryId, int cityId, Option<Integer> districtId) {
        Log.info(TAG, "Saving prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + "...");

        if (FileUtils.dataPath.isEmpty) {
            Log.error(TAG, "Failed to save prayer times for country " + countryId + ", city " + cityId + " and district " + districtId + ", data path is None!");

            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("[");

        for (int i = 0, size = prayerTimesList.size(); i < size; i++) {
            Option<JSONObject> json = prayerTimesList.get(i).toJson();

            if (json.isDefined) {
                stringBuilder.append(json.toString());

                if (i != size - 1) {
                    stringBuilder.append(", ");
                }
            }
        }

        stringBuilder.append("]");

        boolean result = FileUtils.writeFile(stringBuilder.toString(), fileName(countryId, cityId, districtId));

        if (result) {
            Cache.PrayerTimes.setList(countryId, cityId, districtId, prayerTimesList);
        }

        return result;
    }

    public Option<JSONObject> toJson() {
        try {
            JSONObject result = new JSONObject();

            result.put("dayDate", dayDate.getMillis());
            result.put("fajr",    fajr.getMillis());
            result.put("shuruq",  shuruq.getMillis());
            result.put("dhuhr",   dhuhr.getMillis());
            result.put("asr",     asr.getMillis());
            result.put("maghrib", maghrib.getMillis());
            result.put("isha",    isha.getMillis());
            result.put("qibla",   qibla.getMillis());

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

    public DateTime getNextPrayerTime() {
        DateTime now = DateTime.now().withZoneRetainFields(DateTimeZone.UTC);

        if (now.isBefore(fajr)) {
            return fajr;
        } else if (now.isBefore(shuruq)) {
            return shuruq;
        } else if (now.isBefore(dhuhr)) {
            return dhuhr;
        } else if (now.isBefore(asr)) {
            return asr;
        } else if (now.isBefore(maghrib)) {
            return maghrib;
        } else if (now.isBefore(isha)) {
            return isha;
        } else {
            /* After isha, so next day's fajr is next prayer time.
             * I just assume time fajr time will be the same next day too,
             * HOWEVER it may/will vary a few minutes. It is still better than
             * not knowing the time at all. */
            return fajr.plusDays(1);
        }
    }

    public String getNextPrayerTimeName(Context context) {
        DateTime now = DateTime.now().withZoneRetainFields(DateTimeZone.UTC);

        if (now.isBefore(fajr)) {
            return context.getString(R.string.prayerTime_fajr);
        } else if (now.isBefore(shuruq)) {
            return context.getString(R.string.prayerTime_shuruq);
        } else if (now.isBefore(dhuhr)) {
            return context.getString(R.string.prayerTime_dhuhr);
        } else if (now.isBefore(asr)) {
            return context.getString(R.string.prayerTime_asr);
        } else if (now.isBefore(maghrib)) {
            return context.getString(R.string.prayerTime_maghrib);
        } else if (now.isBefore(isha)) {
            return context.getString(R.string.prayerTime_isha);
        } else {
            // Same logic as in getNextPrayerTime(), return fajr.
            return context.getString(R.string.prayerTime_fajr);
        }
    }

    @Override public String toString() {
        return String.format(
            "{\"dayDate\": %d, \"fajr\": %d, \"shuruq\": %d, \"dhuhr\": %d, \"asr\": %d, \"maghrib\": %d, \"isha\": %d, \"qibla\": %d}",
            dayDate.getMillis(), fajr.getMillis(), shuruq.getMillis(), dhuhr.getMillis(), asr.getMillis(), maghrib.getMillis(), isha.getMillis(), qibla.getMillis()
        );
    }
}
