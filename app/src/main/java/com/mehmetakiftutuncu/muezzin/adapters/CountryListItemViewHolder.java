package com.mehmetakiftutuncu.muezzin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.Country;

/**
 * Created by akif on 29/08/15.
 */
public class CountryListItemViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView details;

    public CountryListItemViewHolder(View countryItemLayout) {
        super(countryItemLayout);

        name    = (TextView) countryItemLayout.findViewById(R.id.textView_listItem_country_name);
        details = (TextView) countryItemLayout.findViewById(R.id.textView_listItem_country_details);
    }

    public void setCountry(Country country) {
        if (country != null) {
            if (name != null) {
                name.setText(country.trName());
            }

            if (details != null) {
                details.setText(country.name() + " [" + country.nativeName() + "]");
            }
        }
    }
}
