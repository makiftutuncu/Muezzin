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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by akif on 15/09/16.
 */
public final class District implements Comparable<District>, Parcelable {
    public final int id;
    public final String name;

    public District(int id, @NonNull String name) {
        this.id   = id;
        this.name = name;
    }

    private District(Parcel parcel) {
        this(parcel.readInt(), parcel.readString());
    }

    public static Optional<List<District>> getDistricts(Context context, int cityId) {
        try {
            List<District> districts = new ArrayList<>();

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            Cursor cursor = database.rawQuery(
                String.format(Locale.ENGLISH, "SELECT * FROM %s WHERE %s = %d ORDER BY %s", Database.DistrictTable.TABLE_NAME, Database.DistrictTable.COLUMN_CITY_ID, cityId, Database.DistrictTable.COLUMN_ID),
                null
            );

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    while (!cursor.isAfterLast()) {
                        int id      = cursor.getInt(cursor.getColumnIndex(Database.DistrictTable.COLUMN_ID));
                        String name = cursor.getString(cursor.getColumnIndex(Database.DistrictTable.COLUMN_NAME));

                        District district = new District(id, name);

                        districts.add(district);

                        cursor.moveToNext();
                    }
                }

                cursor.close();
            }

            database.close();

            Collator collator = LocaleUtils.getCollator(context);
            Collator turkishCollator = LocaleUtils.getTurkishCollator();

            boolean isTurkish = City.isTurkish(cityId);

            Collections.sort(districts, (d1, d2) -> {
                if (isTurkish) {
                    return turkishCollator.compare(d1.name, d2.name);
                } else {
                    return collator.compare(d1.name, d2.name);
                }
            });

            return Optional.with(districts);
        } catch (Throwable t) {
            Log.error(District.class, t, "Failed to get districts for city '%d' from database!", cityId);

            return Optional.empty();
        }
    }

    public static boolean saveDistricts(Context context, int cityId, List<District> districts) {
        try {
            int numberOfParametersToBind = 1;

            String[] parameters = new String[districts.size() * numberOfParametersToBind];
            StringBuilder insertSQLBuilder = new StringBuilder("INSERT INTO ")
                    .append(Database.DistrictTable.TABLE_NAME)
                    .append(" ('")
                    .append(Database.DistrictTable.COLUMN_ID)
                    .append("', '")
                    .append(Database.DistrictTable.COLUMN_CITY_ID)
                    .append("', '")
                    .append(Database.DistrictTable.COLUMN_NAME)
                    .append("') VALUES ");

            for (int i = 0, size = districts.size(); i < size; i++) {
                District district = districts.get(i);

                insertSQLBuilder.append("(").append(district.id).append(", ").append(cityId).append(", ?)");

                parameters[i * numberOfParametersToBind] = district.name;

                if (i < size - 1) {
                    insertSQLBuilder.append(", ");
                }
            }

            SQLiteDatabase database = Database.with(context).getWritableDatabase();

            boolean result = true;

            try {
                database.beginTransaction();
                database.execSQL("DELETE FROM " + Database.DistrictTable.TABLE_NAME + " WHERE " + Database.DistrictTable.COLUMN_CITY_ID + " = " + cityId);
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
        return String.format(
            Locale.ENGLISH,
            "{\"%d\":{\"name\":\"%s\"}}",
            id,
            name
        );
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        District that = (District) o;

        return compareTo(that) == 0;
    }

    @Override public int compareTo(@NonNull District that) {
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

    public static final Parcelable.Creator<District> CREATOR = new Parcelable.Creator<District>() {
        public District createFromParcel(Parcel parcel) {
            return new District(parcel);
        }

        public District[] newArray(int size) {
            return new District[size];
        }
    };

    public static class InvalidDistrictJsonException extends Exception {
        public InvalidDistrictJsonException(String message, Throwable cause) {
            super(message, cause);
        }

        public InvalidDistrictJsonException(String message) {
            super(message);
        }
    }
}
