package com.mehmetakiftutuncu.muezzin.fragments

import android.content.Context
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.adapters.CitiesAdapter
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.CityViewHolder
import com.mehmetakiftutuncu.muezzin.models.City
import com.mehmetakiftutuncu.muezzin.repositories.CityRepository
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPI

class CitySelectionFragment(private val countryId: Int,
                            override val listener: OnSelectedListener<City>): SelectionFragment<City, SelectionFragment.OnSelectedListener<City>, CityViewHolder, CitiesAdapter>(listener) {
    override val fragmentId = R.layout.fragment_cityselection

    override val multiStateViewId = R.id.multiStateView_citySelection

    override val recyclerViewId = R.id.recyclerView_citySelection

    override val floatingSearchViewId = R.id.floatingSearchView_citySearch

    override val titleId = R.string.placeSelection_city

    override fun loadFromDB(ctx: Context): List<City> = CityRepository.get(ctx, countryId)

    override fun download(ctx: Context, onFail: (Throwable) -> Unit, onSuccess: (List<City>) -> Unit) =
        MuezzinAPI.getCities(ctx, countryId, onFail, onSuccess)

    override fun saveToDB(ctx: Context, items: List<City>): Boolean =
        CityRepository.save(ctx, countryId, items)

    override fun adapt(items: List<City>): CitiesAdapter =
        CitiesAdapter(items, listener)
}