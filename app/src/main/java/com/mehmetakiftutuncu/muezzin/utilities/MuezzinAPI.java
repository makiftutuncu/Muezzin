package com.mehmetakiftutuncu.muezzin.utilities;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.github.mehmetakiftutuncu.toolbelt.Optional;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.models.Place;
import com.mehmetakiftutuncu.muezzin.models.PrayerTimesOfDay;

import org.joda.time.LocalDate;
import org.json.JSONObject;

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by akif on 15/09/16.
 */
public class MuezzinAPI {
    private static MuezzinAPI instance;

    private static final String BASE_URL = "https://muezzin.herokuapp.com";
    private static final OkHttpClient client = new OkHttpClient();
    private static Handler handler = new Handler(Looper.getMainLooper());

    private MuezzinAPI() {}

    @NonNull public static MuezzinAPI get() {
        if (instance == null) {instance = new MuezzinAPI();}

        return instance;
    }

    public void getCountries(@NonNull Context context, @NonNull OnCountriesDownloadedListener listener) {
        String url = String.format(Locale.ENGLISH, "%s/countries", BASE_URL);
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                handler.post(() -> listener.onDownloadCountriesFailed(e));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        Country.InvalidCountryJsonException e = new Country.InvalidCountryJsonException(String.format(Locale.ENGLISH, "Failed to parse countries! Muezzin API returned status '%d' with body '%s'.", response.code(), response.body().string()));

                        handler.post(() -> listener.onDownloadCountriesFailed(e));
                    } else {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONObject countriesJson = json.getJSONObject("countries");

                        Optional<Country> Turkey = Optional.empty();
                        List<Country> countries = new ArrayList<>();

                        Iterator<String> keys = countriesJson.keys();

                        while (keys.hasNext()) {
                            String idString = keys.next();
                            int id = Integer.parseInt(idString);
                            Country country = Country.fromJson(id, countriesJson.getJSONObject(idString));

                            if (country.isTurkey) {
                                Turkey = Optional.with(country);
                            } else {
                                countries.add(country);
                            }
                        }

                        Collator collator = LocaleUtils.getCollator(context);

                        boolean isTurkish = LocaleUtils.isLanguageTurkish(context);
                        boolean isEnglish = LocaleUtils.isLanguageEnglish(context);

                        Collections.sort(countries, (c1, c2) -> {
                            if (isTurkish) {
                                return collator.compare(c1.nameTurkish, c2.nameTurkish);
                            } else if (isEnglish) {
                                return collator.compare(c1.name, c2.name);
                            } else {
                                return collator.compare(c1.nameNative, c2.nameNative);
                            }
                        });

                        if (Turkey.isDefined()) {
                            countries.add(0, Turkey.get());
                        }

                        handler.post(() -> listener.onCountriesDownloaded(countries));
                    }
                } catch (Exception e) {
                    handler.post(() -> listener.onDownloadCountriesFailed(e));
                }
            }
        });
    }

    public void getCities(@NonNull Context context, int countryId, @NonNull OnCitiesDownloadedListener listener) {
        String url = String.format(Locale.ENGLISH, "%s/countries/%d/cities", BASE_URL, countryId);
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                handler.post(() -> listener.onDownloadCitiesFailed(e));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        City.InvalidCityJsonException e = new City.InvalidCityJsonException(String.format(Locale.ENGLISH, "Failed to parse cities for country '%d'! Muezzin API returned status '%d' with body '%s'.", countryId, response.code(), response.body().string()));

                        handler.post(() -> listener.onDownloadCitiesFailed(e));
                    } else {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONObject citiesJson = json.getJSONObject("cities");

                        List<City> cities = new ArrayList<>();

                        Iterator<String> keys = citiesJson.keys();

                        while (keys.hasNext()) {
                            String idString = keys.next();
                            int id = Integer.parseInt(idString);
                            City city = City.fromJson(id, citiesJson.getJSONObject(idString));

                            cities.add(city);
                        }

                        Collator collator = LocaleUtils.getCollator(context);
                        Collections.sort(cities, (c1, c2) -> collator.compare(c1.name, c2.name));

                        handler.post(() -> listener.onCitiesDownloaded(cities));
                    }
                } catch (Exception e) {
                    handler.post(() -> listener.onDownloadCitiesFailed(e));
                }
            }
        });
    }

    public void getDistricts(@NonNull Context context, int countryId, int cityId, @NonNull OnDistrictsDownloadedListener listener) {
        String url = String.format(Locale.ENGLISH, "%s/countries/%d/cities/%d/districts", BASE_URL, countryId, cityId);
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                handler.post(() -> listener.onDownloadDistrictsFailed(e));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        District.InvalidDistrictJsonException e = new District.InvalidDistrictJsonException(String.format(Locale.ENGLISH, "Failed to parse districts for country '%d' and city '%d'! Muezzin API returned status '%d' with body '%s'.", countryId, cityId, response.code(), response.body().string()));

                        handler.post(() -> listener.onDownloadDistrictsFailed(e));
                    } else {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONObject districtsJson = json.getJSONObject("districts");

                        List<District> districts = new ArrayList<>();

                        Iterator<String> keys = districtsJson.keys();

                        while (keys.hasNext()) {
                            String idString = keys.next();
                            int id = Integer.parseInt(idString);
                            String name = districtsJson.getString(idString);

                            District district = new District(id, name);

                            districts.add(district);
                        }

                        Collator collator = LocaleUtils.getCollator(context);
                        Collections.sort(districts, (d1, d2) -> collator.compare(d1.name, d2.name));

                        handler.post(() -> listener.onDistrictsDownloaded(districts));
                    }
                } catch (Exception e) {
                    handler.post(() -> listener.onDownloadDistrictsFailed(e));
                }
            }
        });
    }

    public void getPrayerTimes(@NonNull Place place, @NonNull OnPrayerTimesDownloadedListener listener) {
        String url = place.districtId.isDefined() ? (
            String.format(Locale.ENGLISH, "%s/prayerTimes/country/%d/city/%d/district/%d", BASE_URL, place.countryId, place.cityId, place.districtId.get())
        ) : (
            String.format(Locale.ENGLISH, "%s/prayerTimes/country/%d/city/%d", BASE_URL, place.countryId, place.cityId)
        );

        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                handler.post(() -> listener.onDownloadPrayerTimesFailed(e));
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        PrayerTimesOfDay.InvalidPrayerTimesOfDayJsonException e = new PrayerTimesOfDay.InvalidPrayerTimesOfDayJsonException(
                            place.districtId.isDefined() ? (
                                String.format(Locale.ENGLISH, "Failed to parse prayer times for country '%d', city '%d' and district '%d'! Muezzin API returned status '%d' with body '%s'.", place.countryId, place.cityId, place.districtId.get(), response.code(), response.body().string())
                            ) : (
                                String.format(Locale.ENGLISH, "Failed to parse prayer times for country '%d' and city '%d'! Muezzin API returned status '%d' with body '%s'.", place.countryId, place.cityId, response.code(), response.body().string())
                            )
                        );

                        handler.post(() -> listener.onDownloadPrayerTimesFailed(e));
                    } else {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONObject prayerTimesJson = json.getJSONObject("prayerTimes");

                        List<PrayerTimesOfDay> prayerTimes = new ArrayList<>();

                        Iterator<String> keys = prayerTimesJson.keys();

                        while (keys.hasNext()) {
                            String dateString = keys.next();
                            LocalDate date = LocalDate.parse(dateString, PrayerTimesOfDay.DATE_FORMATTER);

                            PrayerTimesOfDay prayerTimesOfDay = PrayerTimesOfDay.fromJson(date, prayerTimesJson.getJSONObject(dateString));

                            prayerTimes.add(prayerTimesOfDay);
                        }

                        handler.post(() -> listener.onPrayerTimesDownloaded(prayerTimes));
                    }
                } catch (Exception e) {
                    handler.post(() -> listener.onDownloadPrayerTimesFailed(e));
                }
            }
        });
    }

    public interface OnCountriesDownloadedListener {
        void onCountriesDownloaded(@NonNull List<Country> countries);
        void onDownloadCountriesFailed(Exception e);
    }

    public interface OnCitiesDownloadedListener {
        void onCitiesDownloaded(@NonNull List<City> cities);
        void onDownloadCitiesFailed(Exception e);
    }

    public interface OnDistrictsDownloadedListener {
        void onDistrictsDownloaded(@NonNull List<District> districts);
        void onDownloadDistrictsFailed(Exception e);
    }

    public interface OnPrayerTimesDownloadedListener {
        void onPrayerTimesDownloaded(@NonNull List<PrayerTimesOfDay> prayerTimes);
        void onDownloadPrayerTimesFailed(Exception e);
    }
}
