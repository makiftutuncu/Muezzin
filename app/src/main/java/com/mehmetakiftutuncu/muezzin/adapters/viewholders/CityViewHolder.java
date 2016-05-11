package com.mehmetakiftutuncu.muezzin.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.City;

/**
 * Created by akif on 11/05/16.
 */
public class CityViewHolder extends RecyclerView.ViewHolder {
    private TextView textViewName;

    public CityViewHolder(View cityItemLayout) {
        super(cityItemLayout);

        textViewName = (TextView) cityItemLayout.findViewById(R.id.textView_item_city_name);
    }

    public void setFrom(City city) {
        textViewName.setText(city.name);
    }
}
