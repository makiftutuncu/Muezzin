package com.mehmetakiftutuncu.muezzin.models;

import com.mehmetakiftutuncu.muezzin.utilities.FileUtils;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;
import com.mehmetakiftutuncu.muezzin.utilities.Log;
import com.mehmetakiftutuncu.muezzin.utilities.StringUtils;
import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.mehmetakiftutuncu.muezzin.utilities.option.Some;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class Country {
    public final int id;
    public final String name;
    public final String trName;
    public final String nativeName;

    private static String fileName = "countries";

    public Country(int id, String name, String trName, String nativeName) {
        this.id         = id;
        this.name       = name;
        this.trName     = trName;
        this.nativeName = nativeName;
    }

    public String localizedName() {
        if (LocaleUtils.isLanguageTr()) {
            return trName;
        }

        return name;
    }

    public static Option<ArrayList<Country>> loadAll() {
        Log.info(Country.class, "Loading all countries...");

        if (FileUtils.dataPath == null) {
            Log.error(Country.class, "Failed to load all countries, data path is null!");

            return new None<>();
        }

        Option<String> data = FileUtils.readFile(fileName);

        if (StringUtils.isEmpty(data.getOrElse(""))) {
            Log.error(Country.class, "Failed to load all countries, loaded data is None or empty!");

            return new None<>();
        }

        try {
            ArrayList<Country> countries = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(data.get());

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Option<Country> country = fromJson(jsonObject);

                if (country.isDefined) {
                    countries.add(country.get());
                }
            }

            return new Some<>(countries);
        } catch (Throwable t) {
            Log.error(Country.class, "Failed to load all countries, cannot construct countries from " + data.get() + "!", t);

            return new None<>();
        }
    }

    public static boolean saveAll(ArrayList<Country> countries) {
        Log.info(Country.class, "Saving all countries...");

        if (FileUtils.dataPath.isEmpty) {
            Log.error(Country.class, "Failed to save all countries, data path is None!");

            return false;
        }

        StringBuilder stringBuilder = new StringBuilder("[");

        for (int i = 0, size = countries.size(); i < size; i++) {
            Option<JSONObject> json = countries.get(i).toJson();

            if (json.isDefined) {
                stringBuilder.append(json.toString());

                if (i != size - 1) {
                    stringBuilder.append(", ");
                }
            }
        }

        stringBuilder.append("]");

        boolean result = FileUtils.writeFile(stringBuilder.toString(), fileName);

        return result;
    }

    public Option<JSONObject> toJson() {
        try {
            JSONObject result = new JSONObject();

            result.put("id",         id);
            result.put("name",       name);
            result.put("trName",     trName);
            result.put("nativeName", nativeName);

            return new Some<>(result);
        } catch (Throwable t) {
            Log.error(this, "Failed to convert " + toString() + " to Json!", t);

            return new None<>();
        }
    }

    public static Option<Country> fromJson(JSONObject json) {
        try {
            int id            = json.getInt("id");
            String name       = json.getString("name");
            String trName     = json.getString("trName");
            String nativeName = json.getString("nativeName");

            return new Some<>(new Country(id, name, trName, nativeName));
        } catch (Throwable t) {
            Log.error(Country.class, "Failed to create country from Json " + json.toString() + "!", t);

            return new None<>();
        }
    }

    public static Option<ArrayList<Country>> fromJsonArray(JSONArray jsonArray) {
        try {
            ArrayList<Country> countries = new ArrayList<>();

            for (int i = 0, size = jsonArray.length(); i < size; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Option<Country> country = fromJson(jsonObject);

                if (country.isDefined) {
                    countries.add(country.get());
                }
            }

            return new Some<>(countries);
        } catch (Throwable t) {
            Log.error(Country.class, "Failed to create countries from Json array " + jsonArray.toString() + "!", t);

            return new None<>();
        }
    }

    @Override public String toString() {
        return String.format(
            "{\"id\": %d, \"name\": \"%s\", \"trName\": \"%s\", \"nativeName\": \"%s\"}",
            id, name, trName, nativeName
        );
    }
}
