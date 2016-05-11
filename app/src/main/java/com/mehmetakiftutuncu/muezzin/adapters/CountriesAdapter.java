package com.mehmetakiftutuncu.muezzin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.CountryViewHolder;
import com.mehmetakiftutuncu.muezzin.models.Country;

import java.util.ArrayList;

/**
 * Created by akif on 11/05/16.
 */
public class CountriesAdapter extends RecyclerView.Adapter<CountryViewHolder> {
    private ArrayList<Country> countries;

    public CountriesAdapter(ArrayList<Country> countries) {
        this.countries = countries;
    }

    @Override public CountryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View countryItemLayout = LayoutInflater.from(context).inflate(R.layout.item_county, parent, false);

        return new CountryViewHolder(context, countryItemLayout);
    }

    @Override public void onBindViewHolder(CountryViewHolder holder, int position) {
        holder.setFrom(countries.get(position));
    }

    @Override public int getItemCount() {
        return countries != null ? countries.size() : 0;
    }
}
