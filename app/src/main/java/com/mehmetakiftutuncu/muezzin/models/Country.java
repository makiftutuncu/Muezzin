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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by akif on 15/09/16.
 */
public final class Country implements Comparable<Country>, Parcelable {
    public final int id;
    public final String name;
    public final String nameTurkish;
    public final String nameNative;

    public final boolean isTurkey;

    public Country(int id, @NonNull String name, @NonNull String nameTurkish, @NonNull String nameNative) {
        this.id          = id;
        this.name        = name;
        this.nameTurkish = nameTurkish;
        this.nameNative  = nameNative;

        this.isTurkey = id == 2;
    }

    private Country(Parcel parcel) {
        this(parcel.readInt(), parcel.readString(), parcel.readString(), parcel.readString());
    }

    public String getLocalizedName(Context context) {
        if (LocaleUtils.isLanguageTurkish(context)) {
            return nameTurkish;
        } else {
            return name;
        }
    }

    public static Optional<List<Country>> getCountries(Context context) {
        try {
            String orderBy = LocaleUtils.isLanguageTurkish(context) ? Database.CountryTable.COLUMN_NAME_TURKISH : (LocaleUtils.isLanguageEnglish(context) ? Database.CountryTable.COLUMN_NAME : Database.CountryTable.COLUMN_NAME_NATIVE);

            List<Country> countries = new ArrayList<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            Optional<Country> Turkey = Optional.empty();
            Cursor cursor = database.rawQuery(String.format(Locale.ENGLISH, "SELECT * FROM %s ORDER BY %s", Database.CountryTable.TABLE_NAME, orderBy), null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        int id             = cursor.getInt(cursor.getColumnIndex(Database.CountryTable.COLUMN_ID));
                        String englishName = cursor.getString(cursor.getColumnIndex(Database.CountryTable.COLUMN_NAME));
                        String turkishName = cursor.getString(cursor.getColumnIndex(Database.CountryTable.COLUMN_NAME_TURKISH));
                        String nativeName  = cursor.getString(cursor.getColumnIndex(Database.CountryTable.COLUMN_NAME_NATIVE));

                        Country country = new Country(id, englishName, turkishName, nativeName);

                        if (country.isTurkey) {
                            Turkey = Optional.with(country);
                        } else {
                            countries.add(country);
                        }

                        cursor.moveToNext();
                    }

                }

                cursor.close();
            }

            if (Turkey.isDefined()) {
                countries.add(0, Turkey.get());
            }

            database.close();

            return Optional.with(countries);
        } catch (Throwable t) {
            Log.error(Country.class, t, "Failed to get countries from database!");

            return Optional.empty();
        }
    }

    public static boolean saveCountries(Context context, List<Country> countries) {
        try {
            int numberOfParametersToBind = 3;

            String[] parameters = new String[countries.size() * numberOfParametersToBind];
            StringBuilder insertSQLBuilder = new StringBuilder("INSERT INTO ")
                    .append(Database.CountryTable.TABLE_NAME)
                    .append(" ('")
                    .append(Database.CountryTable.COLUMN_ID)
                    .append("', '")
                    .append(Database.CountryTable.COLUMN_NAME)
                    .append("', '")
                    .append(Database.CountryTable.COLUMN_NAME_TURKISH)
                    .append("', '")
                    .append(Database.CountryTable.COLUMN_NAME_NATIVE)
                    .append("') VALUES ");

            for (int i = 0, size = countries.size(); i < size; i++) {
                Country country = countries.get(i);

                insertSQLBuilder.append("(").append(country.id).append(", ?, ?, ?)");

                parameters[i * numberOfParametersToBind]       = country.name;
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
                Log.error(Country.class, t, "Failed to save countries to database, transaction failed!");

                result = false;
            } finally {
                database.endTransaction();
            }

            database.close();

            return result;
        } catch (Throwable t) {
            Log.error(Country.class, t, "Failed to save countries to database!");

            return false;
        }
    }

    @NonNull public String toJson() {
        return String.format(
                Locale.ENGLISH,
                "{\"%d\":{\"name\":\"%s\",\"nameTurkish\":\"%s\",\"nameNative\":\"%s\"}}",
                id,
                name,
                nameTurkish,
                nameNative
        );
    }

    @NonNull public static Country fromJson(int id, @NonNull JSONObject json) throws InvalidCountryJsonException {
        try {
            String name        = json.getString("name");
            String nameTurkish = json.getString("nameTurkish");
            String nameNative  = json.getString("nameNative");

            return new Country(id, name, nameTurkish, nameNative);
        } catch (Exception e) {
            throw new InvalidCountryJsonException(String.format(Locale.ENGLISH, "Failed to parse Country for id '%d' from Json: %s", id, json), e);
        }
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Country that = (Country) o;

        return compareTo(that) == 0;
    }

    @Override public int compareTo(@NonNull Country that) {
        if (this.id < that.id) return -1; else if (this.id > that.id) return 1;

        int nameCompared = this.name.compareTo(that.name);
        if (nameCompared != 0) return nameCompared;

        int nameTurkishCompared = this.nameTurkish.compareTo(that.nameTurkish);
        if (nameTurkishCompared != 0) return nameTurkishCompared;

        int nameNativeCompared = this.nameNative.compareTo(that.nameNative);
        if (nameNativeCompared != 0) return nameNativeCompared;

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
        parcel.writeString(nameTurkish);
        parcel.writeString(nameNative);
    }

    public static final Parcelable.Creator<Country> CREATOR = new Parcelable.Creator<Country>() {
        public Country createFromParcel(Parcel parcel) {
            return new Country(parcel);
        }

        public Country[] newArray(int size) {
            return new Country[size];
        }
    };

    public static class InvalidCountryJsonException extends Exception {
        public InvalidCountryJsonException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidCountryJsonException(String message) {
            super(message);
        }
    }
}
