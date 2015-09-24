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

public class District {
    public final int id;
    public final String name;

    private static String fileName(int countryId, int cityId) {
        return "districts." + countryId + "." + cityId;
    }

    public District(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public static Option<ArrayList<District>> loadAll(int countryId, int cityId) {
        Log.info(District.class, "Loading all districts for country " + countryId + " and city " + cityId + "...");

        if (FileUtils.dataPath == null) {
            Log.error(District.class, "Failed to load all districts for country " + countryId + " and city " + cityId + ", data path is null!");

            return new None<>();
        }

        Option<String> data = FileUtils.readFile(fileName(countryId, cityId));

        if (StringUtils.isEmpty(data.getOrElse(""))) {
            Log.error(District.class, "Failed to load all districts for country " + countryId + " and city " + cityId + ", loaded data is None or empty!");

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

            return new Some<>(districts);
        } catch (Throwable t) {
            Log.error(District.class, "Failed to load all districts for country " + countryId + " and city " + cityId + ", cannot construct districts from " + data.get() + "!", t);

            return new None<>();
        }
    }

    public static boolean saveAll(ArrayList<District> districts, int countryId, int cityId) {
        Log.info(District.class, "Saving all districts for country " + countryId + "...");

        if (FileUtils.dataPath.isEmpty) {
            Log.error(District.class, "Failed to save all districts for country " + countryId + " and city " + cityId + ", data path is None!");

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

    public static Option<District> fromJson(JSONObject json) {
        try {
            int id      = json.getInt("id");
            String name = json.getString("name");

            return new Some<>(new District(id, name));
        } catch (Throwable t) {
            Log.error(District.class, "Failed to create district from Json " + json.toString() + "!", t);

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
            Log.error(District.class, "Failed to create districts from Json array " + jsonArray.toString() + "!", t);

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
