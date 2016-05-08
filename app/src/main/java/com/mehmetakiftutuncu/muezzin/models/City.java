package com.mehmetakiftutuncu.muezzin.models;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import org.json.JSONObject;

/**
 * Created by akif on 08/05/16.
 */
@SuppressLint("DefaultLocale")
public class City {
    public final int id;
    public final int countryId;
    public final String name;

    public City(int id, int countryId, String name) {
        this.id = id;
        this.countryId = countryId;
        this.name = name;
    }

    @NonNull public String toJson() {
        return String.format("{\"id\":%d,\"countryId\":%d,\"name\":\"%s\"}", id, countryId, name);
    }

    @NonNull public static Optional<City> fromJson(int countryId, JSONObject json) {
        try {
            int id      = json.getInt("id");
            String name = json.getString("name");

            return new Some<>(new City(id, countryId, name));
        } catch (Throwable t) {
            Log.error(String.format("Failed to generate city for country '%d' from Json '%s'!", countryId, json), t, City.class, "fromJson");

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }
}
