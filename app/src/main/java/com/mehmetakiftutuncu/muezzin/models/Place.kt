package com.mehmetakiftutuncu.muezzin.models

import android.os.Bundle
import org.json.JSONObject

data class Place(val countryId: Int,
                 val cityId: Int,
                 val districtId: Int?) {
    fun toJson(): String =
        """{"countryId":$countryId,"cityId":$cityId${if (districtId != null) ""","districtId":$districtId""" else ""}}"""

    fun toBundle(): Bundle =
        Bundle().apply {
            putInt(extraCountryId, countryId)
            putInt(extraCityId, cityId)

            if (districtId != null) {
                putInt(extraDistrictId, districtId)
            }
        }

    override fun toString(): String = toJson()

    companion object {
        private const val extraCountryId  = "countryId"
        private const val extraCityId     = "cityId"
        private const val extraDistrictId = "districtId"

        fun fromJson(json: JSONObject): Place? =
            json.runCatching {
                Place(
                    getInt("countryId"),
                    getInt("cityId"),
                    optInt("districtId", 0).takeUnless { it == 0 }
                )
            }.fold({ it }, { null })

        fun fromBundle(bundle: Bundle): Place =
            bundle.run {
                Place(
                    getInt(extraCountryId),
                    getInt(extraCityId),
                    getInt(extraDistrictId, 0).takeUnless { it == 0 },
                )
            }
    }
}