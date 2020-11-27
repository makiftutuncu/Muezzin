package com.mehmetakiftutuncu.muezzin.models

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils
import org.json.JSONObject

data class Country(val id: Int,
                   val name: String,
                   val nameTurkish: String,
                   val nameNative: String): Comparable<Country>, Parcelable {
    constructor(parcel: Parcel): this(parcel.readInt(),
                                      parcel.readString() ?: "",
                                      parcel.readString() ?: "",
                                      parcel.readString() ?: "")

    val isTurkey = isTurkey(id)

    fun localizedName(ctx: Context): String =
        if (LocaleUtils.isLanguageTurkish(ctx)) {
            nameTurkish
        } else {
            name
        }

    fun toJson(): String = """{"$id":{"name":"$name","nameTurkish":"$nameTurkish","nameNative":"$nameNative"}}"""

    override fun compareTo(other: Country): Int =
        id.compareTo(other.id).takeUnless { it == 0 } ?: let {
            name.compareTo(other.name).takeUnless { it == 0 } ?: let {
                nameTurkish.compareTo(other.nameTurkish).takeUnless { it == 0 } ?:
                    nameNative.compareTo(other.nameNative)
            }
        }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.run {
            writeInt(id)
            writeString(name)
            writeString(nameTurkish)
            writeString(nameNative)
        }
    }

    override fun toString(): String = toJson()

    companion object {
        val CREATOR = object : Parcelable.Creator<Country> {
            override fun createFromParcel(parcel: Parcel): Country = Country(parcel)

            override fun newArray(size: Int): Array<Country?> = arrayOfNulls(size)
        }

        fun isTurkey(id: Int): Boolean = id == 2

        fun fromJson(id: Int, json: JSONObject): Country =
            json.runCatching {
                Country(
                    id,
                    getString("name"),
                    getString("nameTurkish"),
                    getString("nameNative")
                )
            }.fold(
                { it },
                { e -> throw InvalidCountryJsonException(id, json, e) }
            )
    }

    class InvalidCountryJsonException(id: Int,
                                      json: JSONObject,
                                      override val cause: Throwable): Exception("Failed to parse country for id '$id' from Json: $json", cause)

    class InvalidCountriesException(code: Int,
                                    body: String): Exception("Failed to parse countries, Muezzin API returned status '$code' with body '$body'")
}