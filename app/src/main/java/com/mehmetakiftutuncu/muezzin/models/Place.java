package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.mehmetakiftutuncu.muezzin.database.Database;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by akif on 16/05/16.
 */
public class Place {
    private static final String EXTRA_COUNTRY_ID  = "countryId";
    private static final String EXTRA_CITY_ID     = "cityId";
    private static final String EXTRA_DISTRICT_ID = "districtId";

    public final int countryId;
    public final int cityId;
    public final Optional<Integer> districtId;

    public Place(int countryId, int cityId, Optional<Integer> districtId) {
        this.countryId  = countryId;
        this.cityId     = cityId;
        this.districtId = districtId;
    }

    public Optional<String> getPlaceName(Context context) {
        try {
            Optional<String> placeName = new None<>();

            String countryNameColumnName = LocaleUtils.isLanguageTurkish(context) ? Database.CountryTable.COLUMN_TURKISH_NAME : Database.CountryTable.COLUMN_ENGLISH_NAME;

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            String query;
            if (districtId.isDefined) {
                query = String.format(Locale.ENGLISH,
                        "SELECT co.%s AS countryName, ci.%s AS cityName, di.%s AS districtName FROM %s AS co JOIN %s AS ci ON (co.%s = ci.%s) JOIN %s AS di ON (ci.%s = di.%s) WHERE co.%s = %d AND ci.%s = %d AND di.%s = %d",
                        countryNameColumnName,
                        Database.CityTable.COLUMN_NAME,
                        Database.DistrictTable.COLUMN_NAME,
                        Database.CountryTable.TABLE_NAME,
                        Database.CityTable.TABLE_NAME,
                        Database.CountryTable.COLUMN_ID,
                        Database.CityTable.COLUMN_COUNTRY_ID,
                        Database.DistrictTable.TABLE_NAME,
                        Database.CityTable.COLUMN_ID,
                        Database.DistrictTable.COLUMN_CITY_ID,
                        Database.CountryTable.COLUMN_ID,
                        countryId,
                        Database.CityTable.COLUMN_ID,
                        cityId,
                        Database.DistrictTable.COLUMN_ID,
                        districtId.get()
                );
            } else {
                query = String.format(Locale.ENGLISH,
                        "SELECT co.%s AS countryName, ci.%s AS cityName FROM %s AS co JOIN %s AS ci ON (co.%s = ci.%s) WHERE co.%s = %d AND ci.%s = %d",
                        countryNameColumnName,
                        Database.CityTable.COLUMN_NAME,
                        Database.CountryTable.TABLE_NAME,
                        Database.CityTable.TABLE_NAME,
                        Database.CountryTable.COLUMN_ID,
                        Database.CityTable.COLUMN_COUNTRY_ID,
                        Database.CountryTable.COLUMN_ID,
                        countryId,
                        Database.CityTable.COLUMN_ID,
                        cityId
                );
            }

            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    String countryName  = cursor.getString(cursor.getColumnIndex("countryName"));
                    String cityName     = cursor.getString(cursor.getColumnIndex("cityName"));
                    String districtName = districtId.isDefined ? cursor.getString(cursor.getColumnIndex("districtName")) : "";

                    placeName = new Some<>(String.format(Locale.ENGLISH, "%s%s, %s", (districtName.isEmpty() ? "": (districtName + ", ")), cityName, countryName));
                }

                cursor.close();
            }

            database.close();

            return placeName;
        } catch (Throwable t) {
            Log.error(getClass(), t, "Failed to get name for place '%s'!", this);

            return new None<>();
        }
    }

    @NonNull public String toJson() {
        return String.format(Locale.ENGLISH, "{\"countryId\":%d,\"cityId\":%d%s}", countryId, cityId, districtId.isDefined ? String.format(Locale.ENGLISH, ",\"districtId\":%d", districtId.get()) : "");
    }

    @NonNull public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putInt(EXTRA_COUNTRY_ID, countryId);
        bundle.putInt(EXTRA_CITY_ID, cityId);

        if (districtId.isDefined) {
            bundle.putInt(EXTRA_DISTRICT_ID, districtId.get());
        }

        return bundle;
    }

    @NonNull public static Optional<Place> fromJson(JSONObject json) {
        try {
            int countryId                = json.getInt("countryId");
            int cityId                   = json.getInt("cityId");
            Optional<Integer> districtId = json.optInt("districtId", 0) > 0 ? new Some<>(json.getInt("districtId")) : new None<Integer>();

            return new Some<>(new Place(countryId, cityId, districtId));
        } catch (Throwable t) {
            Log.error(Place.class, t, "Failed to generate place from Json '%s'!", json);

            return new None<>();
        }
    }

    @NonNull public static Optional<Place> fromBundle(Bundle bundle) {
        if (bundle == null) {
            return new None<>();
        }

        int countryId                = bundle.getInt(EXTRA_COUNTRY_ID);
        int cityId                   = bundle.getInt(EXTRA_CITY_ID);
        Optional<Integer> districtId = bundle.containsKey(EXTRA_DISTRICT_ID) ? new Some<>(bundle.getInt(EXTRA_DISTRICT_ID)) : new None<Integer>();

        return new Some<>(new Place(countryId, cityId, districtId));
    }

    @Override public String toString() {
        return toJson();
    }
}
