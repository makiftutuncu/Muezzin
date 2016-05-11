package com.mehmetakiftutuncu.muezzin.adapters.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.Country;

/**
 * Created by akif on 11/05/16.
 */
public class CountryViewHolder extends RecyclerView.ViewHolder {
    private Context context;

    private TextView textViewName;
    private TextView textViewNativeName;

    public CountryViewHolder(Context context, View countryItemLayout) {
        super(countryItemLayout);

        this.context = context;

        textViewName       = (TextView) countryItemLayout.findViewById(R.id.textView_item_country_name);
        textViewNativeName = (TextView) countryItemLayout.findViewById(R.id.textView_item_country_nativeName);
    }

    public void setFrom(Country country) {
        String nameToUse = country.getLocalizedName(context);

        textViewName.setText(nameToUse);

        textViewNativeName.setText(country.nativeName);
    }
}
