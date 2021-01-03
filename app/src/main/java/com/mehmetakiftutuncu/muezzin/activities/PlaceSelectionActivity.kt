package com.mehmetakiftutuncu.muezzin.activities

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.fragment.app.Fragment
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.fragments.CitySelectionFragment
import com.mehmetakiftutuncu.muezzin.fragments.CountrySelectionFragment
import com.mehmetakiftutuncu.muezzin.fragments.DistrictSelectionFragment
import com.mehmetakiftutuncu.muezzin.fragments.SelectionFragment
import com.mehmetakiftutuncu.muezzin.models.City
import com.mehmetakiftutuncu.muezzin.models.Country
import com.mehmetakiftutuncu.muezzin.models.District
import com.mehmetakiftutuncu.muezzin.models.Place
import com.mehmetakiftutuncu.muezzin.utilities.Pref

class PlaceSelectionActivity: MuezzinActivity() {
    private var countryId: Int = 0
    private var cityId: Int = 0
    private var districtId: Int? = null

    private var startedFromPreferences = false

    private val districtSelectedListener = object : SelectionFragment.OnSelectedListener<District> {
        override fun onSelected(item: District) {
            districtId = item.id
            selectPlace()
        }
    }

    private val citySelectedListener = object : SelectionFragment.OnSelectedListener<City> {
        override fun onSelected(item: City) {
            cityId = item.id
            replaceFragment(DistrictSelectionFragment(countryId, cityId, districtSelectedListener), true)
        }
    }

    private val countrySelectedListener = object : SelectionFragment.OnSelectedListener<Country> {
        override fun onSelected(item: Country) {
            countryId = item.id
            replaceFragment(CitySelectionFragment(countryId, citySelectedListener), true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_placeselection)

        savedInstanceState?.getInt("countryId")?.run { countryId = this }
        savedInstanceState?.getInt("cityId")?.run { cityId = this }
        savedInstanceState?.getInt("districtId")?.takeIf { it > 0 }?.run { districtId = this }

        startedFromPreferences = savedInstanceState?.getBoolean(extraStartedFromPreferences) ?: intent?.extras?.getBoolean(extraStartedFromPreferences) ?: false

        val existingFragment =
            supportFragmentManager.findFragmentByTag("DistrictSelectionFragment") ?: let {
                supportFragmentManager.findFragmentByTag("CitySelectionFragment") ?: let {
                    supportFragmentManager.findFragmentByTag("CountrySelectionFragment")
                }
            }

        val fragment = existingFragment ?: CountrySelectionFragment(countrySelectedListener)

        replaceFragment(fragment, false)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        outState.run {
            putInt("countryId", countryId)
            putInt("cityId", cityId)
            districtId?.let { putInt("districtId", it) }
            putBoolean(extraStartedFromPreferences, startedFromPreferences)
        }

        super.onSaveInstanceState(outState, outPersistentState)
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean): Unit =
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout_placeSelectionContainer, fragment, fragment.tag)
                .run {
            if (addToBackStack) {
                addToBackStack(fragment.tag)
            }

            commitAllowingStateLoss()
        }

    private fun selectPlace() {
        Place(countryId, cityId, districtId).also { place ->
            Log.debug(javaClass, "Place '$place' is selected!")
            Pref.Places.setCurrentPlace(ctx, place)

            if (!startedFromPreferences) {
                Intent(ctx, PrayerTimesActivity::class.java).run {
                    putExtras(place.toBundle())
                    startActivity(this)
                }
            }

            finish()
        }
    }

    companion object {
        const val extraStartedFromPreferences = "startedFromPreferences"
    }
}