package com.mehmetakiftutuncu.muezzin.models

import android.content.Context
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.json.JSONObject
import java.util.*

data class PrayerTimesOfDay(val date: LocalDate,
                            val fajr: LocalTime,
                            val shuruq: LocalTime,
                            val dhuhr: LocalTime,
                            val asr: LocalTime,
                            val maghrib: LocalTime,
                            val isha: LocalTime,
                            val qibla: LocalTime) {
    constructor(date: LocalDate, prayerTimesOfDay: Map<PrayerTimeType, LocalTime>): this(
            date,
            prayerTimesOfDay.getValue(PrayerTimeType.Fajr),
            prayerTimesOfDay.getValue(PrayerTimeType.Shuruq),
            prayerTimesOfDay.getValue(PrayerTimeType.Dhuhr),
            prayerTimesOfDay.getValue(PrayerTimeType.Asr),
            prayerTimesOfDay.getValue(PrayerTimeType.Maghrib),
            prayerTimesOfDay.getValue(PrayerTimeType.Isha),
            prayerTimesOfDay.getValue(PrayerTimeType.Qibla)
    )

    fun prayerTimeByType(type: PrayerTimeType): LocalTime =
        when (type) {
            PrayerTimeType.Fajr -> fajr
            PrayerTimeType.Shuruq -> shuruq
            PrayerTimeType.Dhuhr -> dhuhr
            PrayerTimeType.Asr -> asr
            PrayerTimeType.Maghrib -> maghrib
            PrayerTimeType.Isha -> isha
            PrayerTimeType.Qibla -> qibla
        }

    fun nextPrayerTime(): LocalTime = prayerTimeAfter(LocalTime.now())

    fun prayerTimeAfter(time: LocalTime): LocalTime = prayerTimeByType(prayerTimeTypeAfter(time))

    fun nextPrayerTimeType(): PrayerTimeType = prayerTimeTypeAfter(LocalTime.now())

    fun prayerTimeTypeAfter(time: LocalTime): PrayerTimeType =
        PrayerTimeType.values().find { time.isBefore(prayerTimeByType(it)) } ?: let {
            /* After isha, so next day's fajr is next prayer time.
             * I just assume fajr time will be the same next day too,
             * HOWEVER it may/will vary a few minutes. It is still better than
             * not knowing the time at all. */
            PrayerTimeType.Fajr
        }

    fun toJson(): String =
        """{"${date.toString(dateFormatter)}":{""" +
        """"fajr":"${fajr.toString(timeFormatter)}",""" +
        """"shuruq":"${shuruq.toString(timeFormatter)}",""" +
        """"dhuhr":"${dhuhr.toString(timeFormatter)}",""" +
        """"asr":"${asr.toString(timeFormatter)}",""" +
        """"maghrib":"${maghrib.toString(timeFormatter)}",""" +
        """"isha":"${isha.toString(timeFormatter)}",""" +
        """"qibla":"${qibla.toString(timeFormatter)}"""" +
        """}}"""

    override fun toString(): String = toJson()

    companion object {
        val dateFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
        val timeFormatter = DateTimeFormat.forPattern("HH:mm")

        fun localizedName(ctx: Context, prayerTimeType: PrayerTimeType): String =
            ctx.getString(
                    ctx.resources.getIdentifier(
                            "prayerTime_${prayerTimeType.name}",
                            "string",
                            ctx.packageName
                    )
            )

        fun fromJson(date: LocalDate, json: JSONObject): PrayerTimesOfDay =
            json.runCatching {
                val times = keys().asSequence().fold(emptyMap<PrayerTimeType, LocalTime>()) { map, key ->
                    val type = PrayerTimeType.from(key)
                    val time = LocalTime.parse(getString(key), timeFormatter)
                    map + (type to time)
                }
                PrayerTimesOfDay(date, times)
            }.fold(
                { it },
                { e -> throw InvalidPrayerTimesOfDayJsonException(date, json, e) }
            )
    }

    class InvalidPrayerTimesOfDayJsonException(date: LocalDate,
                                               json: JSONObject,
                                               override val cause: Throwable): Exception("Failed to parse prayer times of day for '$date' from Json: $json")

    class InvalidPrayerTimesException(place: Place,
                                      code: Int,
                                      body: String): Exception("Failed to parse prayer times for place '$place', Muezzin API returned status '$code' with body '$body'")
}