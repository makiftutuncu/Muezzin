package com.mehmetakiftutuncu.muezzin.adapters.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mehmetakiftutuncu.interfaces.OnCountrySelectedListener;
import com.mehmetakiftutuncu.muezzin.R;
import com.mehmetakiftutuncu.muezzin.models.Country;

/**
 * Created by akif on 11/05/16.
 */
public class CountryViewHolder extends RecyclerView.ViewHolder {
    private Context context;

    private View countryItemLayout;
    private TextView textViewName;
    private TextView textViewNativeName;

    private OnCountrySelectedListener onCountrySelectedListener;

    public CountryViewHolder(Context context, View countryItemLayout, OnCountrySelectedListener onCountrySelectedListener) {
        super(countryItemLayout);

        this.context = context;
        this.countryItemLayout = countryItemLayout;
        this.onCountrySelectedListener = onCountrySelectedListener;

        textViewName       = (TextView) countryItemLayout.findViewById(R.id.textView_item_country_name);
        textViewNativeName = (TextView) countryItemLayout.findViewById(R.id.textView_item_country_nativeName);
    }

    public void setFrom(final Country country) {
        String nameToUse = country.getLocalizedName(context);

        textViewName.setText(nameToUse);
        textViewNativeName.setText(country.nativeName);

        countryItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onCountrySelectedListener.onCountrySelected(country);
            }
        });
    }
}
