package com.mehmetakiftutuncu.muezzin.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.DistrictViewHolder;
import com.mehmetakiftutuncu.muezzin.fragments.DistrictSelectionFragment;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.District;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by akif on 11/05/16.
 */
public class DistrictsAdapter extends RecyclerView.Adapter<DistrictViewHolder> {
    private List<District> allDistricts;
    private List<District> districts;

    private DistrictSelectionFragment.OnDistrictSelectedListener onDistrictSelectedListener;

    public DistrictsAdapter(List<District> districts, DistrictSelectionFragment.OnDistrictSelectedListener onDistrictSelectedListener) {
        this.allDistricts = districts;
        this.districts    = districts;

        this.onDistrictSelectedListener = onDistrictSelectedListener;
    }

    @Override public DistrictViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();

        View districtItemLayout = LayoutInflater.from(context).inflate(R.layout.item_district, parent, false);

        return new DistrictViewHolder(districtItemLayout, onDistrictSelectedListener);
    }

    @Override public void onBindViewHolder(DistrictViewHolder holder, int position) {
        holder.setFrom(districts.get(position));
    }

    @Override public int getItemCount() {
        return districts != null ? districts.size() : 0;
    }

    public void search(String query, int cityId) {
        String q = query.trim();

        if (q.isEmpty()) {
            districts = allDistricts;

            return;
        }

        districts = new ArrayList<>();
        Locale locale = new Locale("tr", "TR");

        for (int i = 0, size = allDistricts.size(); i < size; i++) {
            District district = allDistricts.get(i);

            if (district.name.toLowerCase(City.isTurkish(cityId) ? locale : Locale.getDefault()).contains(q)) {
                districts.add(district);
            }
        }

        notifyDataSetChanged();
    }
}
