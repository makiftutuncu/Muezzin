package com.mehmetakiftutuncu.muezzin.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.interfaces.OnCitySelectedListener;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.City;

/**
 * Created by akif on 11/05/16.
 */
public class CityViewHolder extends RecyclerView.ViewHolder {
    private View cityItemLayout;
    private TextView textViewName;

    private OnCitySelectedListener onCitySelectedListener;

    public CityViewHolder(View cityItemLayout, OnCitySelectedListener onCitySelectedListener) {
        super(cityItemLayout);

        this.cityItemLayout = cityItemLayout;
        this.onCitySelectedListener = onCitySelectedListener;

        textViewName = (TextView) cityItemLayout.findViewById(R.id.textView_item_city_name);
    }

    public void setFrom(final City city) {
        textViewName.setText(city.name);

        cityItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onCitySelectedListener.onCitySelected(city);
            }
        });
    }
}
