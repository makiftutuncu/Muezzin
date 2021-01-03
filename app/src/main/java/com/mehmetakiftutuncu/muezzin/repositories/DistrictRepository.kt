package com.mehmetakiftutuncu.muezzin.repositories

import android.content.Context
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.models.City
import com.mehmetakiftutuncu.muezzin.models.District
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils

object DistrictRepository: Repository() {
    const val tableName    = "district"
    const val columnId     = "id"
    const val columnCityId = "cityId"
    const val columnName   = "name"

    const val createTableSQL =
        """
        CREATE TABLE $tableName(
            $columnId INTEGER PRIMARY KEY,
            $columnCityId INTEGER NOT NULL,
            $columnName TEXT NOT NULL
        );
        """

    fun get(ctx: Context, cityId: Int): List<District> =
        try {
            val districts = list(
                ctx,
                "SELECT * FROM $tableName WHERE $columnCityId = $cityId ORDER BY $columnId"
            ) { cursor ->
                val id   = cursor.getInt(cursor.getColumnIndex(columnId))
                val name = cursor.getString(cursor.getColumnIndex(columnName))

                District(id, name)
            }

            val collator = LocaleUtils.getCollator(ctx)
            val isTurkish = City.isTurkish(cityId)

            districts.sortedWith { (_, name1), (_, name2) ->
                (if (isTurkish) LocaleUtils.turkishCollator else collator).compare(name1, name2)
            }

            districts
        } catch (t: Throwable) {
            Log.error(DistrictRepository::class.java, t, "Failed to get districts for city '$cityId' from database!")
            emptyList()
        }

    fun save(ctx: Context, cityId: Int, districts: List<District>): Boolean =
        try {
            val sql =
                """
                INSERT INTO $tableName('$columnId', '$columnCityId', '$columnName')
                VALUES ${districts.joinToString(", ") { "(${it.id}, $cityId, ?)" }}
                """

            val parameters = districts.map { it.name }.toTypedArray()

            write(ctx) {
                try {
                    beginTransaction()
                    execSQL("DELETE FROM $tableName WHERE $columnCityId = $cityId")
                    execSQL(sql, parameters)
                    setTransactionSuccessful()
                    true
                } catch (t: Throwable) {
                    Log.error(DistrictRepository::class.java, t, "Failed to save districts for city '$cityId' to database, transaction failed!")
                    false
                } finally {
                    endTransaction()
                }
            }
        } catch (t: Throwable) {
            Log.error(DistrictRepository::class.java, t, "Failed to save districts for city '$cityId' to database!")
            false
        }
}