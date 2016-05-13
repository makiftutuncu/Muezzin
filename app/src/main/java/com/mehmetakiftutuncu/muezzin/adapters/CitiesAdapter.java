package com.mehmetakiftutuncu.muezzin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.interfaces.OnCitySelectedListener;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.CityViewHolder;
import com.mehmetakiftutuncu.muezzin.models.City;

import java.util.ArrayList;

/**
 * Created by akif on 11/05/16.
 */
public class CitiesAdapter extends RecyclerView.Adapter<CityViewHolder> {
    private ArrayList<City> cities;

    private OnCitySelectedListener onCitySelectedListener;

    public CitiesAdapter(ArrayList<City> cities, OnCitySelectedListener onCitySelectedListener) {
        this.cities = cities;
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
}
