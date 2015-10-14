package com.mehmetakiftutuncu.muezzin.models;

import com.mehmetakiftutuncu.muezzin.utilities.Cache;
import com.mehmetakiftutuncu.muezzin.utilities.FileUtils;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.StringUtils;
import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.mehmetakiftutuncu.muezzin.utilities.option.Some;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class District {
    public static final String TAG = "District";

    public final int id;
    public final String name;

    public static String fileName(int countryId, int cityId) {
        return TAG + "." + countryId + "." + cityId;
    }

    public District(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public static Option<ArrayList<District>> loadAll(int countryId, int cityId) {
        Log.info(TAG, "Loading all districts for country " + countryId + " and city " + cityId + "...");

        Option<ArrayList<District>> fromCache = Cache.District.getList(countryId, cityId);

        if (fromCache.isDefined) {
            return fromCache;
        }

        if (FileUtils.dataPath.isEmpty) {
            Log.error(TAG, "Failed to load all districts for country " + countryId + " and city " + cityId + ", data path is None!");

            return new None<>();
        }

        Option<String> data = FileUtils.readFile(fileName(countryId, cityId));

        if (data.isEmpty) {
            return new None<>();
        }

        if (StringUtils.isEmpty(data.get())) {
            Log.error(TAG, "Failed to load all districts for country " + countryId + " and city " + cityId + ", loaded data are None or empty!");

            return new None<>();
        }

        try {
            ArrayList<District> districts = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(data.get());

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Option<District> district = fromJson(jsonObject);

                if (district.isDefined) {
                    districts.add(district.get());
                }
            }

            Cache.District.setList(countryId, cityId, districts);

            return new Some<>(districts);
        } catch (Throwable t) {
            Log.error(TAG, "Failed to load all districts for country " + countryId + " and city " + cityId + ", cannot construct districts from " + data.get() + "!", t);

            return new None<>();
        }
    }

    public static Option<District> get(int countryId, int cityId, int districtId) {
        Log.info(TAG, "Getting district " + districtId + " for city " + cityId + " and country " + countryId + "...");

        Option<District> fromCache = Cache.District.get(countryId, cityId, districtId);

        if (fromCache.isDefined) {
            return fromCache;
        }

        Option<ArrayList<District>> districtsOption = loadAll(countryId, cityId);

        if (districtsOption.isEmpty) {
            return new None<>();
        }

        try {
            ArrayList<District> districts = districtsOption.get();

            for (int i = 0, size = districts.size(); i < size; i++) {
                District district = districts.get(i);

                if (district.id == districtId) {
                    Cache.District.set(countryId, cityId, district);

                    return new Some<>(district);
                }
            }

            return new None<>();
        } catch (Throwable t) {
            Log.error(TAG, "Failed to get district " + districtId + "for city " + cityId + " and country " + countryId + "!", t);

            return new None<>();
        }
    }

    public static boolean saveAll(ArrayList<District> districts, int countryId, int cityId) {
        Log.info(TAG, "Saving all districts for country " + countryId + " and city " + cityId + "...");

        if (FileUtils.dataPath.isEmpty) {
            Log.error(TAG, "Failed to save all districts for country " + countryId + " and city " + cityId + ", data path is None!");

            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("[");

        for (int i = 0, size = districts.size(); i < size; i++) {
            Option<JSONObject> json = districts.get(i).toJson();

            if (json.isDefined) {
                stringBuilder.append(json.toString());

                if (i != size - 1) {
                    stringBuilder.append(", ");
                }
            }
        }

        stringBuilder.append("]");

        boolean result = FileUtils.writeFile(stringBuilder.toString(), fileName(countryId, cityId));

        if (result) {
            Cache.District.setList(countryId, cityId, districts);
        }

        return result;
    }

    public Option<JSONObject> toJson() {
        try {
            JSONObject result = new JSONObject();

            result.put("id",   id);
            result.put("name", name);

            return new Some<>(result);
        } catch (Throwable t) {
            Log.error(TAG, "Failed to convert " + toString() + " to Json!", t);

            return new None<>();
        }
    }

    public static Option<District> fromJson(JSONObject json) {
        try {
            int id      = json.getInt("id");
            String name = json.getString("name");

            return new Some<>(new District(id, name));
        } catch (Throwable t) {
            Log.error(TAG, "Failed to create district from Json " + json.toString() + "!", t);

            return new None<>();
        }
    }

    public static Option<ArrayList<District>> fromJsonArray(JSONArray jsonArray) {
        try {
            ArrayList<District> districts = new ArrayList<>();

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Option<District> district = fromJson(jsonObject);

                if (district.isDefined) {
                    districts.add(district.get());
                }
            }

            return new Some<>(districts);
        } catch (Throwable t) {
            Log.error(TAG, "Failed to create districts from Json array " + jsonArray.toString() + "!", t);

            return new None<>();
        }
    }

    @Override public String toString() {
        return String.format(
                "{\"id\": %d, \"name\": \"%s\"}",
                id, name
        );
    }
}
