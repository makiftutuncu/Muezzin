package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

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

    public PrayerTimes(int countryId, int cityId, Optional<Integer> districtId, long day, long fajr, long shuruq, long dhuhr, long asr, long maghrib, long isha, long qibla) {
        this.countryId  = countryId;
        this.cityId     = cityId;
        this.districtId = districtId;
        this.day        = new DateTime(day,     DateTimeZone.UTC);
        this.fajr       = new DateTime(fajr,    DateTimeZone.UTC);
        this.shuruq     = new DateTime(shuruq,  DateTimeZone.UTC);
        this.dhuhr      = new DateTime(dhuhr,   DateTimeZone.UTC);
        this.asr        = new DateTime(asr,     DateTimeZone.UTC);
        this.maghrib    = new DateTime(maghrib, DateTimeZone.UTC);
        this.isha       = new DateTime(isha,    DateTimeZone.UTC);
        this.qibla      = new DateTime(qibla,   DateTimeZone.UTC);
    }

    public static Optional<ArrayList<PrayerTimes>> getPrayerTimes(Context context, int countryId, int cityId, Optional<Integer> districtId) {
        try {
            ArrayList<PrayerTimes> prayerTimes = new ArrayList<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            String query;
            if (districtId.isDefined) {
                query = String.format(Locale.ENGLISH,
                        "SELECT * FROM %s WHERE %s = %d AND %s = %d AND %s = %d ORDER BY %s",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        cityId,
                        Database.PrayerTimesTable.COLUMN_DISTRICT_ID,
                        districtId.get(),
                        Database.PrayerTimesTable.COLUMN_DAY
                );
            } else {
                query = String.format(Locale.ENGLISH,
                        "SELECT * FROM %s WHERE %s = %d AND %s = %d ORDER BY %s",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        cityId,
                        Database.PrayerTimesTable.COLUMN_DAY
                );
            }

            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    long day     = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_DAY));
                    long fajr    = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_FAJR));
                    long shuruq  = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_SHURUQ));
                    long dhuhr   = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_DHUHR));
                    long asr     = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_ASR));
                    long maghrib = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_MAGHRIB));
                    long isha    = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_ISHA));
                    long qibla   = cursor.getLong(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_QIBLA));

                    PrayerTimes p = new PrayerTimes(countryId, cityId, districtId, day, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla);

                    prayerTimes.add(p);

                    cursor.moveToNext();
                }

                cursor.close();
            }

            database.close();

            return new Some<>(prayerTimes);
        } catch (Throwable t) {
            Log.error(PrayerTimes.class, t, "Failed to get prayer times for country '%d', city '%d' and district '%s' from database!", countryId, cityId, districtId);

            return new None<>();
        }
    }

    public static boolean savePrayerTimes(Context context, int countryId, int cityId, Optional<Integer> districtId, ArrayList<PrayerTimes> prayerTimes) {
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
                        .append(p.countryId).append(", ")
                        .append(p.cityId).append(", ")
                        .append(p.districtId.isDefined ? "" + p.districtId.get() : "NULL").append(", ")
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
            if (districtId.isDefined) {
                deleteQuery = String.format(Locale.ENGLISH,
                        "DELETE FROM %s WHERE %s = %d AND %s = %d AND %s = %d",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        cityId,
                        Database.PrayerTimesTable.COLUMN_DISTRICT_ID,
                        districtId.get()
                );
            } else {
                deleteQuery = String.format(Locale.ENGLISH,
                        "DELETE FROM %s WHERE %s = %d AND %s = %d",
                        Database.PrayerTimesTable.TABLE_NAME,
                        Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                        countryId,
                        Database.PrayerTimesTable.COLUMN_CITY_ID,
                        cityId
                );
            }

            try {
                database.beginTransaction();
                database.execSQL(deleteQuery);
                database.execSQL(insertSQLBuilder.toString());
                database.setTransactionSuccessful();
            } catch (Throwable t) {
                Log.error(PrayerTimes.class, t, "Failed to save prayer times for country '%d', city '%d' and district '%s' to database, transaction failed!", countryId, cityId, districtId);

                result = false;
            } finally {
                database.endTransaction();
            }

            database.close();

            return result;
        } catch (Throwable t) {
            Log.error(PrayerTimes.class, t, "Failed to save prayer times for country '%d', city '%d' and district '%s' to database!", countryId, cityId, districtId);

            return false;
        }
    }

    @NonNull public String toJson() {
        String districtIdString = districtId.isDefined ? districtId.toString() : null;

        return String.format(Locale.ENGLISH,
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

    @NonNull public static Optional<PrayerTimes> fromJson(int countryId, int cityId, Optional<Integer> districtId, JSONObject json) {
        try {
            long day     = json.getLong("dayDate");
            long fajr    = json.getLong("fajr");
            long shuruq  = json.getLong("shuruq");
            long dhuhr   = json.getLong("dhuhr");
            long asr     = json.getLong("asr");
            long maghrib = json.getLong("maghrib");
            long isha    = json.getLong("isha");
            long qibla   = json.getLong("qibla");

            return new Some<>(new PrayerTimes(countryId, cityId, districtId, day, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla));
        } catch (Throwable t) {
            Log.error(PrayerTimes.class, t, "Failed to generate prayer times for country '%d', city '%d' and district '%s' from Json '%s'!", countryId, cityId, districtId, json);

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }
}
