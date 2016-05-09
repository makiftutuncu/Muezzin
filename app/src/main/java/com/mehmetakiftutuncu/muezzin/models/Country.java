package com.mehmetakiftutuncu.muezzin.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.database.Database;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import org.json.JSONObject;

import java.util.ArrayList;

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

    public Optional<ArrayList<Country>> getCountries(Context context) {
        try {
            ArrayList<Country> countries = new ArrayList<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            Cursor cursor = database.rawQuery("SELECT * FROM " + Database.CountryTable.TABLE_NAME, null);

            if (cursor != null && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    int id             = cursor.getInt(cursor.getColumnIndex(Database.CountryTable.COLUMN_ID));
                    String nameEnglish = cursor.getString(cursor.getColumnIndex(Database.CountryTable.COLUMN_NAME_ENGLISH));
                    String nameTurkish = cursor.getString(cursor.getColumnIndex(Database.CountryTable.COLUMN_NAME_TURKISH));
                    String nameNative  = cursor.getString(cursor.getColumnIndex(Database.CountryTable.COLUMN_NAME_NATIVE));

                    Country country = new Country(id, nameEnglish, nameTurkish, nameNative);

                    countries.add(country);

                    cursor.moveToNext();
                }

                cursor.close();
            }

            database.close();

            return new Some<>(countries);
        } catch (Throwable t) {
            Log.error(getClass(), t, "Failed to get countries from database!");

            return new None<>();
        }
    }

    public boolean saveCountries(Context context, ArrayList<Country> countries) {
        try {
            int numberOfParametersToBind = 3;

            String[] parameters = new String[countries.size() * numberOfParametersToBind];
            StringBuilder insertSQLBuilder = new StringBuilder("INSERT INTO ")
                    .append(Database.CountryTable.TABLE_NAME)
                    .append(" (")
                    .append(Database.CountryTable.COLUMN_ID)
                    .append(", ")
                    .append(Database.CountryTable.COLUMN_NAME_ENGLISH)
                    .append(", ")
                    .append(Database.CountryTable.COLUMN_NAME_TURKISH)
                    .append(", ")
                    .append(Database.CountryTable.COLUMN_NAME_NATIVE)
                    .append(") VALUES ");

            for (int i = 0, size = countries.size(); i < size; i++) {
                Country country = countries.get(i);

                insertSQLBuilder.append("(").append(country.id).append(", ?, ?, ?)");

                parameters[i * numberOfParametersToBind]       = country.nameEnglish;
                parameters[(i * numberOfParametersToBind) + 1] = country.nameTurkish;
                parameters[(i * numberOfParametersToBind) + 2] = country.nameNative;

                if (i < size - 1) {
                    insertSQLBuilder.append(", ");
                }
            }

            SQLiteDatabase database = Database.with(context).getWritableDatabase();

            boolean result = true;

            try {
                database.beginTransaction();
                database.execSQL("DELETE FROM " + Database.CountryTable.TABLE_NAME);
                database.execSQL(insertSQLBuilder.toString(), parameters);
                database.setTransactionSuccessful();
            } catch (Throwable t) {
                Log.error(getClass(), t, "Failed to save countries to database, transaction failed!");

                result = false;
            } finally {
                database.endTransaction();
            }

            database.close();

            return result;
        } catch (Throwable t) {
            Log.error(getClass(), t, "Failed to save countries to database!");

            return false;
        }
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
            Log.error(Country.class, t, "Failed to generate country from Json '%s'!", json);

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }
}
