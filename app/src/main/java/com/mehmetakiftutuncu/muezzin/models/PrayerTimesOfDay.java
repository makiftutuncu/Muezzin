package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import androidx.annotation.NonNull;

import com.github.mehmetakiftutuncu.toolbelt.Log;
import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.mehmetakiftutuncu.muezzin.database.Database;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by akif on 15/09/16.
 */
public class PrayerTimesOfDay {
    public final LocalDate date;
    public final LocalTime fajr;
    public final LocalTime shuruq;
    public final LocalTime dhuhr;
    public final LocalTime asr;
    public final LocalTime maghrib;
    public final LocalTime isha;
    public final LocalTime qibla;

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm");

    public PrayerTimesOfDay(@NonNull LocalDate date,
                            @NonNull LocalTime fajr,
                            @NonNull LocalTime shuruq,
                            @NonNull LocalTime dhuhr,
                            @NonNull LocalTime asr,
                            @NonNull LocalTime maghrib,
                            @NonNull LocalTime isha,
                            @NonNull LocalTime qibla) {
        this.date    = date;
        this.fajr    = fajr;
        this.shuruq  = shuruq;
        this.dhuhr   = dhuhr;
        this.asr     = asr;
        this.maghrib = maghrib;
        this.isha    = isha;
        this.qibla   = qibla;
    }

    public PrayerTimesOfDay(@NonNull LocalDate date,
                            @NonNull Map<PrayerTimeType, LocalTime> prayerTimesOfDay) {
        this(
            date,
            prayerTimesOfDay.get(PrayerTimeType.Fajr),
            prayerTimesOfDay.get(PrayerTimeType.Shuruq),
            prayerTimesOfDay.get(PrayerTimeType.Dhuhr),
            prayerTimesOfDay.get(PrayerTimeType.Asr),
            prayerTimesOfDay.get(PrayerTimeType.Maghrib),
            prayerTimesOfDay.get(PrayerTimeType.Isha),
            prayerTimesOfDay.get(PrayerTimeType.Qibla)
        );
    }

    public LocalTime getPrayerTimeByType(PrayerTimeType type) {
        switch (type) {
            case Fajr:    return fajr;
            case Shuruq:  return shuruq;
            case Dhuhr:   return dhuhr;
            case Asr:     return asr;
            case Maghrib: return maghrib;
            case Isha:    return isha;
            case Qibla:   return qibla;
            default:      return fajr;
        }
    }

    public LocalTime nextPrayerTime() {
        return nextPrayerTimeAfter(LocalTime.now());
    }

    public LocalTime nextPrayerTimeAfter(LocalTime prayerTime) {
        PrayerTimeType[] prayerTimeTypes = PrayerTimeType.values();

        for (int i = 0, length = prayerTimeTypes.length; i < length; i++) {
            LocalTime time = getPrayerTimeByType(prayerTimeTypes[i]);

            if (prayerTime.isBefore(time)) {
                return time;
            }
        }

        /* After isha, so next day's fajr is next prayer time.
         * I just assume fajr time will be the same next day too,
         * HOWEVER it may/will vary a few minutes. It is still better than
         * not knowing the time at all. */
        return fajr;
    }

    public PrayerTimeType nextPrayerTimeType() {
        return nextPrayerTimeTypeAfter(LocalTime.now());
    }

    public PrayerTimeType nextPrayerTimeTypeAfter(LocalTime prayerTime) {
        PrayerTimeType[] prayerTimeTypes = PrayerTimeType.values();

        for (int i = 0, length = prayerTimeTypes.length; i < length; i++) {
            PrayerTimeType prayerTimeType = prayerTimeTypes[i];
            LocalTime time = getPrayerTimeByType(prayerTimeType);

            if (prayerTime.isBefore(time)) {
                return prayerTimeType;
            }
        }

        // Same logic as in getNextPrayerTime(), return fajr.
        return PrayerTimeType.Fajr;
    }

    public static String prayerTimeLocalizedName(Context context, PrayerTimeType prayerTimeType) {
        return context.getString(context.getResources().getIdentifier(String.format(Locale.ENGLISH, "prayerTime_%s", prayerTimeType.name), "string", context.getPackageName()));
    }

    public static Optional<PrayerTimesOfDay> getPrayerTimesForToday(Context context, Place place) {
        return getPrayerTimesForDay(context, place, LocalDate.now());
    }

    public static Optional<PrayerTimesOfDay> getPrayerTimesForDay(Context context, Place place, LocalDate date) {
        try {
            Optional<PrayerTimesOfDay> prayerTimes = Optional.empty();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            String query = place.districtId.isDefined() ? (
                String.format(
                    Locale.ENGLISH,
                    "SELECT * FROM %s WHERE %s = %d AND %s = %d AND %s = %d AND %s = '%s'",
                    Database.PrayerTimesTable.TABLE_NAME,
                    Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                    place.countryId,
                    Database.PrayerTimesTable.COLUMN_CITY_ID,
                    place.cityId,
                    Database.PrayerTimesTable.COLUMN_DISTRICT_ID,
                    place.districtId.get(),
                    Database.PrayerTimesTable.COLUMN_DATE,
                    date.toString(DATE_FORMATTER)
                )
            ) : (
                String.format(
                    Locale.ENGLISH,
                    "SELECT * FROM %s WHERE %s = %d AND %s = %d AND %s = '%s'",
                    Database.PrayerTimesTable.TABLE_NAME,
                    Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                    place.countryId,
                    Database.PrayerTimesTable.COLUMN_CITY_ID,
                    place.cityId,
                    Database.PrayerTimesTable.COLUMN_DATE,
                    date.toString(DATE_FORMATTER)
                )
            );

            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    prayerTimes = Optional.with(fromCursor(cursor));
                }

                cursor.close();
            }

            database.close();

            return prayerTimes;
        } catch (Throwable t) {
            Log.error(PrayerTimesOfDay.class, t, "Failed to get prayer times for place '%s' and for date '%s' from database!", place, date);

            return Optional.empty();
        }
    }

    public static boolean saveAllPrayerTimes(Context context, Place place, List<PrayerTimesOfDay> prayerTimes) {
        try {
            int numberOfParametersToBind = 8;

            String[] parameters = new String[prayerTimes.size() * numberOfParametersToBind];

            StringBuilder insertSQLBuilder = new StringBuilder("INSERT INTO ")
                    .append(Database.PrayerTimesTable.TABLE_NAME)
                    .append(" ('")
                    .append(Database.PrayerTimesTable.COLUMN_COUNTRY_ID).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_CITY_ID).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_DISTRICT_ID).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_DATE).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_FAJR).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_SHURUQ).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_DHUHR).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_ASR).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_MAGHRIB).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_ISHA).append("', '")
                    .append(Database.PrayerTimesTable.COLUMN_QIBLA)
                    .append("') VALUES ");

            for (int i = 0, size = prayerTimes.size(); i < size; i++) {
                PrayerTimesOfDay p = prayerTimes.get(i);

                parameters[(i * numberOfParametersToBind)]     = p.date.toString(DATE_FORMATTER);
                parameters[(i * numberOfParametersToBind) + 1] = p.fajr.toString(TIME_FORMATTER);
                parameters[(i * numberOfParametersToBind) + 2] = p.shuruq.toString(TIME_FORMATTER);
                parameters[(i * numberOfParametersToBind) + 3] = p.dhuhr.toString(TIME_FORMATTER);
                parameters[(i * numberOfParametersToBind) + 4] = p.asr.toString(TIME_FORMATTER);
                parameters[(i * numberOfParametersToBind) + 5] = p.maghrib.toString(TIME_FORMATTER);
                parameters[(i * numberOfParametersToBind) + 6] = p.isha.toString(TIME_FORMATTER);
                parameters[(i * numberOfParametersToBind) + 7] = p.qibla.toString(TIME_FORMATTER);

                insertSQLBuilder.append("(")
                        .append(place.countryId).append(", ")
                        .append(place.cityId).append(", ")
                        .append(place.districtId.isDefined() ? "" + place.districtId.get() : "NULL")
                        .append(", ?, ?, ?, ?, ?, ?, ?, ?)");

                if (i < size - 1) {
                    insertSQLBuilder.append(", ");
                }
            }

            SQLiteDatabase database = Database.with(context).getWritableDatabase();

            boolean result = true;

            String deleteQuery = place.districtId.isDefined() ? (
                String.format(
                    Locale.ENGLISH,
                    "DELETE FROM %s WHERE %s = %d AND %s = %d AND %s = %d",
                    Database.PrayerTimesTable.TABLE_NAME,
                    Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                    place.countryId,
                    Database.PrayerTimesTable.COLUMN_CITY_ID,
                    place.cityId,
                    Database.PrayerTimesTable.COLUMN_DISTRICT_ID,
                    place.districtId.get()
                )
            ) : (
                String.format(
                    Locale.ENGLISH,
                    "DELETE FROM %s WHERE %s = %d AND %s = %d",
                    Database.PrayerTimesTable.TABLE_NAME,
                    Database.PrayerTimesTable.COLUMN_COUNTRY_ID,
                    place.countryId,
                    Database.PrayerTimesTable.COLUMN_CITY_ID,
                    place.cityId
                )
            );

            try {
                database.beginTransaction();
                database.execSQL(deleteQuery);
                database.execSQL(insertSQLBuilder.toString(), parameters);
                database.setTransactionSuccessful();
            } catch (Throwable t) {
                Log.error(PrayerTimesOfDay.class, t, "Failed to save prayer times for place '%s' to database, transaction failed!", place);

                result = false;
            } finally {
                database.endTransaction();
            }

            database.close();

            return result;
        } catch (Throwable t) {
            Log.error(PrayerTimesOfDay.class, t, "Failed to save prayer times for place '%s' to database!", place);

            return false;
        }
    }

    @NonNull public String toJson() {
        return String.format(
            "{\"%s\":{\"fajr\":\"%s\",\"shuruq\":\"%s\",\"dhuhr\":\"%s\",\"asr\":\"%s\",\"maghrib\":\"%s\",\"isha\":\"%s\",\"qibla\":\"%s\"}}",
            date.toString(DATE_FORMATTER),
            fajr.toString(TIME_FORMATTER),
            shuruq.toString(TIME_FORMATTER),
            dhuhr.toString(TIME_FORMATTER),
            asr.toString(TIME_FORMATTER),
            maghrib.toString(TIME_FORMATTER),
            isha.toString(TIME_FORMATTER),
            qibla.toString(TIME_FORMATTER)
        );
    }

    @NonNull public static PrayerTimesOfDay fromJson(LocalDate date, JSONObject json) throws InvalidPrayerTimesOfDayJsonException {
        try {
            Map<PrayerTimeType, LocalTime> prayerTimesOfDay = new HashMap<>();

            Iterator<String> keys = json.keys();

            while (keys.hasNext()) {
                String typeString = keys.next();

                PrayerTimeType type = PrayerTimeType.from(typeString);
                LocalTime time = LocalTime.parse(json.getString(typeString), TIME_FORMATTER);

                prayerTimesOfDay.put(type, time);
            }

            return new PrayerTimesOfDay(date, prayerTimesOfDay);
        } catch (Exception e) {
            throw new InvalidPrayerTimesOfDayJsonException(String.format("Failed to parse prayer times of day for '%s' from Json: %s", date, json), e);
        }
    }

    @Override public String toString() {
        return toJson();
    }

    private static PrayerTimesOfDay fromCursor(Cursor cursor) {
        LocalDate date    = LocalDate.parse(cursor.getString(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_DATE)),    DATE_FORMATTER);
        LocalTime fajr    = LocalTime.parse(cursor.getString(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_FAJR)),    TIME_FORMATTER);
        LocalTime shuruq  = LocalTime.parse(cursor.getString(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_SHURUQ)),  TIME_FORMATTER);
        LocalTime dhuhr   = LocalTime.parse(cursor.getString(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_DHUHR)),   TIME_FORMATTER);
        LocalTime asr     = LocalTime.parse(cursor.getString(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_ASR)),     TIME_FORMATTER);
        LocalTime maghrib = LocalTime.parse(cursor.getString(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_MAGHRIB)), TIME_FORMATTER);
        LocalTime isha    = LocalTime.parse(cursor.getString(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_ISHA)),    TIME_FORMATTER);
        LocalTime qibla   = LocalTime.parse(cursor.getString(cursor.getColumnIndex(Database.PrayerTimesTable.COLUMN_QIBLA)),   TIME_FORMATTER);

        return new PrayerTimesOfDay(date, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla);
    }

    public static class InvalidPrayerTimesOfDayJsonException extends Exception {
        public InvalidPrayerTimesOfDayJsonException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidPrayerTimesOfDayJsonException(String message) {
            super(message);
        }
    }
}
