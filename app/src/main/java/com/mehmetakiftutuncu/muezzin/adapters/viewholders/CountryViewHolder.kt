package com.mehmetakiftutuncu.muezzin.adapters.viewholders

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.fragments.SelectionFragment
import com.mehmetakiftutuncu.muezzin.models.Country

class CountryViewHolder(private val ctx: Context,
                        private val view: View,
                        private val listener: SelectionFragment.OnSelectedListener<Country>): RecyclerView.ViewHolder(view) {
    private val textViewName: TextView = view.findViewById(R.id.textView_item_country_name)
    private val textViewNativeName: TextView = view.findViewById(R.id.textView_item_country_nativeName)

    fun set(country: Country) {
        textViewName.text = country.localizedName(ctx)
        textViewNativeName.text = country.nameNative

        view.setOnClickListener {
            listener.onSelected(country)
        }
    }
}