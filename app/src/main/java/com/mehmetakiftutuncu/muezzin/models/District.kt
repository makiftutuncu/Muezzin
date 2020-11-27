package com.mehmetakiftutuncu.muezzin.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class District(val id: Int, val name: String): Comparable<District>, Parcelable {
    constructor(parcel: Parcel): this(parcel.readInt(), parcel.readString() ?: "")

    fun toJson(): String = """{"$id":{"name":"$name"}}"""

    override fun compareTo(other: District): Int =
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
        val CREATOR = object : Parcelable.Creator<District> {
            override fun createFromParcel(parcel: Parcel): District = District(parcel)

            override fun newArray(size: Int): Array<District?> = arrayOfNulls(size)
        }

        fun fromJson(id: Int, json: JSONObject): District =
            json.runCatching {
                District(id, getString("name"))
            }.fold(
                { it },
                { e -> throw InvalidDistrictJsonException(id, json, e) }
            )
    }

    class InvalidDistrictJsonException(id: Int,
                                       json: JSONObject,
                                       override val cause: Throwable): Exception("Failed to parse district for id '$id' from Json: $json")

    class InvalidDistrictsException(countryId: Int,
                                    cityId: Int,
                                    code: Int,
                                    body: String): Exception("Failed to parse districts for country '$countryId' and city '$cityId', Muezzin API returned status '$code' with body '$body'")
}