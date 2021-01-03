package com.mehmetakiftutuncu.muezzin.repositories

import android.content.Context
import android.database.Cursor
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.models.Place
import com.mehmetakiftutuncu.muezzin.models.PrayerTimesOfDay
import org.joda.time.LocalDate
import org.joda.time.LocalTime

object PrayerTimesOfDayRepository: Repository() {
    const val tableName        = "prayerTimes"

    private const val columnCountryId  = "countryId"
    private const val columnCityId     = "cityId"
    private const val columnDistrictId = "districtId"
    private const val columnDate       = "date"
    private const val columnFajr       = "fajr"
    private const val columnShuruq     = "shuruq"
    private const val columnDhuhr      = "dhuhr"
    private const val columnAsr        = "asr"
    private const val columnMaghrib    = "maghrib"
    private const val columnIsha       = "isha"
    private const val columnQibla      = "qibla"

    const val createTableSQL =
        """
        CREATE TABLE $tableName(
            $columnCountryId INTEGER NOT NULL,
            $columnCityId INTEGER NOT NULL,
            $columnDistrictId INTEGER,
            $columnDate TEXT NOT NULL,
            $columnFajr TEXT NOT NULL,
            $columnShuruq TEXT NOT NULL,
            $columnDhuhr TEXT NOT NULL,
            $columnAsr TEXT NOT NULL,
            $columnMaghrib TEXT NOT NULL,
            $columnIsha TEXT NOT NULL,
            $columnQibla TEXT NOT NULL
        );
        """

    fun getForToday(ctx: Context, place: Place): PrayerTimesOfDay? =
        get(ctx, place, LocalDate.now())

    fun get(ctx: Context, place: Place, date: LocalDate): PrayerTimesOfDay? =
        try {
            val sql =
                """
                SELECT *
                FROM $tableName
                WHERE $columnDate = ${date.toString(PrayerTimesOfDay.dateFormatter)}
                AND $columnCountryId = ${place.countryId}
                AND $columnCityId = ${place.cityId}
                ${if (place.districtId != null) "AND $columnDistrictId = ${place.districtId}" else ""}
                """

            first(ctx, sql) {
                fromCursor(it)
            }
        } catch (t: Throwable) {
            Log.error(javaClass, t, "Failed to get prayer times for place '$place' and for date '$date' from database!")
            null
        }

    fun save(ctx: Context, place: Place, prayerTimes: List<PrayerTimesOfDay>): Boolean =
        try {
            val deleteSQL = "DELETE FROM $tableName WHERE $columnCountryId = ${place.countryId}"

            val insertSQL =
                """
                INSERT INTO $tableName('$columnCountryId', '$columnCityId', '$columnDistrictId', '$columnDate', '$columnFajr', '$columnShuruq', '$columnDhuhr', '$columnAsr', '$columnMaghrib', '$columnIsha', '$columnQibla')
                VALUES ${prayerTimes.joinToString(", ") { "(${place.countryId}, ${place.cityId}, ${place.districtId}, '${it.date.toString(PrayerTimesOfDay.dateFormatter)}', '${it.fajr.toString(PrayerTimesOfDay.timeFormatter)}', '${it.shuruq.toString(PrayerTimesOfDay.timeFormatter)}', '${it.dhuhr.toString(PrayerTimesOfDay.timeFormatter)}', '${it.asr.toString(PrayerTimesOfDay.timeFormatter)}', '${it.maghrib.toString(PrayerTimesOfDay.timeFormatter)}', '${it.isha.toString(PrayerTimesOfDay.timeFormatter)}', '${it.qibla.toString(PrayerTimesOfDay.timeFormatter)}')" }}
                """

            write(ctx) {
                try {
                    beginTransaction()
                    execSQL(deleteSQL)
                    execSQL(insertSQL)
                    setTransactionSuccessful()
                    true
                } catch (t: Throwable) {
                    Log.error(javaClass, t, "Failed to save prayer times for place '$place' to database, transaction failed!")
                    false
                } finally {
                    endTransaction()
                }
            }
        } catch (t: Throwable) {
            Log.error(javaClass, t, "Failed to save prayer times for place '$place' to database!")
            false
        }

    private fun fromCursor(cursor: Cursor): PrayerTimesOfDay =
        cursor.run {
            val date    = LocalDate.parse(getString(getColumnIndex(columnDate)),    PrayerTimesOfDay.dateFormatter)
            val fajr    = LocalTime.parse(getString(getColumnIndex(columnFajr)),    PrayerTimesOfDay.timeFormatter)
            val shuruq  = LocalTime.parse(getString(getColumnIndex(columnShuruq)),  PrayerTimesOfDay.timeFormatter)
            val dhuhr   = LocalTime.parse(getString(getColumnIndex(columnDhuhr)),   PrayerTimesOfDay.timeFormatter)
            val asr     = LocalTime.parse(getString(getColumnIndex(columnAsr)),     PrayerTimesOfDay.timeFormatter)
            val maghrib = LocalTime.parse(getString(getColumnIndex(columnMaghrib)), PrayerTimesOfDay.timeFormatter)
            val isha    = LocalTime.parse(getString(getColumnIndex(columnIsha)),    PrayerTimesOfDay.timeFormatter)
            val qibla   = LocalTime.parse(getString(getColumnIndex(columnQibla)),   PrayerTimesOfDay.timeFormatter)

            PrayerTimesOfDay(date, fajr, shuruq, dhuhr, asr, maghrib, isha, qibla);
        }
}