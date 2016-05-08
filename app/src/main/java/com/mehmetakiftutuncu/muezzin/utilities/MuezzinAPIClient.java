package com.mehmetakiftutuncu.muezzin.utilities;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.models.PrayerTime;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by akif on 08/05/16.
 */
@SuppressLint("DefaultLocale")
public class MuezzinAPIClient {
    private static final AsyncHttpClient client = new AsyncHttpClient();

    private static final String BASE_URL         = "https://muezzin.herokuapp.com/";
    private static final String COUNTRIES_API    = "countries";
    private static final String CITIES_API       = "%d/cities";
    private static final String DISTRICTS_API    = "%d/districts";
    private static final String PRAYER_TIMES_API = "prayertimes/%d/%d/%s";

    private static void get(String path, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url(path), params, responseHandler);
    }

    private static String url(String path) {
        return BASE_URL + path;
    }

    public interface OnCountriesDownloadedListener {
        void onCountriesDownloaded(@NonNull ArrayList<Country> countries);
        void onCountriesDownloadFailed();
    }

    public static void getCountries(@NonNull final OnCountriesDownloadedListener listener) {
        Log.debug("Getting countries...", MuezzinAPIClient.class, "getCountries");

        String path = COUNTRIES_API;

        get(path, null, new JsonHttpResponseHandler("UTF-8") {
            @Override public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode != 200) {
                    Log.error(String.format("Failed to get countries, Muezzin API returned invalid status '%d'!", statusCode), MuezzinAPIClient.class, "getCountries");
                    listener.onCountriesDownloadFailed();

                    return;
                }

                try {
                    JSONArray countriesJsonArray = response.getJSONArray("countries");
                    int numberOfCountries        = countriesJsonArray.length();

                    ArrayList<Country> countries = new ArrayList<>();

                    for (int i = 0; i < numberOfCountries; i++) {
                        JSONObject countryJson         = countriesJsonArray.getJSONObject(i);
                        Optional<Country> maybeCountry = Country.fromJson(countryJson);

                        if (maybeCountry.isDefined) {
                            countries.add(maybeCountry.get());
                        }
                    }

                    if (countries.size() != numberOfCountries) {
                        Log.error(String.format("Failed to parse some of the countries from Json '%s'!", response), MuezzinAPIClient.class, "getCountries");
                        listener.onCountriesDownloadFailed();

                        return;
                    }

                    Log.debug("Successfully got countries!", MuezzinAPIClient.class, "getCountries");
                    listener.onCountriesDownloaded(countries);
                } catch (Throwable t) {
                    Log.error("Failed to get countries!", t, MuezzinAPIClient.class, "getCountries");
                    listener.onCountriesDownloadFailed();
                }
            }

            @Override public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.error(String.format("Failed to get countries, Muezzin API response status '%d' and body '%s'", statusCode, responseString), throwable, MuezzinAPIClient.class, "getCountries");
                listener.onCountriesDownloadFailed();
            }
        });
    }

    public interface OnCitiesDownloadedListener {
        void onCitiesDownloaded(@NonNull ArrayList<City> cities);
        void onCitiesDownloadFailed();
    }

    public static void getCities(final int countryId, @NonNull final OnCitiesDownloadedListener listener) {
        Log.debug(String.format("Getting cities for country '%d'...", countryId), MuezzinAPIClient.class, "getCities");

        String path = String.format(CITIES_API, countryId);

        get(path, null, new JsonHttpResponseHandler("UTF-8") {
            @Override public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode != 200) {
                    Log.error(String.format("Failed to get cities for country '%d', Muezzin API returned invalid status '%d'!", countryId, statusCode), MuezzinAPIClient.class, "getCities");
                    listener.onCitiesDownloadFailed();

                    return;
                }

                try {
                    JSONArray citiesJsonArray = response.getJSONArray("cities");
                    int numberOfCities        = citiesJsonArray.length();

                    ArrayList<City> cities = new ArrayList<>();

                    for (int i = 0; i < numberOfCities; i++) {
                        JSONObject cityJson      = citiesJsonArray.getJSONObject(i);
                        Optional<City> maybeCity = City.fromJson(countryId, cityJson);

                        if (maybeCity.isDefined) {
                            cities.add(maybeCity.get());
                        }
                    }

                    if (cities.size() != numberOfCities) {
                        Log.error(String.format("Failed to parse some of the cities for country '%d' from Json '%s'!", countryId, response), MuezzinAPIClient.class, "getCities");
                        listener.onCitiesDownloadFailed();

                        return;
                    }

                    Log.debug(String.format("Successfully got cities for country '%d'!", countryId), MuezzinAPIClient.class, "getCities");
                    listener.onCitiesDownloaded(cities);
                } catch (Throwable t) {
                    Log.error(String.format("Failed to get cities for country '%d'!", countryId), t, MuezzinAPIClient.class, "getCities");
                    listener.onCitiesDownloadFailed();
                }
            }

            @Override public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.error(String.format("Failed to get cities for country '%d', Muezzin API response status '%d' and body '%s'", countryId, statusCode, responseString), throwable, MuezzinAPIClient.class, "getCities");
                listener.onCitiesDownloadFailed();
            }
        });
    }

    public interface OnDistrictsDownloadedListener {
        void onDistrictsDownloaded(@NonNull ArrayList<District> districts);
        void onDistrictsDownloadFailed();
    }

    public static void getDistricts(final int cityId, @NonNull final OnDistrictsDownloadedListener listener) {
        Log.debug(String.format("Getting districts for city '%d'...", cityId), MuezzinAPIClient.class, "getDistricts");

        String path = String.format(DISTRICTS_API, cityId);

        get(path, null, new JsonHttpResponseHandler("UTF-8") {
            @Override public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode != 200) {
                    Log.error(String.format("Failed to get districts for city '%d', Muezzin API returned invalid status '%d'!", cityId, statusCode), MuezzinAPIClient.class, "getDistricts");
                    listener.onDistrictsDownloadFailed();

                    return;
                }

                try {
                    JSONArray districtsJsonArray = response.getJSONArray("districts");
                    int numberOfDistricts        = districtsJsonArray.length();

                    ArrayList<District> districts = new ArrayList<>();

                    for (int i = 0; i < numberOfDistricts; i++) {
                        JSONObject districtJson          = districtsJsonArray.getJSONObject(i);
                        Optional<District> maybeDistrict = District.fromJson(cityId, districtJson);

                        if (maybeDistrict.isDefined) {
                            districts.add(maybeDistrict.get());
                        }
                    }

                    if (districts.size() != numberOfDistricts) {
                        Log.error(String.format("Failed to parse some of the districts for city '%d' from Json '%s'!", cityId, response), MuezzinAPIClient.class, "getDistricts");
                        listener.onDistrictsDownloadFailed();

                        return;
                    }

                    Log.debug(String.format("Successfully got districts for city '%d'!", cityId), MuezzinAPIClient.class, "getDistricts");
                    listener.onDistrictsDownloaded(districts);
                } catch (Throwable t) {
                    Log.error(String.format("Failed to get districts for city '%d'!", cityId), t, MuezzinAPIClient.class, "getDistricts");
                    listener.onDistrictsDownloadFailed();
                }
            }

            @Override public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.error(String.format("Failed to get districts for city '%d', Muezzin API response status '%d' and body '%s'", cityId, statusCode, responseString), throwable, MuezzinAPIClient.class, "getDistricts");
                listener.onDistrictsDownloadFailed();
            }
        });
    }

    public interface OnPrayerTimesDownloadedListener {
        void onPrayerTimesDownloaded(@NonNull ArrayList<PrayerTime> prayerTimes);
        void onPrayerTimesDownloadFailed();
    }

    public static void getPrayerTimes(final int countryId, final int cityId, final Optional<Integer> districtId, @NonNull final OnPrayerTimesDownloadedListener listener) {
        Log.debug(String.format("Getting prayer times for country '%d', city '%d' and district '%s'...", countryId, cityId, districtId), MuezzinAPIClient.class, "getPrayerTimes");

        String path = String.format(PRAYER_TIMES_API, countryId, cityId, districtId.toString());

        get(path, null, new JsonHttpResponseHandler("UTF-8") {
            @Override public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode != 200) {
                    Log.error(String.format("Failed to get prayer times for country '%d', city '%d' and district '%s', Muezzin API returned invalid status '%d'!", countryId, cityId, districtId, statusCode), MuezzinAPIClient.class, "getPrayerTimes");
                    listener.onPrayerTimesDownloadFailed();

                    return;
                }

                try {
                    JSONArray prayerTimesJsonArray = response.getJSONArray("times");
                    int numberOfTimes              = prayerTimesJsonArray.length();

                    ArrayList<PrayerTime> prayerTimes = new ArrayList<>();

                    for (int i = 0; i < numberOfTimes; i++) {
                        JSONObject prayerTimeJson            = prayerTimesJsonArray.getJSONObject(i);
                        Optional<PrayerTime> maybePrayerTime = PrayerTime.fromJson(countryId, cityId, districtId, prayerTimeJson);

                        if (maybePrayerTime.isDefined) {
                            prayerTimes.add(maybePrayerTime.get());
                        }
                    }

                    if (prayerTimes.size() != numberOfTimes) {
                        Log.error(String.format("Failed to parse some of the prayer times for country '%d', city '%d' and district '%s' from Json '%s'!", countryId, cityId, districtId, response), MuezzinAPIClient.class, "getPrayerTimes");
                        listener.onPrayerTimesDownloadFailed();

                        return;
                    }

                    Log.debug(String.format("Successfully got prayer times for country '%d', city '%d' and district '%s'!", countryId, cityId, districtId), MuezzinAPIClient.class, "getPrayerTimes");
                    listener.onPrayerTimesDownloaded(prayerTimes);
                } catch (Throwable t) {
                    Log.error(String.format("Failed to get prayer times for country '%d', city '%d' and district '%s'!", countryId, cityId, districtId), t, MuezzinAPIClient.class, "getPrayerTimes");
                    listener.onPrayerTimesDownloadFailed();
                }
            }

            @Override public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.error(String.format("Failed to get prayer times for country '%d', city '%d' and district '%s', Muezzin API response status '%d' and body '%s'", countryId, cityId, districtId, statusCode, responseString), throwable, MuezzinAPIClient.class, "getPrayerTimes");
                listener.onPrayerTimesDownloadFailed();
            }
        });
    }
}
