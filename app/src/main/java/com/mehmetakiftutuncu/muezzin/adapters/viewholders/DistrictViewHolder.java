package com.mehmetakiftutuncu.muezzin.adapters.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.District;

/**
 * Created by akif on 11/05/16.
 */
public class DistrictViewHolder extends RecyclerView.ViewHolder {
    private TextView textViewName;

    public DistrictViewHolder(View districtItemLayout) {
        super(districtItemLayout);

        textViewName = (TextView) districtItemLayout.findViewById(R.id.textView_item_district_name);
    }

    public void setFrom(District district) {
        textViewName.setText(district.name);
    }
}
