package com.mehmetakiftutuncu.muezzin.adapters.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.fragments.SelectionFragment
import com.mehmetakiftutuncu.muezzin.models.District

class DistrictViewHolder(private val view: View,
                         private val listener: SelectionFragment.OnSelectedListener<District>): RecyclerView.ViewHolder(view) {
    private val textViewName: TextView = view.findViewById(R.id.textView_item_district_name)

    fun set(district: District) {
        textViewName.text = district.name

        view.setOnClickListener {
            listener.onSelected(district)
        }
    }
}