package com.mehmetakiftutuncu.muezzin.fragments.preferences

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.activities.LicencesActivity
import com.mehmetakiftutuncu.muezzin.activities.PlaceSelectionActivity
import com.mehmetakiftutuncu.muezzin.activities.preferences.ReminderPreferencesActivity
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder
import com.mehmetakiftutuncu.muezzin.repositories.PlaceRepository
import com.mehmetakiftutuncu.muezzin.utilities.Pref
import com.mehmetakiftutuncu.muezzin.widget.PrayerTimesWidget

class PreferencesFragment: PreferenceFragmentCompat() {
    private val ctx: Context by lazy { requireActivity() }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onResume() {
        super.onResume()

        initializePreferences()

        PrayerTimeReminder.rescheduleReminders(ctx)
        PrayerTimesWidget.updateAllWidgets(ctx)
    }

    private fun initializePreferences() {
        findPreference<Preference>(keyGeneralPlace)?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                Intent(ctx, PlaceSelectionActivity::class.java).apply {
                    putExtra(PlaceSelectionActivity.extraStartedFromPreferences, true)
                    startActivity(this)
                }
                true
            }

            updatePlaceSummary(this)
        }

        findPreference<Preference>(keyGeneralReminders)?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(ctx, ReminderPreferencesActivity::class.java))
                true
            }
        }

        findPreference<Preference>(keyMoreRate)?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                Intent(Intent.ACTION_VIEW, Uri.parse(rateUri)).let {
                    Intent.createChooser(it, getString(R.string.preferences_more_rate))
                }.apply {
                    startActivity(this)
                }
                true
            }
        }

        findPreference<Preference>(keyMoreFeedback)?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(feedbackContact))
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.applicationName))
                }.let {
                    Intent.createChooser(it, getString(R.string.preferences_more_feedback))
                }.apply {
                    startActivity(this)
                }
                true
            }

        }

        findPreference<Preference>(keyMoreVersion)?.apply {
            summary = Pref.appVersionName(ctx)
        }

        findPreference<Preference>(keyMoreLicenses)?.apply {
            onPreferenceClickListener = Preference.OnPreferenceClickListener {
                startActivity(Intent(ctx, LicencesActivity::class.java))
                true
            }
        }
    }

    private fun updatePlaceSummary(preference: Preference) =
        Pref.Places.getCurrentPlace(ctx)?.let { PlaceRepository.getName(ctx, it) }?.apply {
            preference.summary = this
        }

    companion object {
        const val keyGeneralPlace = "general_place"
        const val keyGeneralReminders = "general_reminders"
        const val keyMoreRate = "more_rate"
        const val keyMoreFeedback = "more_feedback"
        const val keyMoreVersion = "more_version"
        const val keyMoreLicenses = "more_licenses"

        const val rateUri = "market://details?id=com.mehmetakiftutuncu.muezzin"

        const val feedbackContact = "m.akif.tutuncu@gmail.com"
    }
}