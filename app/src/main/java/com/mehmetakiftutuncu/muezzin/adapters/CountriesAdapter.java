package com.mehmetakiftutuncu.muezzin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.indexedrecyclerview.IndexedRecyclerView;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class CountriesAdapter extends RecyclerView.Adapter<CountriesViewHolder> implements IndexedRecyclerView.Indices {
    private List<Country> countries;
    private LinkedHashMap<String, Integer> mapIndex;
    private OnItemClickedListener onItemClickedListener;

    public CountriesAdapter(List<Country> countries, OnItemClickedListener onItemClickedListener) {
        this.countries = countries;

        final Collator collator = LocaleUtils.getCollator();
        Collections.sort(this.countries, new Comparator<Country>() {
            @Override
            public int compare(Country lhs, Country rhs) {
                return collator.compare(lhs.localizedName(), rhs.localizedName());
            }
        });

        generateMapIndex();

        this.onItemClickedListener = onItemClickedListener;
    }

    @Override
    public CountriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View countryItemLayout = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_twolines, parent, false);

        return new CountriesViewHolder(countryItemLayout, onItemClickedListener);
    }

    @Override
    public void onBindViewHolder(CountriesViewHolder holder, int position) {
        if (holder != null) {
            holder.setCountry(countries.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return countries != null ? countries.size() : 0;
    }

    @Override
    public HashMap<String, Integer> getIndicesMap() {
        return mapIndex;
    }

    private void generateMapIndex() {
        mapIndex = new LinkedHashMap<>(256);

        for (int i = 0, size = countries.size(); i < size; i++) {
            Country country = countries.get(i);
            String key = String.valueOf(country.localizedName().substring(0, 1));

            if (!mapIndex.containsKey(key)) {
                mapIndex.put(key, i);
            }
        }
    }
}
