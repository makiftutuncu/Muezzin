package com.mehmetakiftutuncu.muezzin.models;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class PrayerTimes {
    private int countryId;
    private int cityId;
    private int districtId;
    private DateTime dayDate;
    private DateTime fajr;
    private DateTime shuruq;
    private DateTime dhuhr;
    private DateTime asr;
    private DateTime maghrib;
    private DateTime isha;
    private DateTime qibla;

    public PrayerTimes(int countryId, int cityId, int districtId, DateTime dayDate, DateTime fajr, DateTime shuruq, DateTime dhuhr, DateTime asr, DateTime maghrib, DateTime isha, DateTime qibla) {
        this.countryId  = countryId;
        this.cityId     = cityId;
        this.districtId = districtId;
        this.dayDate    = dayDate;
        this.fajr       = fajr;
        this.shuruq     = shuruq;
        this.dhuhr      = dhuhr;
        this.asr        = asr;
        this.maghrib    = maghrib;
        this.isha       = isha;
        this.qibla      = qibla;
    }

    public PrayerTimes(int countryId, int cityId, int districtId, long dayDate, long fajr, long shuruq, long dhuhr, long asr, long maghrib, long isha, long qibla) {
        this.countryId  = countryId;
        this.cityId     = cityId;
        this.districtId = districtId;
        this.dayDate    = new DateTime(dayDate, DateTimeZone.UTC);
        this.fajr       = new DateTime(fajr,    DateTimeZone.UTC);
        this.shuruq     = new DateTime(shuruq,  DateTimeZone.UTC);
        this.dhuhr      = new DateTime(dhuhr,   DateTimeZone.UTC);
        this.asr        = new DateTime(asr,     DateTimeZone.UTC);
        this.maghrib    = new DateTime(maghrib, DateTimeZone.UTC);
        this.isha       = new DateTime(isha,    DateTimeZone.UTC);
        this.qibla      = new DateTime(qibla,   DateTimeZone.UTC);
    }

    public int countryId() {
        return countryId;
    }

    public int cityId() {
        return cityId;
    }

    public int districtId() {
        return districtId;
    }

    public DateTime dayDate() {
        return dayDate;
    }

    public DateTime fajr() {
        return fajr;
    }

    public DateTime shuruq() {
        return shuruq;
    }

    public DateTime dhuhr() {
        return dhuhr;
    }

    public DateTime asr() {
        return asr;
    }

    public DateTime maghrib() {
        return maghrib;
    }

    public DateTime isha() {
        return isha;
    }

    public DateTime qibla() {
        return qibla;
    }
}
