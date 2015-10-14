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

public class City {
    public static final String TAG = "City";

    public final int id;
    public final String name;

    public static String fileName(int countryId) {
        return TAG + "." + countryId;
    }

    public City(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public static Option<ArrayList<City>> loadAll(int countryId) {
        Log.info(TAG, "Loading all cities for country " + countryId + "...");

        Option<ArrayList<City>> fromCache = Cache.City.getList(countryId);

        if (fromCache.isDefined) {
            return fromCache;
        }

        if (FileUtils.dataPath.isEmpty) {
            Log.error(TAG, "Failed to load all cities for country " + countryId + ", data path is None!");

            return new None<>();
        }

        Option<String> data = FileUtils.readFile(fileName(countryId));

        if (data.isEmpty) {
            return new None<>();
        }

        if (StringUtils.isEmpty(data.get())) {
            Log.error(TAG, "Failed to load all cities for country " + countryId + ", loaded data are empty!");

            return new None<>();
        }

        try {
            ArrayList<City> cities = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(data.get());

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Option<City> city = fromJson(jsonObject);

                if (city.isDefined) {
                    cities.add(city.get());
                }
            }

            Cache.City.setList(countryId, cities);

            return new Some<>(cities);
        } catch (Throwable t) {
            Log.error(TAG, "Failed to load all cities for country " + countryId + ", cannot construct cities from " + data.get() + "!", t);

            return new None<>();
        }
    }

    public static Option<City> get(int countryId, int cityId) {
        Log.info(TAG, "Getting city " + cityId + " for country " + countryId + "...");

        Option<City> fromCache = Cache.City.get(countryId, cityId);

        if (fromCache.isDefined) {
            return fromCache;
        }

        Option<ArrayList<City>> citiesOption = loadAll(countryId);

        if (citiesOption.isEmpty) {
            return new None<>();
        }

        try {
            ArrayList<City> cities = citiesOption.get();

            for (int i = 0, size = cities.size(); i < size; i++) {
                City city = cities.get(i);

                if (city.id == cityId) {
                    Cache.City.set(countryId, cityId, city);

                    return new Some<>(city);
                }
            }

            return new None<>();
        } catch (Throwable t) {
            Log.error(TAG, "Failed to get city " + cityId + " for country " + countryId + "!", t);

            return new None<>();
        }
    }

    public static boolean saveAll(ArrayList<City> cities, int countryId) {
        Log.info(TAG, "Saving all cities for country " + countryId + "...");

        if (FileUtils.dataPath.isEmpty) {
            Log.error(TAG, "Failed to save all cities for country " + countryId + ", data path is None!");

            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("[");

        for (int i = 0, size = cities.size(); i < size; i++) {
            Option<JSONObject> json = cities.get(i).toJson();

            if (json.isDefined) {
                stringBuilder.append(json.toString());

                if (i != size - 1) {
                    stringBuilder.append(", ");
                }
            }
        }

        stringBuilder.append("]");

        boolean result = FileUtils.writeFile(stringBuilder.toString(), fileName(countryId));

        if (result) {
            Cache.City.setList(countryId, cities);
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

    public static Option<City> fromJson(JSONObject json) {
        try {
            int id      = json.getInt("id");
            String name = json.getString("name");

            return new Some<>(new City(id, name));
        } catch (Throwable t) {
            Log.error(TAG, "Failed to create city from Json " + json.toString() + "!", t);

            return new None<>();
        }
    }

    public static Option<ArrayList<City>> fromJsonArray(JSONArray jsonArray) {
        try {
            ArrayList<City> cities = new ArrayList<>();

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Option<City> city = fromJson(jsonObject);

                if (city.isDefined) {
                    cities.add(city.get());
                }
            }

            return new Some<>(cities);
        } catch (Throwable t) {
            Log.error(TAG, "Failed to create cities from Json array " + jsonArray.toString() + "!", t);

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
