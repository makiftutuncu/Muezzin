package com.mehmetakiftutuncu.muezzin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.indexedrecyclerview.IndexedRecyclerView;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class CitiesAdapter extends RecyclerView.Adapter<CitiesAndDistrictsViewHolder> implements IndexedRecyclerView.Indices {
    private List<City> cities;
    private LinkedHashMap<String, Integer> mapIndex;
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

        generateMapIndex();

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

    @Override
    public HashMap<String, Integer> getIndicesMap() {
        return mapIndex;
    }

    private void generateMapIndex() {
        mapIndex = new LinkedHashMap<>(256);

        for (int i = 0, size = cities.size(); i < size; i++) {
            City city = cities.get(i);
            String key = String.valueOf(city.name.substring(0, 1));

            if (!mapIndex.containsKey(key)) {
                mapIndex.put(key, i);
            }
        }
    }
}
