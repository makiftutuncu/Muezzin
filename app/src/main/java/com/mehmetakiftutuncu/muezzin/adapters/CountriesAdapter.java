package com.mehmetakiftutuncu.muezzin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.CountryViewHolder;
import com.mehmetakiftutuncu.muezzin.interfaces.OnCountrySelectedListener;
import com.mehmetakiftutuncu.muezzin.models.Country;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by akif on 11/05/16.
 */
public class CountriesAdapter extends RecyclerView.Adapter<CountryViewHolder> {
    private ArrayList<Country> allCountries;
    private ArrayList<Country> countries;

    private OnCountrySelectedListener onCountrySelectedListener;

    public CountriesAdapter(ArrayList<Country> countries, OnCountrySelectedListener onCountrySelectedListener) {
        this.allCountries = countries;
        this.countries    = countries;

        this.onCountrySelectedListener = onCountrySelectedListener;
    }

    @Override public CountryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View countryItemLayout = LayoutInflater.from(context).inflate(R.layout.item_county, parent, false);

        return new CountryViewHolder(context, countryItemLayout, onCountrySelectedListener);
    }

    @Override public void onBindViewHolder(CountryViewHolder holder, int position) {
        holder.setFrom(countries.get(position));
    }

    @Override public int getItemCount() {
        return countries != null ? countries.size() : 0;
    }

    public void search(String query) {
        String q = query.trim();

        if (q.isEmpty()) {
            countries = allCountries;

            return;
        }

        countries = new ArrayList<>();
        Locale locale = new Locale("tr", "TR");

        for (int i = 0, size = allCountries.size(); i < size; i++) {
            Country country = allCountries.get(i);

            if (country.nativeName.toLowerCase().contains(q) || country.englishName.toLowerCase(Locale.ENGLISH).contains(q) || country.turkishName.toLowerCase(locale).contains(q)) {
                countries.add(country);
            }
        }

        notifyDataSetChanged();
    }
}
