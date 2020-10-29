package com.mehmetakiftutuncu.muezzin.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.github.mehmetakiftutuncu.toolbelt.Log;
import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.mehmetakiftutuncu.muezzin.database.Database;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;

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

    public Optional<String> getPlaceName(Context context, boolean isFullNameRequired) {
        try {
            Optional<String> placeName = Optional.empty();

            String countryNameColumnName = LocaleUtils.isLanguageTurkish(context) ? Database.CountryTable.COLUMN_NAME_TURKISH : Database.CountryTable.COLUMN_NAME;

            SQLiteDatabase database = Database.with(context).getReadableDatabase();

            String query;
            if (isFullNameRequired) {
                if (districtId.isDefined()) {
                    query = String.format(
                        Locale.ENGLISH,
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
                    query = String.format(
                        Locale.ENGLISH,
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
            } else {
                if (districtId.isDefined()) {
                    query = String.format(
                        Locale.ENGLISH,
                        "SELECT di.%s AS districtName FROM %s AS co JOIN %s AS ci ON (co.%s = ci.%s) JOIN %s AS di ON (ci.%s = di.%s) WHERE co.%s = %d AND ci.%s = %d AND di.%s = %d",
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
                    query = String.format(
                        Locale.ENGLISH,
                        "SELECT ci.%s AS cityName FROM %s AS co JOIN %s AS ci ON (co.%s = ci.%s) WHERE co.%s = %d AND ci.%s = %d",
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
            }

            Cursor cursor = database.rawQuery(query, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    if (isFullNameRequired) {
                        String countryName  = cursor.getString(cursor.getColumnIndex("countryName"));
                        String cityName     = cursor.getString(cursor.getColumnIndex("cityName"));
                        String districtName = districtId.isDefined() ? cursor.getString(cursor.getColumnIndex("districtName")) : "";

                        placeName = Optional.with(String.format(Locale.ENGLISH, "%s%s, %s", ((districtName.isEmpty() || districtName.equals(cityName)) ? "": (districtName + ", ")), cityName, countryName));
                    } else {
                        if (districtId.isDefined()) {
                            String districtName = cursor.getString(cursor.getColumnIndex("districtName"));

                            placeName = Optional.with(districtName);
                        } else {
                            String cityName = cursor.getString(cursor.getColumnIndex("cityName"));

                            placeName = Optional.with(cityName);
                        }
                    }
                }

                cursor.close();
            }

            database.close();

            return placeName;
        } catch (Throwable t) {
            Log.error(getClass(), t, "Failed to get name for place '%s'!", this);

            return Optional.empty();
        }
    }

    public Optional<String> getPlaceName(Context context) {
        return getPlaceName(context, true);
    }

    @NonNull public String toJson() {
        return String.format(Locale.ENGLISH, "{\"countryId\":%d,\"cityId\":%d%s}", countryId, cityId, districtId.isDefined() ? String.format(Locale.ENGLISH, ",\"districtId\":%d", districtId.get()) : "");
    }

    @NonNull public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putInt(EXTRA_COUNTRY_ID, countryId);
        bundle.putInt(EXTRA_CITY_ID, cityId);

        if (districtId.isDefined()) {
            bundle.putInt(EXTRA_DISTRICT_ID, districtId.get());
        }

        return bundle;
    }

    @NonNull public static Optional<Place> fromJson(JSONObject json) {
        try {
            int countryId                = json.getInt("countryId");
            int cityId                   = json.getInt("cityId");
            Optional<Integer> districtId = json.optInt("districtId", 0) > 0 ? Optional.with(json.getInt("districtId")) : Optional.<Integer>empty();

            return Optional.with(new Place(countryId, cityId, districtId));
        } catch (Throwable t) {
            Log.error(Place.class, t, "Failed to generate place from Json '%s'!", json);

            return Optional.empty();
        }
    }

    @NonNull public static Optional<Place> fromBundle(Bundle bundle) {
        if (bundle == null) {
            return Optional.empty();
        }

        int countryId                = bundle.getInt(EXTRA_COUNTRY_ID);
        int cityId                   = bundle.getInt(EXTRA_CITY_ID);
        Optional<Integer> districtId = bundle.containsKey(EXTRA_DISTRICT_ID) ? Optional.with(bundle.getInt(EXTRA_DISTRICT_ID)) : Optional.<Integer>empty();

        return Optional.with(new Place(countryId, cityId, districtId));
    }

    @Override public String toString() {
        return toJson();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Place)) return false;

        Place that = (Place) o;

        return  this.countryId  == that.countryId &&
                this.cityId     == that.cityId &&
                this.districtId == that.districtId;
    }
}
