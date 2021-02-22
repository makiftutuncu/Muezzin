package com.mehmetakiftutuncu.muezzin.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.repositories.CityRepository
import com.mehmetakiftutuncu.muezzin.repositories.CountryRepository
import com.mehmetakiftutuncu.muezzin.repositories.DistrictRepository
import com.mehmetakiftutuncu.muezzin.repositories.PrayerTimesOfDayRepository

class Database(ctx: Context): SQLiteOpenHelper(ctx, name, null, version) {
    override fun onCreate(database: SQLiteDatabase) =
        database.run {
            execSQL(CountryRepository.createTableSQL)
            execSQL(CityRepository.createTableSQL)
            execSQL(DistrictRepository.createTableSQL)
            execSQL(PrayerTimesOfDayRepository.createTableSQL)
        }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.run {
            execSQL("DROP TABLE IF EXISTS " + CountryRepository.tableName)
            execSQL("DROP TABLE IF EXISTS " + CityRepository.tableName)
            execSQL("DROP TABLE IF EXISTS " + DistrictRepository.tableName)
            execSQL("DROP TABLE IF EXISTS " + PrayerTimesOfDayRepository.tableName)
        }.also {
            Log.warn(javaClass, "Upgrading database '$name' from version '$oldVersion' to '$newVersion'!")
            onCreate(database)
        }
    }

    companion object {
        private const val name = "muezzin"
        private const val version = 2
    }
}