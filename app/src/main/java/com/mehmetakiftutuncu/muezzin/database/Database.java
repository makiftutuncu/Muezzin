package com.mehmetakiftutuncu.muezzin.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.mehmetakiftutuncu.toolbelt.Log;

/**
 * Created by akif on 09/05/16.
 */
public class Database extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "muezzin";
    public static final int DATABASE_VERSION = 2;

    public class CountryTable {
        public static final String TABLE_NAME = "country";

        public static final String COLUMN_ID           = "id";
        public static final String COLUMN_NAME         = "name";
        public static final String COLUMN_NAME_TURKISH = "nameTurkish";
        public static final String COLUMN_NAME_NATIVE  = "nameNative";

        public static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID           + " INTEGER PRIMARY KEY, " +
                COLUMN_NAME +         " TEXT NOT NULL, " +
                COLUMN_NAME_TURKISH + " TEXT NOT NULL, " +
                COLUMN_NAME_NATIVE +  " TEXT NOT NULL" +
            ");";
    }

    public class CityTable {
        public static final String TABLE_NAME = "city";

        public static final String COLUMN_ID         = "id";
        public static final String COLUMN_COUNTRY_ID = "countryId";
        public static final String COLUMN_NAME       = "name";

        public static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID         + " INTEGER PRIMARY KEY, " +
                COLUMN_COUNTRY_ID + " INTEGER NOT NULL, " +
                COLUMN_NAME       + " TEXT NOT NULL" +
            ");";
    }

    public class DistrictTable {
        public static final String TABLE_NAME = "district";

        public static final String COLUMN_ID      = "id";
        public static final String COLUMN_CITY_ID = "cityId";
        public static final String COLUMN_NAME    = "name";

        public static final String CREATE_TABLE_SQL =
            "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID      + " INTEGER PRIMARY KEY, " +
                COLUMN_CITY_ID + " INTEGER NOT NULL, " +
                COLUMN_NAME    + " TEXT NOT NULL" +
            ");";
    }

    public class PrayerTimesTable {
        public static final String TABLE_NAME = "prayerTimes";

        public static final String COLUMN_COUNTRY_ID  = "countryId";
        public static final String COLUMN_CITY_ID     = "cityId";
        public static final String COLUMN_DISTRICT_ID = "districtId";
        public static final String COLUMN_DATE        = "date";
        public static final String COLUMN_FAJR        = "fajr";
        public static final String COLUMN_SHURUQ      = "shuruq";
        public static final String COLUMN_DHUHR       = "dhuhr";
        public static final String COLUMN_ASR         = "asr";
        public static final String COLUMN_MAGHRIB     = "maghrib";
        public static final String COLUMN_ISHA        = "isha";
        public static final String COLUMN_QIBLA       = "qibla";

        public static final String CREATE_TABLE_SQL =
                "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_COUNTRY_ID  + " INTEGER NOT NULL, " +
                    COLUMN_CITY_ID     + " INTEGER NOT NULL, " +
                    COLUMN_DISTRICT_ID + " INTEGER, " +
                    COLUMN_DATE        + " TEXT NOT NULL, " +
                    COLUMN_FAJR        + " TEXT NOT NULL, " +
                    COLUMN_SHURUQ      + " TEXT NOT NULL, " +
                    COLUMN_DHUHR       + " TEXT NOT NULL, " +
                    COLUMN_ASR         + " TEXT NOT NULL, " +
                    COLUMN_MAGHRIB     + " TEXT NOT NULL, " +
                    COLUMN_ISHA        + " TEXT NOT NULL, " +
                    COLUMN_QIBLA       + " TEXT NOT NULL" +
                ");";
    }

    private Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static Database with(Context context) {
        return new Database(context);
    }

    @Override public void onCreate(SQLiteDatabase database) {
        database.execSQL(CountryTable.CREATE_TABLE_SQL);
        database.execSQL(CityTable.CREATE_TABLE_SQL);
        database.execSQL(DistrictTable.CREATE_TABLE_SQL);
        database.execSQL(PrayerTimesTable.CREATE_TABLE_SQL);
    }

    @Override public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.warn(getClass(), "Upgrading database '%s' from version '%d' to '%d'!", DATABASE_NAME, oldVersion, newVersion);

        database.execSQL("DROP TABLE IF EXISTS " + CountryTable.TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + CityTable.TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + DistrictTable.TABLE_NAME);
        database.execSQL("DROP TABLE IF EXISTS " + PrayerTimesTable.TABLE_NAME);

        onCreate(database);
    }
}
