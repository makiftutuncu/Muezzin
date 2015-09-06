package com.mehmetakiftutuncu.muezzin.utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mehmetakiftutuncu.muezzin.models.Country;

import java.util.ArrayList;
import java.util.List;

public class Data {
    public static List<Country> loadCountries() {
        Log.info(Data.class, "Loading countries...");

        if (FileUtils.dataPath == null) {
            Log.error(Data.class, "Failed to load countries, data path is null!");
            return null;
        } else {
            String fileName = "countries";
            String data = FileUtils.readFile(fileName);

            if (StringUtils.isEmpty(data)) {
                Log.error(Data.class, "Failed to load countries, loaded data is empty!");
                return null;
            } else {
                Gson gson = new Gson();
                List<Country> countries = gson.fromJson(data, new TypeToken<ArrayList<Country>>(){}.getType());

                if (countries == null || countries.isEmpty()) {
                    Log.error(Data.class, "Failed to load countries, countries object is empty!");
                    return null;
                } else {
                    return countries;
                }
            }
        }
    }

    public static boolean saveCountries(List<Country> countries) {
        Log.info(Data.class, "Saving countries...");

        if (countries == null) {
            Log.error(Data.class, "Failed to save countries, countries object is null!");
            return false;
        } else {
            if (FileUtils.dataPath == null) {
                Log.error(Data.class, "Failed to save countries, data path is null!");
                return false;
            } else {
                Gson gson = new Gson();
                String data = gson.toJson(countries, new TypeToken<ArrayList<Country>>(){}.getType());

                return FileUtils.writeFile(data, "countries");
            }
        }
    }
}
