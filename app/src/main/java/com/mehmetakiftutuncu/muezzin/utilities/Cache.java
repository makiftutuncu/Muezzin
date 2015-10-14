package com.mehmetakiftutuncu.muezzin.utilities;

import com.mehmetakiftutuncu.muezzin.utilities.option.None;
import com.mehmetakiftutuncu.muezzin.utilities.option.Option;
import com.mehmetakiftutuncu.muezzin.utilities.option.Some;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;

public class Cache {
    public static class Country {
        private final static HashMap<Integer, com.mehmetakiftutuncu.muezzin.models.Country> map = new HashMap<>();
        private static Option<ArrayList<com.mehmetakiftutuncu.muezzin.models.Country>> list     = new None<>();

        public static Option<com.mehmetakiftutuncu.muezzin.models.Country> get(int id) {
            if (map.containsKey(id)) {
                return new Some<>(map.get(id));
            }

            return new None<>();
        }

        public static void set(com.mehmetakiftutuncu.muezzin.models.Country country) {
            map.put(country.id, country);
        }

        public static Option<ArrayList<com.mehmetakiftutuncu.muezzin.models.Country>> getList() {
            return list;
        }

        public static void setList(ArrayList<com.mehmetakiftutuncu.muezzin.models.Country> countries) {
            list = new Some<>(countries);
        }
    }

    public static class City {
        private final static HashMap<String, com.mehmetakiftutuncu.muezzin.models.City> map                = new HashMap<>();
        private final static HashMap<String, ArrayList<com.mehmetakiftutuncu.muezzin.models.City>> listMap = new HashMap<>();

        public static Option<com.mehmetakiftutuncu.muezzin.models.City> get(int countryId, int cityId) {
            String key = com.mehmetakiftutuncu.muezzin.models.City.fileName(countryId) + "." + cityId;

            if (map.containsKey(key)) {
                return new Some<>(map.get(key));
            }

            return new None<>();
        }

        public static void set(int countryId, int cityId, com.mehmetakiftutuncu.muezzin.models.City city) {
            String key = com.mehmetakiftutuncu.muezzin.models.City.fileName(countryId) + "." + cityId;

            map.put(key, city);
        }

        public static Option<ArrayList<com.mehmetakiftutuncu.muezzin.models.City>> getList(int countryId) {
            String key = com.mehmetakiftutuncu.muezzin.models.City.fileName(countryId);

            if (listMap.containsKey(key)) {
                return new Some<>(listMap.get(key));
            }

            return new None<>();
        }

        public static void setList(int countryId, ArrayList<com.mehmetakiftutuncu.muezzin.models.City> cities) {
            String key = com.mehmetakiftutuncu.muezzin.models.City.fileName(countryId);

            listMap.put(key, cities);
        }
    }

    public static class District {
        private final static HashMap<String, com.mehmetakiftutuncu.muezzin.models.District> map                = new HashMap<>();
        private final static HashMap<String, ArrayList<com.mehmetakiftutuncu.muezzin.models.District>> listMap = new HashMap<>();

        public static Option<com.mehmetakiftutuncu.muezzin.models.District> get(int countryId, int cityId, int districtId) {
            String key = com.mehmetakiftutuncu.muezzin.models.District.fileName(countryId, cityId) + "." + districtId;

            if (map.containsKey(key)) {
                return new Some<>(map.get(key));
            }

            return new None<>();
        }

        public static void set(int countryId, int cityId, com.mehmetakiftutuncu.muezzin.models.District district) {
            String key = com.mehmetakiftutuncu.muezzin.models.District.fileName(countryId, cityId) + "." + district.id;

            map.put(key, district);
        }

        public static Option<ArrayList<com.mehmetakiftutuncu.muezzin.models.District>> getList(int countryId, int cityId) {
            String key = com.mehmetakiftutuncu.muezzin.models.District.fileName(countryId, cityId);

            if (listMap.containsKey(key)) {
                return new Some<>(listMap.get(key));
            }

            return new None<>();
        }

        public static void setList(int countryId, int cityId, ArrayList<com.mehmetakiftutuncu.muezzin.models.District> districts) {
            String key = com.mehmetakiftutuncu.muezzin.models.District.fileName(countryId, cityId);

            listMap.put(key, districts);
        }
    }

    public static class PrayerTimes {
        private final static HashMap<String, com.mehmetakiftutuncu.muezzin.models.PrayerTimes> map                = new HashMap<>();
        private final static HashMap<String, ArrayList<com.mehmetakiftutuncu.muezzin.models.PrayerTimes>> listMap = new HashMap<>();

        public static Option<com.mehmetakiftutuncu.muezzin.models.PrayerTimes> get(int countryId, int cityId, Option<Integer> districtId, DateTime dayDate) {
            String key = com.mehmetakiftutuncu.muezzin.models.PrayerTimes.fileName(countryId, cityId, districtId) + "." + dayDate.getMillis();

            if (map.containsKey(key)) {
                return new Some<>(map.get(key));
            }

            return new None<>();
        }

        public static void set(int countryId, int cityId, Option<Integer> districtId, com.mehmetakiftutuncu.muezzin.models.PrayerTimes prayerTimes) {
            String key = com.mehmetakiftutuncu.muezzin.models.PrayerTimes.fileName(countryId, cityId, districtId) + "." + prayerTimes.dayDate.getMillis();

            map.put(key, prayerTimes);
        }

        public static Option<ArrayList<com.mehmetakiftutuncu.muezzin.models.PrayerTimes>> getList(int countryId, int cityId, Option<Integer> districtId) {
            String key = com.mehmetakiftutuncu.muezzin.models.PrayerTimes.fileName(countryId, cityId, districtId);

            if (listMap.containsKey(key)) {
                return new Some<>(listMap.get(key));
            }

            return new None<>();
        }

        public static void setList(int countryId, int cityId, Option<Integer> districtId, ArrayList<com.mehmetakiftutuncu.muezzin.models.PrayerTimes> prayerTimesList) {
            String key = com.mehmetakiftutuncu.muezzin.models.PrayerTimes.fileName(countryId, cityId, districtId);

            listMap.put(key, prayerTimesList);
        }
    }
}
