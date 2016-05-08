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
public class Country {
    public final int id;
    public final String nameEnglish;
    public final String nameTurkish;
    public final String nameNative;

    public Country(int id, String nameEnglish, String nameTurkish, String nameNative) {
        this.id = id;
        this.nameEnglish = nameEnglish;
        this.nameTurkish = nameTurkish;
        this.nameNative = nameNative;
    }

    @NonNull public String toJson() {
        return String.format("{\"id\":%d,\"nameEnglish\":\"%s\",\"nameTurkish\":\"%s\",\"nameNative\":\"%s\"}", id, nameEnglish, nameTurkish, nameNative);
    }

    @NonNull public static Optional<Country> fromJson(JSONObject json) {
        try {
            int id             = json.getInt("id");
            String nameEnglish = json.getString("name");
            String nameTurkish = json.getString("trName");
            String nameNative  = json.getString("nativeName");

            return new Some<>(new Country(id, nameEnglish, nameTurkish, nameNative));
        } catch (Throwable t) {
            Log.error(String.format("Failed to generate country from Json '%s'!", json), t, Country.class, "fromJson");

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }
}
