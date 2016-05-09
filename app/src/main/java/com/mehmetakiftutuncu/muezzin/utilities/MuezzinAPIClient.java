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
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
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
        Log.debug(MuezzinAPIClient.class, "Getting countries...");

        String path = COUNTRIES_API;

        get(path, null, new JsonHttpResponseHandler("UTF-8") {
            @Override public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode != 200) {
                    Log.error(MuezzinAPIClient.class, "Failed to get countries, Muezzin API returned invalid status '%d'!", statusCode);
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
                        Log.error(MuezzinAPIClient.class, "Failed to parse some of the countries from Json '%s'!", response);
                        listener.onCountriesDownloadFailed();

                        return;
                    }

                    Log.debug(MuezzinAPIClient.class, "Successfully got countries!");
                    listener.onCountriesDownloaded(countries);
                } catch (Throwable t) {
                    Log.error(MuezzinAPIClient.class, t, "Failed to get countries!");
                    listener.onCountriesDownloadFailed();
                }
            }

            @Override public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.error(MuezzinAPIClient.class, throwable, "Failed to get countries, Muezzin API response status '%d' and body '%s'", statusCode, responseString);
                listener.onCountriesDownloadFailed();
            }
        });
    }

    public interface OnCitiesDownloadedListener {
        void onCitiesDownloaded(@NonNull ArrayList<City> cities);
        void onCitiesDownloadFailed();
    }

    public static void getCities(final int countryId, @NonNull final OnCitiesDownloadedListener listener) {
        Log.debug(MuezzinAPIClient.class, "Getting cities for country '%d'...", countryId);

        String path = String.format(CITIES_API, countryId);

        get(path, null, new JsonHttpResponseHandler("UTF-8") {
            @Override public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode != 200) {
                    Log.error(MuezzinAPIClient.class, "Failed to get cities for country '%d', Muezzin API returned invalid status '%d'!", countryId, statusCode);
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
                        Log.error(MuezzinAPIClient.class, "Failed to parse some of the cities for country '%d' from Json '%s'!", countryId, response);
                        listener.onCitiesDownloadFailed();

                        return;
                    }

                    Log.debug(MuezzinAPIClient.class, "Successfully got cities for country '%d'!", countryId);
                    listener.onCitiesDownloaded(cities);
                } catch (Throwable t) {
                    Log.error(MuezzinAPIClient.class, t, "Failed to get cities for country '%d'!", countryId);
                    listener.onCitiesDownloadFailed();
                }
            }

            @Override public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.error(MuezzinAPIClient.class, throwable, "Failed to get cities for country '%d', Muezzin API response status '%d' and body '%s'", countryId, statusCode, responseString);
                listener.onCitiesDownloadFailed();
            }
        });
    }

    public interface OnDistrictsDownloadedListener {
        void onDistrictsDownloaded(@NonNull ArrayList<District> districts);
        void onDistrictsDownloadFailed();
    }

    public static void getDistricts(final int cityId, @NonNull final OnDistrictsDownloadedListener listener) {
        Log.debug(MuezzinAPIClient.class, "Getting districts for city '%d'...", cityId);

        String path = String.format(DISTRICTS_API, cityId);

        get(path, null, new JsonHttpResponseHandler("UTF-8") {
            @Override public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode != 200) {
                    Log.error(MuezzinAPIClient.class, "Failed to get districts for city '%d', Muezzin API returned invalid status '%d'!", cityId, statusCode);
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
                        Log.error(MuezzinAPIClient.class, "Failed to parse some of the districts for city '%d' from Json '%s'!", cityId, response);
                        listener.onDistrictsDownloadFailed();

                        return;
                    }

                    Log.debug(MuezzinAPIClient.class, "Successfully got districts for city '%d'!", cityId);
                    listener.onDistrictsDownloaded(districts);
                } catch (Throwable t) {
                    Log.error(MuezzinAPIClient.class, t, "Failed to get districts for city '%d'!", cityId);
                    listener.onDistrictsDownloadFailed();
                }
            }

            @Override public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.error(MuezzinAPIClient.class, throwable, "Failed to get districts for city '%d', Muezzin API response status '%d' and body '%s'", cityId, statusCode, responseString);
                listener.onDistrictsDownloadFailed();
            }
        });
    }

    public interface OnPrayerTimesDownloadedListener {
        void onPrayerTimesDownloaded(@NonNull ArrayList<PrayerTimes> prayerTimes);
        void onPrayerTimesDownloadFailed();
    }

    public static void getPrayerTimes(final int countryId, final int cityId, final Optional<Integer> districtId, @NonNull final OnPrayerTimesDownloadedListener listener) {
        Log.debug(MuezzinAPIClient.class, "Getting prayer times for country '%d', city '%d' and district '%s'...", countryId, cityId, districtId);

        String path = String.format(PRAYER_TIMES_API, countryId, cityId, districtId.toString());

        get(path, null, new JsonHttpResponseHandler("UTF-8") {
            @Override public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode != 200) {
                    Log.error(MuezzinAPIClient.class, "Failed to get prayer times for country '%d', city '%d' and district '%s', Muezzin API returned invalid status '%d'!", countryId, cityId, districtId, statusCode);
                    listener.onPrayerTimesDownloadFailed();

                    return;
                }

                try {
                    JSONArray prayerTimesJsonArray = response.getJSONArray("times");
                    int numberOfTimes              = prayerTimesJsonArray.length();

                    ArrayList<PrayerTimes> prayerTimes = new ArrayList<>();

                    for (int i = 0; i < numberOfTimes; i++) {
                        JSONObject prayerTimeJson            = prayerTimesJsonArray.getJSONObject(i);
                        Optional<PrayerTimes> maybePrayerTime = PrayerTimes.fromJson(countryId, cityId, districtId, prayerTimeJson);

                        if (maybePrayerTime.isDefined) {
                            prayerTimes.add(maybePrayerTime.get());
                        }
                    }

                    if (prayerTimes.size() != numberOfTimes) {
                        Log.error(MuezzinAPIClient.class, "Failed to parse some of the prayer times for country '%d', city '%d' and district '%s' from Json '%s'!", countryId, cityId, districtId, response);
                        listener.onPrayerTimesDownloadFailed();

                        return;
                    }

                    Log.debug(MuezzinAPIClient.class, "Successfully got prayer times for country '%d', city '%d' and district '%s'!", countryId, cityId, districtId);
                    listener.onPrayerTimesDownloaded(prayerTimes);
                } catch (Throwable t) {
                    Log.error(MuezzinAPIClient.class, t, "Failed to get prayer times for country '%d', city '%d' and district '%s'!", countryId, cityId, districtId);
                    listener.onPrayerTimesDownloadFailed();
                }
            }

            @Override public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.error(MuezzinAPIClient.class, throwable, "Failed to get prayer times for country '%d', city '%d' and district '%s', Muezzin API response status '%d' and body '%s'", countryId, cityId, districtId, statusCode, responseString);
                listener.onPrayerTimesDownloadFailed();
            }
        });
    }
}
