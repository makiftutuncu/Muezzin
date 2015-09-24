package com.mehmetakiftutuncu.muezzin.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.interfaces.OnItemClickedListener;
import com.mehmetakiftutuncu.muezzin.models.Country;

public class CountriesViewHolder extends RecyclerView.ViewHolder {
    public TextView name;
    public TextView details;

    public CountriesViewHolder(final View countryItemLayout, final OnItemClickedListener onItemClickedListener) {
        super(countryItemLayout);

        name    = (TextView) countryItemLayout.findViewById(R.id.textView_listItem_primary);
        details = (TextView) countryItemLayout.findViewById(R.id.textView_listItem_secondary);

        countryItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickedListener != null) {
                    onItemClickedListener.onItemClicked(countryItemLayout, getAdapterPosition());
                }
            }
        });
    }

    public void setCountry(Country country) {
        if (country != null) {

            if (name != null) {
                name.setText(country.localizedName());
            }

            if (details != null) {
                details.setText(country.nativeName);
            }
        }
    }
}
