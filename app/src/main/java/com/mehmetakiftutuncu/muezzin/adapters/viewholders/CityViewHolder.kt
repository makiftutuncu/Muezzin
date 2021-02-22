package com.mehmetakiftutuncu.muezzin.adapters.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.fragments.SelectionFragment
import com.mehmetakiftutuncu.muezzin.models.City

class CityViewHolder(private val view: View,
                     private val listener: SelectionFragment.OnSelectedListener<City>): RecyclerView.ViewHolder(view) {
    private val textViewName: TextView = view.findViewById(R.id.textView_item_city_name)

    fun set(city: City) {
        textViewName.text = city.name

        view.setOnClickListener {
            listener.onSelected(city)
        }
    }
}