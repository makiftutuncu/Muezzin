package com.mehmetakiftutuncu.muezzin.adapters

import android.content.Context
import android.view.View
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.DistrictViewHolder
import com.mehmetakiftutuncu.muezzin.fragments.SelectionFragment
import com.mehmetakiftutuncu.muezzin.models.City
import com.mehmetakiftutuncu.muezzin.models.District
import java.util.*

class DistrictsAdapter(private val cityId: Int,
                       override val items: List<District>,
                       override val listener: SelectionFragment.OnSelectedListener<District>): SearchableAdapter<District, DistrictViewHolder>(items, listener) {
    override fun hold(ctx: Context, view: View, listener: SelectionFragment.OnSelectedListener<District>) =
        DistrictViewHolder(view, listener)

    override fun set(holder: DistrictViewHolder, item: District) = holder.set(item)

    override fun search(query: String): List<District> =
        (if (City.isTurkish(cityId)) tr else Locale.getDefault()).let { locale ->
            items.filter { d ->
                d.name.toLowerCase(locale).contains(query)
            }
        }
}