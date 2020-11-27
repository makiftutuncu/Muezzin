package com.mehmetakiftutuncu.muezzin.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class City(val id: Int, val name: String): Comparable<City>, Parcelable {
    constructor(parcel: Parcel): this(parcel.readInt(), parcel.readString() ?: "")

    val isTurkish = isTurkish(id)

    fun toJson(): String = """{"$id":{"name":"$name"}}"""

    override fun compareTo(other: City): Int =
        id.compareTo(other.id).takeUnless { it == 0 } ?:
            name.compareTo(other.name)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.run {
            writeInt(id)
            writeString(name)
        }
    }

    override fun toString(): String = toJson()

    companion object {
        val CREATOR = object : Parcelable.Creator<City> {
            override fun createFromParcel(parcel: Parcel): City = City(parcel)

            override fun newArray(size: Int): Array<City?> = arrayOfNulls(size)
        }

        fun isTurkish(id: Int): Boolean = id in 500..580

        fun fromJson(id: Int, json: JSONObject): City =
            json.runCatching {
                City(id, getString("name"))
            }.fold(
                { it },
                { e -> throw InvalidCityJsonException(id, json, e) }
            )
    }

    class InvalidCityJsonException(id: Int,
                                   json: JSONObject,
                                   override val cause: Throwable): Exception("Failed to parse city for id '$id' from Json: $json")

    class InvalidCitiesException(countryId: Int,
                                 code: Int,
                                 body: String): Exception("Failed to parse cities for country '$countryId', Muezzin API returned status '$code' with body '$body'")
}