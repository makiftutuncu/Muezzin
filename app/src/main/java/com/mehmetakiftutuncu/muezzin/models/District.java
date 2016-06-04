package com.mehmetakiftutuncu.muezzin.models;

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
import java.util.Locale;

/**
 * Created by akif on 08/05/16.
 */
public class District {
    public final int id;
    public final int cityId;
    public final String name;

    public District(int id, int cityId, String name) {
        this.id = id;
        this.cityId = cityId;
        this.name = name;
    }

    public static Optional<ArrayList<District>> getDistricts(Context context, int cityId) {
        try {
            ArrayList<District> districts = new ArrayList<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            Cursor cursor = database.rawQuery(
                    String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE %s = %d", Database.DistrictTable.TABLE_NAME, Database.DistrictTable.COLUMN_CITY_ID, cityId),
                    null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        int id      = cursor.getInt(cursor.getColumnIndex(Database.DistrictTable.COLUMN_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Database.DistrictTable.COLUMN_NAME));

                        District district = new District(id, cityId, name);

                        districts.add(district);

                        cursor.moveToNext();
                    }
                }

                cursor.close();
            }

            database.close();

            return new Some<>(districts);
        } catch (Throwable t) {
            Log.error(District.class, t, "Failed to get districts for city '%d' from database!", cityId);

            return new None<>();
        }
    }

    public static boolean saveDistricts(Context context, int cityId, ArrayList<District> districts) {
        try {
            int numberOfParametersToBind = 1;

            String[] parameters = new String[districts.size() * numberOfParametersToBind];
            StringBuilder insertSQLBuilder = new StringBuilder("INSERT INTO ")
                    .append(Database.DistrictTable.TABLE_NAME)
                    .append(" (")
                    .append(Database.DistrictTable.COLUMN_ID)
                    .append(", ")
                    .append(Database.DistrictTable.COLUMN_CITY_ID)
                    .append(", ")
                    .append(Database.DistrictTable.COLUMN_NAME)
                    .append(") VALUES ");

            for (int i = 0, size = districts.size(); i < size; i++) {
                District district = districts.get(i);

                insertSQLBuilder.append("(").append(district.id).append(", ").append(district.cityId).append(", ?)");

                parameters[i * numberOfParametersToBind] = district.name;

                if (i < size - 1) {
                    insertSQLBuilder.append(", ");
                }
            }

            SQLiteDatabase database = Database.with(context).getWritableDatabase();

            boolean result = true;

            try {
                database.beginTransaction();
                database.execSQL("DELETE FROM " + Database.DistrictTable.TABLE_NAME);
                database.execSQL(insertSQLBuilder.toString(), parameters);
                database.setTransactionSuccessful();
            } catch (Throwable t) {
                Log.error(District.class, t, "Failed to save districts for city '%d' to database, transaction failed!", cityId);

                result = false;
            } finally {
                database.endTransaction();
            }

            database.close();

            return result;
        } catch (Throwable t) {
            Log.error(District.class, t, "Failed to save districts for city '%d' to database!", cityId);

            return false;
        }
    }

    @NonNull public String toJson() {
        return String.format(Locale.ENGLISH, "{\"id\":%d,\"cityId\":%d,\"name\":\"%s\"}", id, cityId, name);
    }

    @NonNull public static Optional<District> fromJson(int cityId, String json) {
        try {
            return fromJson(cityId, new JSONObject(json));
        } catch (Throwable t) {
            Log.error(District.class, t, "Failed to generate district for city '%d' from Json '%s'!", cityId, json);

            return new None<>();
        }
    }

    @NonNull public static Optional<District> fromJson(int cityId, JSONObject json) {
        try {
            int id      = json.getInt("id");
            String name = json.getString("name");

            return new Some<>(new District(id, cityId, name));
        } catch (Throwable t) {
            Log.error(District.class, t, "Failed to generate district for city '%d' from Json '%s'!", cityId, json);

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }
}
