package com.mehmetakiftutuncu.muezzin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.models.District;
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DistrictsAdapter extends RecyclerView.Adapter<CitiesAndDistrictsViewHolder> {
    private List<District> districts;
    private OnItemClickedListener onItemClickedListener;

    public DistrictsAdapter(List<District> districts, OnItemClickedListener onItemClickedListener) {
        this.districts = districts;

        final Collator collator = LocaleUtils.getCollator();
        Collections.sort(this.districts, new Comparator<District>() {
            @Override
            public int compare(District lhs, District rhs) {
                return collator.compare(lhs.name, rhs.name);
            }
        });

        this.onItemClickedListener = onItemClickedListener;
    }

    @Override
    public CitiesAndDistrictsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View districtItemLayout = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_twolines, parent, false);

        return new CitiesAndDistrictsViewHolder(districtItemLayout, onItemClickedListener);
    }

    @Override
    public void onBindViewHolder(CitiesAndDistrictsViewHolder holder, int position) {
        if (holder != null) {
            holder.setDistrict(districts.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return districts != null ? districts.size() : 0;
    }
}
