package com.mehmetakiftutuncu.muezzin.repositories

import android.content.Context
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.models.City
import com.mehmetakiftutuncu.muezzin.models.Country
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils

object CityRepository: Repository() {
    const val tableName       = "city"
    const val columnId        = "id"
    const val columnCountryId = "countryId"
    const val columnName      = "name"

    const val createTableSQL =
        """
        CREATE TABLE $tableName(
            $columnId INTEGER PRIMARY KEY,
            $columnCountryId INTEGER NOT NULL,
            $columnName TEXT NOT NULL
        );
        """

    fun get(ctx: Context, countryId: Int): List<City> =
        try {
            val cities = list(
                ctx,
                "SELECT * FROM $tableName WHERE $columnCountryId = $countryId ORDER BY $columnId"
            ) { cursor ->
                val id   = cursor.getInt(cursor.getColumnIndex(columnId))
                val name = cursor.getString(cursor.getColumnIndex(columnName))

                City(id, name)
            }

            val collator = LocaleUtils.getCollator(ctx)
            val isTurkey = Country.isTurkey(countryId)

            cities.sortedWith { (_, name1), (_, name2) ->
                (if (isTurkey) LocaleUtils.turkishCollator else collator).compare(name1, name2)
            }

            cities
        } catch (t: Throwable) {
            Log.error(javaClass, t, "Failed to get cities for country '$countryId' from database!")
            emptyList()
        }

    fun save(ctx: Context, countryId: Int, cities: List<City>): Boolean =
        try {
            val sql =
                """
                INSERT INTO $tableName('$columnId', '$columnCountryId', '$columnName')
                VALUES ${cities.joinToString(", ") { "(${it.id}, $countryId, ?)" }}
                """

            val parameters = cities.map { it.name }.toTypedArray()

            write(ctx) {
                try {
                    beginTransaction()
                    execSQL("DELETE FROM $tableName WHERE $columnCountryId = $countryId")
                    execSQL(sql, parameters)
                    setTransactionSuccessful()
                    true
                } catch (t: Throwable) {
                    Log.error(javaClass, t, "Failed to save cities for country '$countryId' to database, transaction failed!")
                    false
                } finally {
                    endTransaction()
                }
            }
        } catch (t: Throwable) {
            Log.error(javaClass, t, "Failed to save cities for country '$countryId' to database!")
            false
        }
}