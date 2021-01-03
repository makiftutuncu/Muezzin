package com.mehmetakiftutuncu.muezzin.repositories

import android.content.Context
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.models.Country
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils.turkeyFirstSorted

object CountryRepository: Repository() {
    const val tableName         = "county"
    const val columnId          = "id"
    const val columnName        = "name"
    const val columnNameTurkish = "nameTurkish"
    const val columnNameNative  = "nameNative"

    const val createTableSQL =
        """
        CREATE TABLE $tableName(
            $columnId INTEGER PRIMARY KEY,
            $columnName TEXT NOT NULL,
            $columnNameTurkish TEXT NOT NULL,
            $columnNameNative TEXT NOT NULL
        );
        """

    fun get(ctx: Context): List<Country> =
        try {
            val orderBy =
                when {
                    LocaleUtils.isLanguageTurkish(ctx) -> columnNameTurkish
                    LocaleUtils.isLanguageEnglish(ctx) -> columnName
                    else                               -> columnNameNative
                }

            val countries = list(
                ctx,
                "SELECT * FROM $tableName ORDER BY $orderBy"
            ) { cursor ->
                val id   = cursor.getInt(cursor.getColumnIndex(columnId))
                val name = cursor.getString(cursor.getColumnIndex(columnName))
                val nameTurkish = cursor.getString(cursor.getColumnIndex(columnNameTurkish))
                val nameNative = cursor.getString(cursor.getColumnIndex(columnNameNative))

                Country(id, name, nameTurkish, nameNative)
            }

            countries.turkeyFirstSorted(ctx)
        } catch (t: Throwable) {
            Log.error(CountryRepository::class.java, t, "Failed to get countries from database!")
            emptyList()
        }

    fun save(ctx: Context, countries: List<Country>): Boolean =
        try {
            val sql =
                """
                INSERT INTO $tableName('$columnId', '$columnName', '$columnNameTurkish', '$columnNameNative')
                VALUES ${countries.joinToString(", ") { "(${it.id}, ?, ?, ?)" }}
                """

            val parameters = countries.flatMap { listOf(it.name, it.nameTurkish, it.nameNative) }.toTypedArray()

            write(ctx) {
                try {
                    beginTransaction()
                    execSQL("DELETE FROM $tableName")
                    execSQL(sql, parameters)
                    setTransactionSuccessful()
                    true
                } catch (t: Throwable) {
                    Log.error(CountryRepository::class.java, t, "Failed to save countries to database, transaction failed!")
                    false
                } finally {
                    endTransaction()
                }
            }
        } catch (t: Throwable) {
            Log.error(CountryRepository::class.java, t, "Failed to save countries to database!")
            false
        }
}