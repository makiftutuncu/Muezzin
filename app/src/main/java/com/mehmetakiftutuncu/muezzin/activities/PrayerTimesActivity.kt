package com.mehmetakiftutuncu.muezzin.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.activities.preferences.PreferencesActivity
import com.mehmetakiftutuncu.muezzin.fragments.NoPlacesFoundFragment
import com.mehmetakiftutuncu.muezzin.fragments.PrayerTimesFragment
import com.mehmetakiftutuncu.muezzin.models.Place
import com.mehmetakiftutuncu.muezzin.utilities.Pref
import com.stephentuso.welcome.WelcomeScreenHelper

class PrayerTimesActivity: MuezzinActivity() {
    private val welcomeScreenHelper: WelcomeScreenHelper by lazy {
        WelcomeScreenHelper(this, WelcomeActivity::class.java)
    }

    private val ctx: Context by lazy { this }

    private var shownWelcomeScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_prayertimes)

        shownWelcomeScreen = savedInstanceState?.getBoolean("shownWelcomeScreen") ?: false

        if (!shownWelcomeScreen) {
            shownWelcomeScreen = true

            if (Pref.Application.getVersion(ctx) < 4) {
                // Installed version 2.0 for the first time
                Toast.makeText(ctx, R.string.welcome_updateNotice, Toast.LENGTH_LONG).show()
                Pref.edit(ctx) { clear() }
                welcomeScreenHelper.forceShow()
                Pref.Application.setVersion(this)
            } else {
                welcomeScreenHelper.show(savedInstanceState)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Pref.Places.getCurrentPlace(ctx)?.run { showPrayerTimes(this) } ?: showNoPlacesFound()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_preferences -> {
                startActivity(Intent(this, PreferencesActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        welcomeScreenHelper.onSaveInstanceState(outState)
        outState.putBoolean("shownWelcomeScreen", shownWelcomeScreen)

        super.onSaveInstanceState(outState)
    }

    private fun showNoPlacesFound() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout_prayerTimesContainer, NoPlacesFoundFragment(), "NoPlacesFoundFragment")
            .commit()
    }

    private fun showPrayerTimes(place: Place) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayout_prayerTimesContainer, PrayerTimesFragment(place.toBundle()), "PrayerTimesFragment")
                .commit()
    }
}