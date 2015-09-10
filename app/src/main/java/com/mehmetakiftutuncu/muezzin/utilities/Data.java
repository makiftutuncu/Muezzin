package com.mehmetakiftutuncu.muezzin.utilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;

import java.util.ArrayList;
import java.util.List;

public class Data {
    private static String countriesFileName() {
        return "countries";
    }

    private static String citiesFileName(int countryId) {
        return "cities." + countryId;
    }

    private static String districtsFileName(int countryId, int cityId) {
        return "districts." + countryId + "." + cityId;
    }

    public static List<Country> loadCountries() {
        Log.info(Data.class, "Loading countries...");

        if (FileUtils.dataPath == null) {
            Log.error(Data.class, "Failed to load countries, data path is null!");
            return null;
        } else {
            String data = FileUtils.readFile(countriesFileName());

            if (StringUtils.isEmpty(data)) {
                Log.error(Data.class, "Failed to load countries, loaded data is empty!");
                return null;
            } else {
                Gson gson = new Gson();
                List<Country> countries = gson.fromJson(data, new TypeToken<ArrayList<Country>>() {
                }.getType());

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
                String data = gson.toJson(countries, new TypeToken<ArrayList<Country>>() {
                }.getType());

                return FileUtils.writeFile(data, countriesFileName());
            }
        }
    }

    public static List<City> loadCities(int countryId) {
        Log.info(Data.class, "Loading cities for country " + countryId + "...");

        if (FileUtils.dataPath == null) {
            Log.error(Data.class, "Failed to load cities for country " + countryId + ", data path is null!");
            return null;
        } else {
            String data = FileUtils.readFile(citiesFileName(countryId));

            if (StringUtils.isEmpty(data)) {
                Log.error(Data.class, "Failed to load cities for country " + countryId + ", loaded data is empty!");
                return null;
            } else {
                Gson gson = new Gson();
                List<City> cities = gson.fromJson(data, new TypeToken<ArrayList<City>>(){}.getType());

                if (cities == null || cities.isEmpty()) {
                    Log.error(Data.class, "Failed to load cities for country " + countryId + ", cities object is empty!");
                    return null;
                } else {
                    return cities;
                }
            }
        }
    }

    public static boolean saveCities(List<City> cities, int countryId) {
        Log.info(Data.class, "Saving cities for country " + countryId + "...");

        if (cities == null) {
            Log.error(Data.class, "Failed to save cities for country " + countryId + ", cities object is null!");
            return false;
        } else {
            if (FileUtils.dataPath == null) {
                Log.error(Data.class, "Failed to save cities for country " + countryId + ", data path is null!");
                return false;
            } else {
                Gson gson = new Gson();
                String data = gson.toJson(cities, new TypeToken<ArrayList<City>>(){}.getType());

                return FileUtils.writeFile(data, citiesFileName(countryId));
            }
        }
    }

    public static List<District> loadDistricts(int countryId, int cityId) {
        Log.info(Data.class, "Loading districts for country " + countryId + " and city " + cityId + "...");

        if (FileUtils.dataPath == null) {
            Log.error(Data.class, "Failed to load districts for country " + countryId + " and city " + cityId + ", data path is null!");
            return null;
        } else {
            String data = FileUtils.readFile(districtsFileName(countryId, cityId));

            if (StringUtils.isEmpty(data)) {
                Log.error(Data.class, "Failed to load districts for country " + countryId + " and city " + cityId + ", loaded data is empty!");
                return null;
            } else {
                Gson gson = new Gson();
                List<District> districts = gson.fromJson(data, new TypeToken<ArrayList<District>>(){}.getType());

                if (districts == null || districts.isEmpty()) {
                    Log.error(Data.class, "Failed to load districts for country " + countryId + " and city " + cityId + ", districts object is empty!");
                    return null;
                } else {
                    return districts;
                }
            }
        }
    }

    public static boolean saveDistricts(List<District> districts, int countryId, int cityId) {
        Log.info(Data.class, "Saving districts for country " + countryId + " and city " + cityId + "...");

        if (districts == null) {
            Log.error(Data.class, "Failed to save districts for country " + countryId + " and city " + cityId + ", districts object is null!");
            return false;
        } else {
            if (FileUtils.dataPath == null) {
                Log.error(Data.class, "Failed to save districts for country " + countryId + " and city " + cityId + ", data path is null!");
                return false;
            } else {
                Gson gson = new Gson();
                String data = gson.toJson(districts, new TypeToken<ArrayList<District>>(){}.getType());

                return FileUtils.writeFile(data, districtsFileName(countryId, cityId));
            }
        }
    }
}
