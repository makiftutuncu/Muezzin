package com.mehmetakiftutuncu.muezzin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.models.Country;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CountriesAdapter extends RecyclerView.Adapter<CountriesViewHolder> {
    private List<Country> countries;
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
}
