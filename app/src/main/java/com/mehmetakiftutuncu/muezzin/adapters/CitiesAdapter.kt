package com.mehmetakiftutuncu.muezzin.adapters

import android.view.View
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.CityViewHolder
import com.mehmetakiftutuncu.muezzin.fragments.SelectionFragment
import com.mehmetakiftutuncu.muezzin.models.City
import java.util.*

class CitiesAdapter(override val items: List<City>,
                    override val listener: SelectionFragment.OnSelectedListener<City>): SearchableAdapter<City, CityViewHolder>(items, listener) {
    override val itemLayoutId: Int = R.layout.item_city

    override fun hold(view: View, listener: SelectionFragment.OnSelectedListener<City>) =
        CityViewHolder(view, listener)

    override fun set(holder: CityViewHolder, item: City) = holder.set(item)

    override fun search(query: String): List<City> =
        items.filter { c ->
            c.name.toLowerCase(if (c.isTurkish) tr else Locale.getDefault()).contains(query)
        }
}