package com.mehmetakiftutuncu.muezzin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.models.City;
import com.mehmetakiftutuncu.muezzin.models.District;

public class CitiesAndDistrictsViewHolder extends RecyclerView.ViewHolder {
    public TextView primary;
    public TextView secondary;

    public CitiesAndDistrictsViewHolder(final View cityOrDistrictItemLayout, final OnItemClickedListener onItemClickedListener) {
        super(cityOrDistrictItemLayout);

        primary   = (TextView) cityOrDistrictItemLayout.findViewById(R.id.textView_listItem_primary);
        secondary = (TextView) cityOrDistrictItemLayout.findViewById(R.id.textView_listItem_secondary);

        secondary.setVisibility(View.GONE);

        cityOrDistrictItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickedListener != null) {
                    onItemClickedListener.onItemClicked(cityOrDistrictItemLayout, getAdapterPosition());
                }
            }
        });
    }

    public void setCity(City city) {
        if (city != null) {
            if (primary != null) {
                primary.setText(city.name());
            }
        }
    }

    public void setDistrict(District district) {
        if (district != null) {
            if (primary != null) {
                primary.setText(district.name());
            }
        }
    }
}
