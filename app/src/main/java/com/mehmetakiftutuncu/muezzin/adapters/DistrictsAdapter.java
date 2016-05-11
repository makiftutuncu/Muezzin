package com.mehmetakiftutuncu.muezzin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.DistrictViewHolder;
import com.mehmetakiftutuncu.muezzin.models.District;

import java.util.ArrayList;

/**
 * Created by akif on 11/05/16.
 */
public class DistrictsAdapter extends RecyclerView.Adapter<DistrictViewHolder> {
    private ArrayList<District> districts;

    public DistrictsAdapter(ArrayList<District> districts) {
        this.districts = districts;
    }

    @Override public DistrictViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View districtItemLayout = LayoutInflater.from(context).inflate(R.layout.item_district, parent, false);

        return new DistrictViewHolder(districtItemLayout);
    }

    @Override public void onBindViewHolder(DistrictViewHolder holder, int position) {
        holder.setFrom(districts.get(position));
    }

    @Override public int getItemCount() {
        return districts != null ? districts.size() : 0;
    }
}
