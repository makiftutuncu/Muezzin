package com.mehmetakiftutuncu.muezzin.fragments

import android.content.Context
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.adapters.CountriesAdapter
import com.mehmetakiftutuncu.muezzin.adapters.viewholders.CountryViewHolder
import com.mehmetakiftutuncu.muezzin.models.Country
import com.mehmetakiftutuncu.muezzin.repositories.CountryRepository
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPI

class CountrySelectionFragment(override val listener: OnSelectedListener<Country>): SelectionFragment<Country, SelectionFragment.OnSelectedListener<Country>, CountryViewHolder, CountriesAdapter>(listener) {
    override val fragmentId = R.layout.fragment_countryselection

    override val multiStateViewId = R.id.multiStateView_countrySelection

    override val recyclerViewId = R.id.recyclerView_countrySelection

    override val floatingSearchViewId = R.id.floatingSearchView_countrySearch

    override val titleId = R.string.placeSelection_country

    override fun loadFromDB(ctx: Context): List<Country> =
        CountryRepository.get(ctx)

    override fun download(ctx: Context, onFail: (Throwable) -> Unit, onSuccess: (List<Country>) -> Unit) =
        MuezzinAPI.getCountries(ctx, onFail, onSuccess)

    override fun saveToDB(ctx: Context, items: List<Country>): Boolean =
        CountryRepository.save(ctx, items)

    override fun adapt(items: List<Country>): CountriesAdapter =
        CountriesAdapter(items, listener)
}