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

    private static final String dateFormat = "YYYY.MM.dd";
    private static final String timeFormat = "HH:mm";

    public PrayerTimes(int countryId, int cityId, Optional<Integer> districtId, long day, long fajr, long shuruq, long dhuhr, long asr, long maghrib, long isha, long qibla) {
        this(new Place(countryId, cityId, districtId), day, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla);
    }

    public PrayerTimes(Place place, long day, long fajr, long shuruq, long dhuhr, long asr, long maghrib, long isha, long qibla) {
        this.place   = place;
        this.day     = new DateTime(day);
        this.fajr    = new DateTime(fajr);
        this.shuruq  = new DateTime(shuruq);
        this.dhuhr   = new DateTime(dhuhr);
        this.asr     = new DateTime(asr);
        this.maghrib = new DateTime(maghrib);
        this.isha    = new DateTime(isha);
        this.qibla   = new DateTime(qibla);
    }

    public static Optional<ArrayList<PrayerTimes>> getPrayerTimes(Context context, Place place) {
        try {
            ArrayList<PrayerTimes> prayerTimes = new ArrayList<>();

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

                    PrayerTimes p = new PrayerTimes(place.countryId, place.cityId, place.districtId, day, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla);

                    prayerTimes.add(p);

                    cursor.moveToNext();
                }

                cursor.close();
            }

            database.close();

            return new Some<>(prayerTimes);
        } catch (Throwable t) {
            Log.error(PrayerTimes.class, t, "Failed to get prayer times for place '%s' from database!", place);

            return new None<>();
        }
    }

    public static boolean savePrayerTimes(Context context, Place place, ArrayList<PrayerTimes> prayerTimes) {
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
}
