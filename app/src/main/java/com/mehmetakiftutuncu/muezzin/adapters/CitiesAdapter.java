package com.mehmetakiftutuncu.muezzin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.CityViewHolder;
import com.mehmetakiftutuncu.muezzin.fragments.CitySelectionFragment;
import com.mehmetakiftutuncu.muezzin.models.City;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by akif on 11/05/16.
 */
public class CitiesAdapter extends RecyclerView.Adapter<CityViewHolder> {
    private List<City> allCities;
    private List<City> cities;

    private CitySelectionFragment.OnCitySelectedListener onCitySelectedListener;

    public CitiesAdapter(List<City> cities, CitySelectionFragment.OnCitySelectedListener onCitySelectedListener) {
        this.allCities = cities;
        this.cities    = cities;

        this.onCitySelectedListener = onCitySelectedListener;
    }

    @Override public CityViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View cityItemLayout = LayoutInflater.from(context).inflate(R.layout.item_city, parent, false);

        return new CityViewHolder(cityItemLayout, onCitySelectedListener);
    }

    @Override public void onBindViewHolder(CityViewHolder holder, int position) {
        holder.setFrom(cities.get(position));
    }

    @Override public int getItemCount() {
        return cities != null ? cities.size() : 0;
    }

    public void search(String query) {
        String q = query.trim();

        if (q.isEmpty()) {
            cities = allCities;

            return;
        }

        cities = new ArrayList<>();
        Locale locale = new Locale("tr", "TR");

        for (int i = 0, size = allCities.size(); i < size; i++) {
            City city = allCities.get(i);

            if (city.name.toLowerCase(city.isTurkish ? locale : Locale.getDefault()).contains(q)) {
                cities.add(city);
            }
        }

        notifyDataSetChanged();
    }
}
