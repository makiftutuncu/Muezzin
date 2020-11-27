package com.mehmetakiftutuncu.muezzin.models

enum class PrayerTimeType(val key: String) {
    Fajr("fajr"),
    Shuruq("shuruq"),
    Dhuhr("dhuhr"),
    Asr("asr"),
    Maghrib("maghrib"),
    Isha("isha"),
    Qibla("qibla");

    companion object {
        fun from(key: String): PrayerTimeType =
            when (key) {
                Fajr.key    -> Fajr
                Shuruq.key  -> Shuruq
                Dhuhr.key   -> Dhuhr
                Asr.key     -> Asr
                Maghrib.key -> Maghrib
                Isha.key    -> Isha
                Qibla.key   -> Qibla
                else        -> throw NoSuchElementException("There is no prayer time type for key '$key'!")
            }
    }
}