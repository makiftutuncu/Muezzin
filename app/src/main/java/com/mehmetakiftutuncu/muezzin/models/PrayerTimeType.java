package com.mehmetakiftutuncu.muezzin.models;

import android.support.annotation.NonNull;

import java.util.NoSuchElementException;

/**
 * Created by akif on 15/09/16.
 */
public enum PrayerTimeType {
    Fajr("fajr"),
    Shuruq("shuruq"),
    Dhuhr("dhuhr"),
    Asr("asr"),
    Maghrib("maghrib"),
    Isha("isha"),
    Qibla("qibla");

    public final String name;

    PrayerTimeType(@NonNull String name) {
        this.name = name;
    }

    @NonNull public static PrayerTimeType from(@NonNull String name) {
        if (name.equals(Fajr.name))    return Fajr;
        if (name.equals(Shuruq.name))  return Shuruq;
        if (name.equals(Dhuhr.name))   return Dhuhr;
        if (name.equals(Asr.name))     return Asr;
        if (name.equals(Maghrib.name)) return Maghrib;
        if (name.equals(Isha.name))    return Isha;
        if (name.equals(Qibla.name))   return Qibla;

        throw new NoSuchElementException(String.format("There is no prayer time type for name '%s'!", name));
    }
}
