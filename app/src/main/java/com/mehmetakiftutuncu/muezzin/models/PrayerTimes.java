package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.database.Database;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by akif on 08/05/16.
 */
public class PrayerTimes {
    public final Place place;
    public final DateTime day;
    public final DateTime fajr;
    public final DateTime shuruq;
    public final DateTime dhuhr;
    public final DateTime asr;
    public final DateTime maghrib;
    public final DateTime isha;
    public final DateTime qibla;

    public static final String dateFormat          = "dd MMMM YYYY";
    public static final String timeFormat          = "HH:mm";
    public static final String remainingTimeFormat = "HH:mm:ss";

    public PrayerTimes(int countryId, int cityId, Optional<Integer> districtId, long day, long fajr, long shuruq, long dhuhr, long asr, long maghrib, long isha, long qibla) {
        this(new Place(countryId, cityId, districtId), day, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla);
    }

    public PrayerTimes(Place place, long day, long fajr, long shuruq, long dhuhr, long asr, long maghrib, long isha, long qibla) {
        this.place   = place;
        this.day     = new DateTime(day,     DateTimeZone.UTC);
        this.fajr    = new DateTime(fajr,    DateTimeZone.UTC);
        this.shuruq  = new DateTime(shuruq,  DateTimeZone.UTC);
        this.dhuhr   = new DateTime(dhuhr,   DateTimeZone.UTC);
        this.asr     = new DateTime(asr,     DateTimeZone.UTC);
        this.maghrib = new DateTime(maghrib, DateTimeZone.UTC);
        this.isha    = new DateTime(isha,    DateTimeZone.UTC);
        this.qibla   = new DateTime(qibla,   DateTimeZone.UTC);
    }

    public DateTime nextPrayerTime() {
        DateTime now = DateTime.now().withDate(day.getYear(), day.getMonthOfYear(), day.getDayOfMonth()).withZoneRetainFields(DateTimeZone.UTC);

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

    public String nextPrayerTimeName(Context context) {
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

    public static Optional<PrayerTimes> getPrayerTimesForToday(Context context, Place place) {
        return getPrayerTimesForDay(context, place, DateTime.now().withZoneRetainFields(DateTimeZone.UTC).withTimeAtStartOfDay());
    }

    public static Optional<PrayerTimes> getPrayerTimesForDay(Context context, Place place, DateTime day) {
        try {
            Optional<PrayerTimes> prayerTimes = new None<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            String query;
            if (place.districtId.isDefined) {
                query = String.format(Locale.ENGLISH,
                        "SELECT * FROM %s WHERE %s = %d AND %s = %d AND %s = %d AND %s = %d",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        place.countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        place.cityId,
                        Database.PrayerTimesTable.COLUMN_DISTRICT_ID,
                        place.districtId.get(),
                        Database.PrayerTimesTable.COLUMN_DAY,
                        day.withZoneRetainFields(DateTimeZone.UTC).getMillis()
                );
            } else {
                query = String.format(Locale.ENGLISH,
                        "SELECT * FROM %s WHERE %s = %d AND %s = %d AND %s = %d",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        place.countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        place.cityId,
                        Database.PrayerTimesTable.COLUMN_DAY,
                        day.withZoneRetainFields(DateTimeZone.UTC).getMillis()
                );
            }

            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    prayerTimes = new Some<>(fromCursor(place, cursor));
                }

                cursor.close();
            }

            database.close();

            return prayerTimes;
        } catch (Throwable t) {
            Log.error(PrayerTimes.class, t, "Failed to get prayer times for place '%s' and for day '%s' from database!", place, day);

            return new None<>();
        }
    }

    public static Optional<ArrayList<PrayerTimes>> getAllPrayerTimes(Context context, Place place) {
        try {
            ArrayList<PrayerTimes> allPrayerTimes = new ArrayList<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            String query;
            if (place.districtId.isDefined) {
                query = String.format(Locale.ENGLISH,
                        "SELECT * FROM %s WHERE %s = %d AND %s = %d AND %s = %d ORDER BY %s",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        place.countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        place.cityId,
                        Database.PrayerTimesTable.COLUMN_DISTRICT_ID,
                        place.districtId.get(),
                        Database.PrayerTimesTable.COLUMN_DAY
                );
            } else {
                query = String.format(Locale.ENGLISH,
                        "SELECT * FROM %s WHERE %s = %d AND %s = %d ORDER BY %s",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        place.countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        place.cityId,
                        Database.PrayerTimesTable.COLUMN_DAY
                );
            }

            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        PrayerTimes prayerTimes = fromCursor(place, cursor);

                        allPrayerTimes.add(prayerTimes);

                        cursor.moveToNext();
                    }
                }

                cursor.close();
            }

            database.close();

            return new Some<>(allPrayerTimes);
        } catch (Throwable t) {
            Log.error(PrayerTimes.class, t, "Failed to get all prayer times for place '%s' from database!", place);

            return new None<>();
        }
    }

    public static boolean saveAllPrayerTimes(Context context, Place place, ArrayList<PrayerTimes> prayerTimes) {
        try {
            StringBuilder insertSQLBuilder = new StringBuilder("INSERT INTO ")
                    .append(Database.PrayerTimesTable.TABLE_NAME)
                    .append(" (")
                    .append(Database.PrayerTimesTable.COLUMN_COUNTRY_ID).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_CITY_ID).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_DISTRICT_ID).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_DAY).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_FAJR).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_SHURUQ).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_DHUHR).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_ASR).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_MAGHRIB).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_ISHA).append(", ")
                    .append(Database.PrayerTimesTable.COLUMN_QIBLA)
                    .append(") VALUES ");

            for (int i = 0, size = prayerTimes.size(); i < size; i++) {
                PrayerTimes p = prayerTimes.get(i);

                insertSQLBuilder.append("(")
                        .append(p.place.countryId).append(", ")
                        .append(p.place.cityId).append(", ")
                        .append(p.place.districtId.isDefined ? "" + p.place.districtId.get() : "NULL").append(", ")
                        .append(p.day.getMillis()).append(", ")
                        .append(p.fajr.getMillis()).append(", ")
                        .append(p.shuruq.getMillis()).append(", ")
                        .append(p.dhuhr.getMillis()).append(", ")
                        .append(p.asr.getMillis()).append(", ")
                        .append(p.maghrib.getMillis()).append(", ")
                        .append(p.isha.getMillis()).append(", ")
                        .append(p.qibla.getMillis()).append(")");

                if (i < size - 1) {
                    insertSQLBuilder.append(", ");
                }
            }

            SQLiteDatabase database = Database.with(context).getWritableDatabase();

            boolean result = true;

            String deleteQuery;
            if (place.districtId.isDefined) {
                deleteQuery = String.format(Locale.ENGLISH,
                        "DELETE FROM %s WHERE %s = %d AND %s = %d AND %s = %d",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        place.countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        place.cityId,
                        Database.PrayerTimesTable.COLUMN_DISTRICT_ID,
                        place.districtId.get()
                );
            } else {
                deleteQuery = String.format(Locale.ENGLISH,
                        "DELETE FROM %s WHERE %s = %d AND %s = %d",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        place.countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        place.cityId
                );
            }

            try {
                database.beginTransaction();
                database.execSQL(deleteQuery);
                database.execSQL(insertSQLBuilder.toString());
                database.setTransactionSuccessful();
            } catch (Throwable t) {
                Log.error(PrayerTimes.class, t, "Failed to save prayer times for place '%s' to database, transaction failed!", place);

                result = false;
            } finally {
                database.endTransaction();
            }

            database.close();

            return result;
        } catch (Throwable t) {
            Log.error(PrayerTimes.class, t, "Failed to save prayer times for place '%s' to database!", place);

            return false;
        }
    }

    @NonNull public String toJson() {
        return String.format(Locale.ENGLISH,
                "{\"place\":%s, \"day\":\"%s\", \"fajr\":\"%s\", \"shuruq\":\"%s\", \"dhuhr\":\"%s\",\"asr\":\"%s\",\"maghrib\":\"%s\",\"isha\":\"%s\",\"qibla\":\"%s\"}",
                place.toString(),
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

    @NonNull public static Optional<PrayerTimes> fromJson(Place place, JSONObject json) {
        try {
            long day     = json.getLong("dayDate");
            long fajr    = json.getLong("fajr");
            long shuruq  = json.getLong("shuruq");
            long dhuhr   = json.getLong("dhuhr");
            long asr     = json.getLong("asr");
            long maghrib = json.getLong("maghrib");
            long isha    = json.getLong("isha");
            long qibla   = json.getLong("qibla");

            return new Some<>(new PrayerTimes(place, day, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla));
        } catch (Throwable t) {
            Log.error(PrayerTimes.class, t, "Failed to generate prayer times for place '%s' from Json '%s'!", place, json);

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }

    private static PrayerTimes fromCursor(Place place, Cursor cursor) {
        long day     = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_DAY));
        long fajr    = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_FAJR));
        long shuruq  = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_SHURUQ));
        long dhuhr   = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_DHUHR));
        long asr     = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_ASR));
        long maghrib = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_MAGHRIB));
        long isha    = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_ISHA));
        long qibla   = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_QIBLA));

        return new PrayerTimes(place.countryId, place.cityId, place.districtId, day, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla);
    }
}
