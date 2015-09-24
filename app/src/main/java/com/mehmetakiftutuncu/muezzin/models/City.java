package com.mehmetakiftutuncu.muezzin.models;

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
    public final int id;
    public final String name;

    private static String fileName(int countryId) {
        return "cities." + countryId;
    }

    public City(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public static Option<ArrayList<City>> loadAll(int countryId) {
        Log.info(City.class, "Loading all cities for country " + countryId + "...");

        if (FileUtils.dataPath == null) {
            Log.error(City.class, "Failed to load all cities for country " + countryId + ", data path is null!");

            return new None<>();
        }

        Option<String> data = FileUtils.readFile(fileName(countryId));

        if (StringUtils.isEmpty(data.getOrElse(""))) {
            Log.error(City.class, "Failed to load all cities for country " + countryId + ", loaded data is None or empty!");

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

            return new Some<>(cities);
        } catch (Throwable t) {
            Log.error(City.class, "Failed to load all cities for country " + countryId + ", cannot construct cities from " + data.get() + "!", t);

            return new None<>();
        }
    }

    public static boolean saveAll(ArrayList<City> cities, int countryId) {
        Log.info(City.class, "Saving all cities for country " + countryId + "...");

        if (FileUtils.dataPath.isEmpty) {
            Log.error(City.class, "Failed to save all cities for country " + countryId + ", data path is None!");

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

        return result;
    }

    public Option<JSONObject> toJson() {
        try {
            JSONObject result = new JSONObject();

            result.put("id",   id);
            result.put("name", name);

            return new Some<>(result);
        } catch (Throwable t) {
            Log.error(this, "Failed to convert " + toString() + " to Json!", t);

            return new None<>();
        }
    }

    public static Option<City> fromJson(JSONObject json) {
        try {
            int id      = json.getInt("id");
            String name = json.getString("name");

            return new Some<>(new City(id, name));
        } catch (Throwable t) {
            Log.error(City.class, "Failed to create city from Json " + json.toString() + "!", t);

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
            Log.error(City.class, "Failed to create cities from Json array " + jsonArray.toString() + "!", t);

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
