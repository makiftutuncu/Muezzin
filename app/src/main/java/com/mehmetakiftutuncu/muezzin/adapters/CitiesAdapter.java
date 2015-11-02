package com.mehmetakiftutuncu.muezzin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CitiesAdapter extends RecyclerView.Adapter<CitiesAndDistrictsViewHolder> {
    private List<City> cities;
    private OnItemClickedListener onItemClickedListener;

    public CitiesAdapter(List<City> cities, OnItemClickedListener onItemClickedListener) {
        this.cities = cities;

        final Collator collator = LocaleUtils.getCollator();
        Collections.sort(this.cities, new Comparator<City>() {
            @Override
            public int compare(City lhs, City rhs) {
                return collator.compare(lhs.name, rhs.name);
            }
        });

        this.onItemClickedListener = onItemClickedListener;
    }

    @Override
    public CitiesAndDistrictsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cityItemLayout = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_twolines, parent, false);

        return new CitiesAndDistrictsViewHolder(cityItemLayout, onItemClickedListener);
    }

    @Override
    public void onBindViewHolder(CitiesAndDistrictsViewHolder holder, int position) {
        if (holder != null) {
            holder.setCity(cities.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return cities != null ? cities.size() : 0;
    }
}
