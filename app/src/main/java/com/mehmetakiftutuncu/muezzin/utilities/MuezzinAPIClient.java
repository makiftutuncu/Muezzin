package com.mehmetakiftutuncu.muezzin.utilities;

import android.support.annotation.NonNull;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCitiesDownloadedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCountriesDownloadedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnDistrictsDownloadedListener;
import com.mehmetakiftutuncu.muezzin.interfaces.OnPrayerTimesDownloadedListener;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimes;
import com.mehmetakiftutuncu.muezzin.utilities.optional.None;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Optional;
import com.mehmetakiftutuncu.muezzin.utilities.optional.Some;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

/**
 * Created by akif on 08/05/16.
 */
public class MuezzinAPIClient {
    private static final AsyncHttpClient client = new AsyncHttpClient();

    private static final String BASE_URL         = "https://muezzin.herokuapp.com/";
    private static final String COUNTRIES_API    = "countries";
    private static final String CITIES_API       = "%d/cities";
    private static final String DISTRICTS_API    = "%d/districts";
    private static final String PRAYER_TIMES_API = "prayertimes/%d/%d/%s/force";

    private static void get(String path, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url(path), params, responseHandler);
    }

    private static String url(String path) {
        return BASE_URL + path;
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

                    Optional<Country> Turkey = new None<>();
                    ArrayList<Country> countries = new ArrayList<>();

                    for (int i = 0; i < numberOfCountries; i++) {
                        JSONObject countryJson         = countriesJsonArray.getJSONObject(i);
                        Optional<Country> maybeCountry = Country.fromJson(countryJson);

                        if (maybeCountry.isDefined) {
                            Country country = maybeCountry.get();

                            if (country.isTurkey) {
                                Turkey = new Some<>(country);
                            } else {
                                countries.add(country);
                            }
                        }
                    }

                    if (Turkey.isDefined) {
                        countries.add(0, Turkey.get());
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

            @Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorJson) {
                Log.error(MuezzinAPIClient.class, throwable, "Failed to get countries, Muezzin API response status '%d' and body '%s'", statusCode, errorJson);
                listener.onCountriesDownloadFailed();
            }
        });
    }

    public static void getCities(final int countryId, @NonNull final OnCitiesDownloadedListener listener) {
        Log.debug(MuezzinAPIClient.class, "Getting cities for country '%d'...", countryId);

        String path = String.format(Locale.ENGLISH, CITIES_API, countryId);

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

                    if (City.isTurkish(countryId)) {
                        Collections.sort(cities, new Comparator<City>() {
                            @Override public int compare(City lhs, City rhs) {
                                return LocaleUtils.getTurkishCollator().compare(lhs.name, rhs.name);
                            }
                        });
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

            @Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorJson) {
                Log.error(MuezzinAPIClient.class, throwable, "Failed to get cities for country '%d', Muezzin API response status '%d' and body '%s'", countryId, statusCode, errorJson);
                listener.onCitiesDownloadFailed();
            }
        });
    }

    public static void getDistricts(final int cityId, @NonNull final OnDistrictsDownloadedListener listener) {
        Log.debug(MuezzinAPIClient.class, "Getting districts for city '%d'...", cityId);

        String path = String.format(Locale.ENGLISH, DISTRICTS_API, cityId);

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

                    if (District.isTurkish(cityId)) {
                        Collections.sort(districts, new Comparator<District>() {
                            @Override public int compare(District lhs, District rhs) {
                                return LocaleUtils.getTurkishCollator().compare(lhs.name, rhs.name);
                            }
                        });
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

            @Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorJson) {
                Log.error(MuezzinAPIClient.class, throwable, "Failed to get districts for city '%d', Muezzin API response status '%d' and body '%s'", cityId, statusCode, errorJson);
                listener.onDistrictsDownloadFailed();
            }
        });
    }

    public static void getPrayerTimes(final Place place, @NonNull final OnPrayerTimesDownloadedListener listener) {
        Log.debug(MuezzinAPIClient.class, "Getting prayer times for place '%s'...", place);

        String path = String.format(Locale.ENGLISH, PRAYER_TIMES_API, place.countryId, place.cityId, place.districtId.toString());

        get(path, null, new JsonHttpResponseHandler("UTF-8") {
            @Override public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (statusCode != 200) {
                    Log.error(MuezzinAPIClient.class, "Failed to get prayer times for place '%s', Muezzin API returned invalid status '%d'!", place, statusCode);
                    listener.onPrayerTimesDownloadFailed();

                    return;
                }

                try {
                    JSONArray prayerTimesJsonArray = response.getJSONArray("times");
                    int numberOfTimes              = prayerTimesJsonArray.length();

                    ArrayList<PrayerTimes> prayerTimes = new ArrayList<>();

                    for (int i = 0; i < numberOfTimes; i++) {
                        JSONObject prayerTimeJson            = prayerTimesJsonArray.getJSONObject(i);
                        Optional<PrayerTimes> maybePrayerTime = PrayerTimes.fromJson(place, prayerTimeJson);

                        if (maybePrayerTime.isDefined) {
                            prayerTimes.add(maybePrayerTime.get());
                        }
                    }

                    if (prayerTimes.size() != numberOfTimes) {
                        Log.error(MuezzinAPIClient.class, "Failed to parse some of the prayer times for place '%s' from Json '%s'!", place, response);
                        listener.onPrayerTimesDownloadFailed();

                        return;
                    }

                    Log.debug(MuezzinAPIClient.class, "Successfully got prayer times for place '%s'!", place);
                    listener.onPrayerTimesDownloaded(prayerTimes);
                } catch (Throwable t) {
                    Log.error(MuezzinAPIClient.class, t, "Failed to get prayer times for place '%s'!", place);
                    listener.onPrayerTimesDownloadFailed();
                }
            }

            @Override public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.error(MuezzinAPIClient.class, throwable, "Failed to get prayer times for place '%s', Muezzin API response status '%d' and body '%s'", place, statusCode, responseString);
                listener.onPrayerTimesDownloadFailed();
            }

            @Override public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorJson) {
                Log.error(MuezzinAPIClient.class, throwable, "Failed to get prayer times for place '%s', Muezzin API response status '%d' and body '%s'", place, statusCode, errorJson);
                listener.onPrayerTimesDownloadFailed();
            }
        });
    }
}
