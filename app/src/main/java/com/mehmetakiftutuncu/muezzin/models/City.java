package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.github.mehmetakiftutuncu.toolbelt.Log;
import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.mehmetakiftutuncu.muezzin.database.Database;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;

import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by akif on 15/09/16.
 */
public final class City implements Comparable<City>, Parcelable {
    public final int id;
    public final String name;

    public final boolean isTurkish;

    public City(int id, @NonNull String name) {
        this.id   = id;
        this.name = name;

        this.isTurkish = isTurkish(id);
    }

    private City(Parcel parcel) {
        this(parcel.readInt(), parcel.readString());
    }

    public static Optional<List<City>> getCities(final Context context, int countryId) {
        try {
            List<City> cities = new ArrayList<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            Cursor cursor = database.rawQuery(
                String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE %s = %d ORDER BY %s", Database.CityTable.TABLE_NAME, Database.CityTable.COLUMN_COUNTRY_ID, countryId,  Database.CityTable.COLUMN_ID),
                null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        int id      = cursor.getInt(cursor.getColumnIndex(Database.CityTable.COLUMN_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Database.CityTable.COLUMN_NAME));

                        City city = new City(id, name);

                        cities.add(city);

                        cursor.moveToNext();
                    }
                }

                cursor.close();
            }

            database.close();

            Collator collator = LocaleUtils.getCollator(context);
            Collator turkishCollator = LocaleUtils.getTurkishCollator();

            Collections.sort(cities, (c1, c2) -> {
                if (c1.isTurkish) {
                    return turkishCollator.compare(c1.name, c2.name);
                } else {
                    return collator.compare(c1.name, c2.name);
                }
            });

            return Optional.with(cities);
        } catch (Throwable t) {
            Log.error(City.class, t, "Failed to get cities for country '%d' from database!", countryId);

            return Optional.empty();
        }
    }

    public static boolean saveCities(Context context, int countryId, List<City> cities) {
        try {
            int numberOfParametersToBind = 1;

            String[] parameters = new String[cities.size() * numberOfParametersToBind];
            StringBuilder insertSQLBuilder = new StringBuilder("INSERT INTO ")
                    .append(Database.CityTable.TABLE_NAME)
                    .append(" ('")
                    .append(Database.CityTable.COLUMN_ID)
                    .append("', '")
                    .append(Database.CityTable.COLUMN_COUNTRY_ID)
                    .append("', '")
                    .append(Database.CityTable.COLUMN_NAME)
                    .append("') VALUES ");

            for (int i = 0, size = cities.size(); i < size; i++) {
                City city = cities.get(i);

                insertSQLBuilder.append("(").append(city.id).append(", ").append(countryId).append(", ?)");

                parameters[i * numberOfParametersToBind] = city.name;

                if (i < size - 1) {
                    insertSQLBuilder.append(", ");
                }
            }

            SQLiteDatabase database = Database.with(context).getWritableDatabase();

            boolean result = true;

            try {
                database.beginTransaction();
                database.execSQL("DELETE FROM " + Database.CityTable.TABLE_NAME + " WHERE " + Database.CityTable.COLUMN_COUNTRY_ID + " = " + countryId);
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
        return String.format(
            Locale.ENGLISH,
            "{\"%d\":{\"name\":\"%s\"}}",
            id,
            name
        );
    }

    @NonNull public static City fromJson(int id, @NonNull JSONObject json) throws InvalidCityJsonException {
        try {
            String name = json.getString("name");

            return new City(id, name);
        } catch (Exception e) {
            throw new InvalidCityJsonException(String.format(Locale.ENGLISH, "Failed to parse City for id '%d' from Json: %s", id, json), e);
        }
    }

    public static boolean isTurkish(int cityId) {
        return cityId >= 500 && cityId <= 580;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        City that = (City) o;

        return compareTo(that) == 0;
    }

    @Override public int compareTo(@NonNull City that) {
        if (this.id < that.id) return -1; else if (this.id > that.id) return 1;

        int nameCompared = this.name.compareTo(that.name);
        if (nameCompared != 0) return nameCompared;

        return 0;
    }

    @Override public String toString() {
        return toJson();
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(name);
    }

    public static final Parcelable.Creator<City> CREATOR = new Parcelable.Creator<City>() {
        public City createFromParcel(Parcel parcel) {
            return new City(parcel);
        }

        public City[] newArray(int size) {
            return new City[size];
        }
    };

    public static class InvalidCityJsonException extends Exception {
        public InvalidCityJsonException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidCityJsonException(String message) {
            super(message);
        }
    }
}
