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
public class District {
    public final int id;
    public final int cityId;
    public final String name;

    public District(int id, int cityId, String name) {
        this.id = id;
        this.cityId = cityId;
        this.name = name;
    }

    @NonNull public String toJson() {
        return String.format("{\"id\":%d,\"cityId\":%d,\"name\":\"%s\"}", id, cityId, name);
    }

    @NonNull public static Optional<District> fromJson(int cityId, JSONObject json) {
        try {
            int id      = json.getInt("id");
            String name = json.getString("name");

            return new Some<>(new District(id, cityId, name));
        } catch (Throwable t) {
            Log.error(String.format("Failed to generate district for city '%d' from Json '%s'!", cityId, json), t, District.class, "fromJson");

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }
}
