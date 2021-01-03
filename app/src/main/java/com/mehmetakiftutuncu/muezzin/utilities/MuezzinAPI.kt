package com.mehmetakiftutuncu.muezzin.utilities

import android.content.Context
import com.mehmetakiftutuncu.muezzin.models.*
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils.turkeyFirstSorted
import okhttp3.*
import org.joda.time.LocalDate
import org.json.JSONObject
import java.io.IOException

object MuezzinAPI {
    private const val host = "https://muezzin.herokuapp.com"
    private val http = OkHttpClient()

    fun getCountries(ctx: Context,
                     onFail: (Throwable) -> Unit,
                     onSuccess: (List<Country>) -> Unit) =
        get(
            "$host/countries",
            { code, body -> Country.InvalidCountriesException(code, body) },
            onFail
        ) { body ->
            kotlin.runCatching {
                JSONObject(body).getJSONObject("countries")
            }.mapCatching { json ->
                json.keys().asSequence().map { key ->
                    Country.fromJson(key.toInt(), json.getJSONObject(key))
                }.toList()
            }.map {
                it.turkeyFirstSorted(ctx)
            }.fold(
                { countries -> onSuccess(countries) },
                { e -> onFail(e) }
            )
        }

    fun getCities(ctx: Context,
                  countryId: Int,
                  onFail: (Throwable) -> Unit,
                  onSuccess: (List<City>) -> Unit) =
        get(
            "$host/countries/$countryId/cities",
            { code, body -> City.InvalidCitiesException(countryId, code, body) },
            onFail
        ) { body ->
            kotlin.runCatching {
                JSONObject(body).getJSONObject("cities")
            }.mapCatching { json ->
                json.keys().asSequence().map { key ->
                    City.fromJson(key.toInt(), json.getJSONObject(key))
                }.toList()
            }.map { cities ->
                val collator  = LocaleUtils.getCollator(ctx)

                cities.sortedWith { c1, c2 -> collator.compare(c1.name, c2.name) }
            }.fold(
                { cities -> onSuccess(cities) },
                { e -> onFail(e) }
            )
        }

    fun getDistricts(ctx: Context,
                     countryId: Int,
                     cityId: Int,
                     onFail: (Throwable) -> Unit,
                     onSuccess: (List<District>) -> Unit) =
        get(
            "$host/countries/$countryId/cities/$cityId/districts",
            { code, body -> District.InvalidDistrictsException(countryId, cityId, code, body) },
            onFail
        ) { body ->
            kotlin.runCatching {
                JSONObject(body).getJSONObject("districts")
            }.mapCatching { json ->
                json.keys().asSequence().map { key ->
                    District(key.toInt(), json.getString(key))
                }.toList()
            }.map { districts ->
                val collator  = LocaleUtils.getCollator(ctx)

                districts.sortedWith { d1, d2 -> collator.compare(d1.name, d2.name) }
            }.fold(
                { districts -> onSuccess(districts) },
                { e -> onFail(e) }
            )
        }

    fun getPrayerTimes(place: Place,
                       onFail: (Throwable) -> Unit,
                       onSuccess: (List<PrayerTimesOfDay>) -> Unit) {
        val districtPath = if (place.districtId == null) "" else "/district/${place.districtId}"

        val url = "$host/prayerTimes/country/${place.countryId}/city/${place.cityId}$districtPath"

        get(
            url,
            { code, body -> PrayerTimesOfDay.InvalidPrayerTimesException(place, code, body) },
            onFail
        ) { body ->
            kotlin.runCatching {
                JSONObject(body).getJSONObject("prayerTimes")
            }.mapCatching { json ->
                json.keys().asSequence().map { key ->
                    val date = LocalDate.parse(key, PrayerTimesOfDay.dateFormatter)
                    PrayerTimesOfDay.fromJson(date, json.getJSONObject(key))
                }.toList()
            }.fold(
                { prayerTimes -> onSuccess(prayerTimes) },
                { e -> onFail(e) }
            )
        }
    }

    private fun get(url: String,
                    onStatusFailure: (Int, String) -> Throwable,
                    onFail: (Throwable) -> Unit,
                    onSuccess: (String) -> Unit) =
        http.newCall(Request.Builder().url(url).get().build()).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                onFail(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    onStatusFailure(response.code, body)
                    return
                }

                onSuccess(body)
            }
        })
}