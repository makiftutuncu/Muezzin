package com.mehmetakiftutuncu.muezzin.repositories

import android.content.Context
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.models.Place
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils

object PlaceRepository: Repository() {
    fun getName(ctx: Context, place: Place, fullNameRequired: Boolean = true): String? =
        try {
            val countryColumn =
                if (LocaleUtils.isLanguageTurkish(ctx)) {
                    CountryRepository.columnNameTurkish
                } else {
                    CountryRepository.columnName
                }

            val districtSelectSQL =
                if (place.districtId == null) "" else ", di.${DistrictRepository.columnName} AS districtName"

            val districtFromSQL =
                if (place.districtId == null) "" else " JOIN ${DistrictRepository.tableName} AS di ON (ci.${CityRepository.columnId} = di.${DistrictRepository.columnCityId})"

            val districtWhereSQL =
                if (place.districtId == null) "" else " AND di.${DistrictRepository.columnId} = ${place.districtId}"

            val sql =
                when {
                    fullNameRequired ->
                        """
                        SELECT co.$countryColumn AS countryName,
                               ci.${CityRepository.columnName} AS cityName
                               $districtSelectSQL
                        FROM ${CountryRepository.tableName} AS co JOIN
                             ${CityRepository.tableName} AS ci ON (co.${CountryRepository.columnId} = ci.${CityRepository.columnCountryId})
                             $districtFromSQL
                        WHERE co.${CountryRepository.columnId} = ${place.countryId} AND
                              ci.${CityRepository.columnId} = ${place.cityId}
                              $districtWhereSQL
                        """

                    place.districtId != null ->
                        """
                        SELECT di.${DistrictRepository.columnName} AS districtName
                        FROM ${DistrictRepository.tableName} AS di
                        WHERE di.${DistrictRepository.columnId} = ${place.districtId}
                        """

                    else ->
                        """
                        SELECT ci.${CityRepository.columnName} AS cityName
                        FROM ${CityRepository.tableName} AS ci
                        WHERE ci.${CityRepository.columnId} = ${place.cityId}
                        """
                }

            read(ctx) {
                rawQuery(sql, null)?.use { cursor ->
                    if (!cursor.moveToFirst()) {
                        null
                    } else {
                        when {
                            fullNameRequired -> {
                                val countryName  = cursor.getString(cursor.getColumnIndex("countryName"))
                                val cityName     = cursor.getString(cursor.getColumnIndex("cityName"))
                                val districtName = cursor.getColumnIndex("districtName").takeIf { it != -1 }?.let { cursor.getString(it) }

                                if (districtName == null || districtName == cityName) {
                                    "$cityName, $countryName"
                                } else {
                                    "$districtName, $cityName, $countryName"
                                }
                            }

                            place.districtId != null ->
                                cursor.getString(cursor.getColumnIndex("districtName"))

                            else ->
                                cursor.getString(cursor.getColumnIndex("cityName"))
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            Log.error(javaClass, t, "Failed to get name for place '$place' from database!")
            null
        }
}