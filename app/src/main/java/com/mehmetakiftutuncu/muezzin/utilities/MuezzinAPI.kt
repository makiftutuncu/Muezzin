package com.mehmetakiftutuncu.muezzin.utilities

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.mehmetakiftutuncu.muezzin.models.*
import okhttp3.*
import org.joda.time.LocalDate
import org.json.JSONObject
import java.io.IOException

object MuezzinAPI {
    private const val host = "https://muezzin.herokuapp.com"
    private val http = OkHttpClient()
    private val handler = Handler(Looper.getMainLooper())

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
            }.map { countries ->
                val collator  = LocaleUtils.getCollator(ctx)
                val isTurkish = LocaleUtils.isLanguageTurkish(ctx)
                val isEnglish = LocaleUtils.isLanguageEnglish(ctx)

                val (turkey, others) = countries.sortedWith { c1, c2 ->
                    collator.compare(
                        if (isTurkish) c1.nameTurkish else if (isEnglish) c1.name else c1.nameNative,
                        if (isTurkish) c2.nameTurkish else if (isEnglish) c2.name else c2.nameNative
                    )
                }.partition { it.isTurkey }

                turkey + others
            }.fold(
                { countries -> handler.post { Runnable { onSuccess(countries) } } },
                { e -> handler.post { Runnable { onFail(e) } } }
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
                { cities -> handler.post { Runnable { onSuccess(cities) } } },
                { e -> handler.post { Runnable { onFail(e) } } }
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
                    District.fromJson(key.toInt(), json.getJSONObject(key))
                }.toList()
            }.map { districts ->
                val collator  = LocaleUtils.getCollator(ctx)

                districts.sortedWith { d1, d2 -> collator.compare(d1.name, d2.name) }
            }.fold(
                { districts -> handler.post { Runnable { onSuccess(districts) } } },
                { e -> handler.post { Runnable { onFail(e) } } }
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
                { prayerTimes -> handler.post { Runnable { onSuccess(prayerTimes) } } },
                { e -> handler.post { Runnable { onFail(e) } } }
            )
        }
    }

    private fun get(url: String,
                    onStatusFailure: (Int, String) -> Throwable,
                    onFail: (Throwable) -> Unit,
                    onSuccess: (String) -> Unit) =
        http.newCall(Request.Builder().url(url).get().build()).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                handler.post { Runnable { onFail(e) } }
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    handler.post { Runnable { onStatusFailure(response.code, body) } }
                    return
                }

                onSuccess(body)
            }
        })
}