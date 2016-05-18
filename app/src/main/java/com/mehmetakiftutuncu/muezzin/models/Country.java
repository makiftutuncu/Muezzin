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
import java.util.Locale;

/**
 * Created by akif on 08/05/16.
 */
public class Country {
    public final int id;
    public final String englishName;
    public final String turkishName;
    public final String nativeName;

    public Country(int id, String englishName, String turkishName, String nativeName) {
        this.id = id;
        this.englishName = englishName;
        this.turkishName = turkishName;
        this.nativeName = nativeName;
    }

    public String getLocalizedName(Context context) {
        if (LocaleUtils.isLanguageTurkish(context)) {
            return turkishName;
        } else if (LocaleUtils.isLanguageEnglish(context)) {
            return turkishName;
        } else {
            return String.format("%s (%s)", englishName, nativeName);
        }
    }

    public static Optional<ArrayList<Country>> getCountries(Context context) {
        try {
            String orderBy = LocaleUtils.isLanguageTurkish(context) ? Database.CountryTable.COLUMN_TURKISH_NAME : Database.CountryTable.COLUMN_ENGLISH_NAME;;

            ArrayList<Country> countries = new ArrayList<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            Cursor cursor = database.rawQuery(String.format(Locale.ENGLISH, "SELECT * FROM %s ORDER BY %s", Database.CountryTable.TABLE_NAME, orderBy), null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        int id             = cursor.getInt(cursor.getColumnIndex(Database.CountryTable.COLUMN_ID));
                        String englishName = cursor.getString(cursor.getColumnIndex(Database.CountryTable.COLUMN_ENGLISH_NAME));
                        String turkishName = cursor.getString(cursor.getColumnIndex(Database.CountryTable.COLUMN_TURKISH_NAME));
                        String nativeName  = cursor.getString(cursor.getColumnIndex(Database.CountryTable.COLUMN_NATIVE_NAME));

                        Country country = new Country(id, englishName, turkishName, nativeName);

                        countries.add(country);

                        cursor.moveToNext();
                    }

                }

                cursor.close();
            }

            database.close();

            return new Some<>(countries);
        } catch (Throwable t) {
            Log.error(Country.class, t, "Failed to get countries from database!");

            return new None<>();
        }
    }

    public static boolean saveCountries(Context context, ArrayList<Country> countries) {
        try {
            int numberOfParametersToBind = 3;

            String[] parameters = new String[countries.size() * numberOfParametersToBind];
            StringBuilder insertSQLBuilder = new StringBuilder("INSERT INTO ")
                    .append(Database.CountryTable.TABLE_NAME)
                    .append(" (")
                    .append(Database.CountryTable.COLUMN_ID)
                    .append(", ")
                    .append(Database.CountryTable.COLUMN_ENGLISH_NAME)
                    .append(", ")
                    .append(Database.CountryTable.COLUMN_TURKISH_NAME)
                    .append(", ")
                    .append(Database.CountryTable.COLUMN_NATIVE_NAME)
                    .append(") VALUES ");

            for (int i = 0, size = countries.size(); i < size; i++) {
                Country country = countries.get(i);

                insertSQLBuilder.append("(").append(country.id).append(", ?, ?, ?)");

                parameters[i * numberOfParametersToBind]       = country.englishName;
                parameters[(i * numberOfParametersToBind) + 1] = country.turkishName;
                parameters[(i * numberOfParametersToBind) + 2] = country.nativeName;

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
        return String.format(Locale.ENGLISH, "{\"id\":%d,\"englishName\":\"%s\",\"turkishName\":\"%s\",\"nativeName\":\"%s\"}", id, englishName, turkishName, nativeName);
    }

    @NonNull public static Optional<Country> fromJson(JSONObject json) {
        try {
            int id             = json.getInt("id");
            String englishName = json.getString("name");
            String turkishName = json.getString("trName");
            String nativeName  = json.getString("nativeName");

            return new Some<>(new Country(id, englishName, turkishName, nativeName));
        } catch (Throwable t) {
            Log.error(Country.class, t, "Failed to generate country from Json '%s'!", json);

            return new None<>();
        }
    }

    @Override public String toString() {
        return toJson();
    }
}
