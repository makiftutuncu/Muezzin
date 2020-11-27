package com.mehmetakiftutuncu.muezzin.fragments

import android.content.Context
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.adapters.DistrictsAdapter
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.DistrictViewHolder
import com.mehmetakiftutuncu.muezzin.models.District
import com.mehmetakiftutuncu.muezzin.repositories.DistrictRepository
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPI

class DistrictSelectionFragment(private val countryId: Int,
                                private val cityId: Int,
                                override val listener: OnSelectedListener<District>): SelectionFragment<District, SelectionFragment.OnSelectedListener<District>, DistrictViewHolder, DistrictsAdapter>(listener) {
    override val fragmentId = R.layout.fragment_districtselection

    override val multiStateViewId = R.id.multiStateView_districtSelection

    override val recyclerViewId = R.id.recyclerView_districtSelection

    override val floatingSearchViewId = R.id.floatingSearchView_districtSearch

    override val titleId = R.string.placeSelection_district

    override fun loadFromDB(ctx: Context): List<District> = DistrictRepository.get(ctx, cityId)

    override fun download(ctx: Context, onFail: (Throwable) -> Unit, onSuccess: (List<District>) -> Unit) =
        MuezzinAPI.getDistricts(ctx, countryId, cityId, onFail, onSuccess)

    override fun saveToDB(ctx: Context, items: List<District>): Boolean =
        DistrictRepository.save(ctx, cityId, items)

    override fun adapt(items: List<District>): DistrictsAdapter =
        DistrictsAdapter(cityId, items, listener)
}