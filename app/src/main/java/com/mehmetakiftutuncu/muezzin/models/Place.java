package com.mehmetakiftutuncu.muezzin.models;

import android.os.Bundle;
import android.support.annotation.NonNull;

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
