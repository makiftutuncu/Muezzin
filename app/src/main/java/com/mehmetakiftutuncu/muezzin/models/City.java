package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.database.Database;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by akif on 08/05/16.
 */
public class City {
    public final int id;
    public final int countryId;
    public final String name;

    public City(int id, int countryId, String name) {
        this.id = id;
        this.countryId = countryId;
        this.name = name;
    }

    public static Optional<ArrayList<City>> getCities(final Context context, int countryId) {
        try {
            ArrayList<City> cities = new ArrayList<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            Cursor cursor = database.rawQuery(
                    String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE %s = %d", Database.CityTable.TABLE_NAME, Database.CityTable.COLUMN_COUNTRY_ID, countryId),
                    null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        int id      = cursor.getInt(cursor.getColumnIndex(Database.CityTable.COLUMN_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Database.CityTable.COLUMN_NAME));

                        City city = new City(id, countryId, name);

                        cities.add(city);

                        cursor.moveToNext();
                    }
                }

                cursor.close();
            }

            database.close();

            Collections.sort(cities, new Comparator<City>() {
                @Override public int compare(City lhs, City rhs) {
                    return LocaleUtils.getCollator(context).compare(lhs.name, rhs.name);
                }
            });

            return new Some<>(cities);
        } catch (Throwable t) {
            Log.error(City.class, t, "Failed to get cities for country '%d' from database!", countryId);

            return new None<>();
        }
    }

    public static boolean saveCities(Context context, int countryId, ArrayList<City> cities) {
        try {
            int numberOfParametersToBind = 1;

            String[] parameters = new String[cities.size() * numberOfParametersToBind];
            StringBuilder insertSQLBuilder = new StringBuilder("INSERT INTO ")
                    .append(Database.CityTable.TABLE_NAME)
                    .append(" (")
                    .append(Database.CityTable.COLUMN_ID)
                    .append(", ")
                    .append(Database.CityTable.COLUMN_COUNTRY_ID)
                    .append(", ")
                    .append(Database.CityTable.COLUMN_NAME)
                    .append(") VALUES ");

            for (int i = 0, size = cities.size(); i < size; i++) {
                City city = cities.get(i);

                insertSQLBuilder.append("(").append(city.id).append(", ").append(city.countryId).append(", ?)");

                parameters[i * numberOfParametersToBind] = city.name;

                if (i < size - 1) {
                    insertSQLBuilder.append(", ");
                }
            }

            SQLiteDatabase database = Database.with(context).getWritableDatabase();

            boolean result = true;

            try {
                database.beginTransaction();
                database.execSQL("DELETE FROM " + Database.CityTable.TABLE_NAME);
                database.execSQL(insertSQLBuilder.toString(), parameters);
                database.setTransactionSuccessful();
            } catch (Throwable t) {
                Log.error(City.class, t, "Failed to save cities for country '%d' to database, transaction failed!", countryId);

                result = false;
            } finally {
                database.endTransaction();
            }

            database.close();

            return result;
        } catch (Throwable t) {
            Log.error(City.class, t, "Failed to save cities for country '%d' to database!", countryId);

            return false;
        }
    }

    @NonNull public String toJson() {
        return String.format(Locale.ENGLISH, "{\"id\":%d,\"countryId\":%d,\"name\":\"%s\"}", id, countryId, name);
    }

    @NonNull public static Optional<City> fromJson(int countryId, String json) {
        try {
            return fromJson(countryId, new JSONObject(json));
        } catch (Throwable t) {
            Log.error(City.class, t, "Failed to generate city for country '%d' from Json '%s'!", countryId, json);

            return new None<>();
        }
    }

    @NonNull public static Optional<City> fromJson(int countryId, JSONObject json) {
        try {
            int id      = json.getInt("id");
            String name = json.getString("name");

            return new Some<>(new City(id, countryId, name));
        } catch (Throwable t) {
            Log.error(City.class, t, "Failed to generate city for country '%d' from Json '%s'!", countryId, json);

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }
}
