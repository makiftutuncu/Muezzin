package com.mehmetakiftutuncu.muezzin.adapters

import android.view.View
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.CountryViewHolder
import com.mehmetakiftutuncu.muezzin.fragments.SelectionFragment
import com.mehmetakiftutuncu.muezzin.models.Country
import com.mehmetakiftutuncu.muezzin.utilities.LocaleUtils.turkeyFirstSorted
import java.util.*

class CountriesAdapter(override val items: List<Country>,
                       override val listener: SelectionFragment.OnSelectedListener<Country>): SearchableAdapter<Country, CountryViewHolder>(items, listener) {
    override val itemLayoutId: Int = R.layout.item_country

    override fun hold(view: View, listener: SelectionFragment.OnSelectedListener<Country>) =
        CountryViewHolder(ctx, view, listener)

    override fun set(holder: CountryViewHolder, item: Country) = holder.set(item)

    override fun search(query: String): List<Country> =
        items.filter { c ->
            c.name.toLowerCase(Locale.ENGLISH).contains(query) ||
            c.nameTurkish.toLowerCase(tr).contains(query) ||
            c.nameNative.toLowerCase(Locale.getDefault()).contains(query)
        }.turkeyFirstSorted(ctx)
}