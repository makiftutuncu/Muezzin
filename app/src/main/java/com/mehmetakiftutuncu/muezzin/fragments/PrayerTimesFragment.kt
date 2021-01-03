package com.mehmetakiftutuncu.muezzin.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.use
import com.github.mehmetakiftutuncu.toolbelt.Log
import com.kennyc.view.MultiStateView
import com.mehmetakiftutuncu.muezzin.R
import com.mehmetakiftutuncu.muezzin.activities.MuezzinActivity
import com.mehmetakiftutuncu.muezzin.extension.themeColor
import com.mehmetakiftutuncu.muezzin.models.Place
import com.mehmetakiftutuncu.muezzin.models.PrayerTimeReminder
import com.mehmetakiftutuncu.muezzin.models.PrayerTimesOfDay
import com.mehmetakiftutuncu.muezzin.repositories.PlaceRepository
import com.mehmetakiftutuncu.muezzin.repositories.PrayerTimesOfDayRepository
import com.mehmetakiftutuncu.muezzin.utilities.MuezzinAPI
import com.mehmetakiftutuncu.muezzin.utilities.Pref
import com.mehmetakiftutuncu.muezzin.utilities.RemainingTime
import com.mehmetakiftutuncu.muezzin.widget.PrayerTimesWidget
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.chrono.IslamicChronology
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.util.*

class PrayerTimesFragment(bundle: Bundle): StatefulFragment() {
    private lateinit var textViewRemainingTimeInfo: TextView
    private lateinit var textViewRemainingTime: TextView
    private lateinit var textViewFajr: TextView
    private lateinit var textViewDhuhr: TextView
    private lateinit var textViewAsr: TextView
    private lateinit var textViewMaghrib: TextView
    private lateinit var textViewIsha: TextView
    private lateinit var textViewShuruq: TextView

    private var defaultTextColor: Int = 0
    private var redTextColor: Int = 0

    private val timer = Timer()

    private val timerTask = object : TimerTask() {
        override fun run() {
            activity?.runOnUiThread { updateRemainingTime() }
        }
    }

    private val place = Place.fromBundle(bundle)
    private var times: PrayerTimesOfDay? = null

    private val ctx: Context by lazy { requireActivity() }

    override fun onStart() {
        super.onStart()
        loadPrayerTimes()
    }

    override fun onResume() {
        super.onResume()
        scheduleRemainingTimeCounter()
    }

    override fun onPause() {
        super.onPause()
        cancelRemainingTimeCounter()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_prayertimes, container, false)?.apply {
            multiStateView            = findViewById(R.id.multiStateView_prayerTimes)
            textViewRemainingTimeInfo = findViewById(R.id.textView_prayerTimes_remainingTimeInfo)
            textViewRemainingTime     = findViewById(R.id.textView_prayerTimes_remainingTime)
            textViewFajr              = findViewById(R.id.textView_prayerTimes_fajrTime)
            textViewShuruq            = findViewById(R.id.textView_prayerTimes_shuruqTime)
            textViewDhuhr             = findViewById(R.id.textView_prayerTimes_dhuhrTime)
            textViewAsr               = findViewById(R.id.textView_prayerTimes_asrTime)
            textViewMaghrib           = findViewById(R.id.textView_prayerTimes_maghribTime)
            textViewIsha              = findViewById(R.id.textView_prayerTimes_ishaTime)

            defaultTextColor = loadDefaultTextColor()
            redTextColor = loadRedTextColor()
        }

    override fun changeStateTo(newState: Int, retryAction: Int) {
        multiStateView.viewState = newState

        when (newState) {
            MultiStateView.VIEW_STATE_LOADING, MultiStateView.VIEW_STATE_ERROR, MultiStateView.VIEW_STATE_EMPTY -> {
                activity?.let { it as MuezzinActivity }?.apply {
                    setTitle(R.string.applicationName)
                    setSubtitle("")
                }

                multiStateView.getView(newState)?.findViewById<View>(R.id.fab_retry)?.setOnClickListener {
                    retry(retryAction)
                }
            }
        }
    }

    override fun retry(action: Int) =
        when (action) {
            retryActionDownload -> {
                changeStateTo(MultiStateView.VIEW_STATE_LOADING, 0)
                downloadAndSave()
            }

            else -> {}
        }

    private fun initializeUI() {
        changeStateTo(MultiStateView.VIEW_STATE_CONTENT, 0)

        PlaceRepository.getName(ctx, place)?.also { placeName ->
            activity?.let { it as MuezzinActivity }?.apply {
                setTitle(placeName)
                setSubtitle("${LocalDate.now().toString(fullDateFormatter)} / ${getHijriDate()}")
            }

            Pref.Places.getLastPlace(ctx)?.takeUnless { it == place }?.also {
                PrayerTimeReminder.rescheduleReminders(ctx)
            }
        }

        times?.also { (_, fajr, shuruq, dhuhr, asr, maghrib, isha, _) ->
            textViewFajr.text = fajr.toString(PrayerTimesOfDay.timeFormatter)
            textViewDhuhr.text = dhuhr.toString(PrayerTimesOfDay.timeFormatter)
            textViewAsr.text = asr.toString(PrayerTimesOfDay.timeFormatter)
            textViewMaghrib.text = maghrib.toString(PrayerTimesOfDay.timeFormatter)
            textViewIsha.text = isha.toString(PrayerTimesOfDay.timeFormatter)
            textViewShuruq.text = shuruq.toString(PrayerTimesOfDay.timeFormatter)
        }

        PrayerTimesWidget.updateAllWidgets(ctx)
    }

    private fun downloadAndSave() {
        MuezzinAPI.getPrayerTimes(place, { e ->
            Log.error(javaClass, e, "Failed to download prayer times for place '$place'!")
            runOnUI { changeStateTo(MultiStateView.VIEW_STATE_ERROR, retryActionDownload) }
        }) { newTimes ->
            if (!PrayerTimesOfDayRepository.save(ctx, place, newTimes)) {
                runOnUI { changeStateTo(MultiStateView.VIEW_STATE_ERROR, retryActionDownload) }
            } else {
                val now = LocalDate.now()

                times = newTimes.find { it.date == now }

                if (times == null) {
                    Log.error(javaClass, "Did not find today's prayer times in downloaded prayer times!")
                    runOnUI { changeStateTo(MultiStateView.VIEW_STATE_EMPTY, retryActionDownload) }
                } else {
                    runOnUI { initializeUI() }
                }
            }
        }
    }

    private fun loadPrayerTimes() {
        changeStateTo(MultiStateView.VIEW_STATE_LOADING, 0)

        when (val dbTimes = PrayerTimesOfDayRepository.getForToday(ctx, place)) {
            null -> {
                Log.debug(javaClass, "Today's prayer times for place '$place' wasn't found on database!")
                downloadAndSave()
            }

            else -> {
                Log.debug(javaClass, "Loaded today's prayer times for place '$place' from database!")
                times = dbTimes
                initializeUI()
            }
        }
    }

    private fun updateRemainingTime() {
        times?.run {
            val nextPrayerTime = this.nextPrayerTime()
            val nextPrayerTimeName = PrayerTimesOfDay.localizedName(ctx, this.nextPrayerTimeType())
            val remaining = RemainingTime.to(nextPrayerTime)
            val remainingTime = remaining.toString(RemainingTime.formatter)
            val lessThan45Minutes = remaining.hourOfDay == 0 && remaining.minuteOfHour < 45

            val color = if (lessThan45Minutes) redTextColor else defaultTextColor

            textViewRemainingTimeInfo.text = getString(R.string.prayerTimes_cardTitle_remainingTime, nextPrayerTimeName)
            textViewRemainingTime.text = remainingTime

            textViewRemainingTimeInfo.setTextColor(color)
            textViewRemainingTime.setTextColor(color)
        }
    }

    private fun scheduleRemainingTimeCounter() {
        timer.scheduleAtFixedRate(timerTask, 0, 1000)
    }

    private fun cancelRemainingTimeCounter() {
        timer.cancel()
        timer.purge()

        if (timerTask.scheduledExecutionTime() > 0) {
            timerTask.cancel()
        }
    }

    private fun getHijriDate(): String {
        val hijriNow = LocalDateTime.now(IslamicChronology.getInstance())
        val originalHijriDate = hijriNow.toString(fullDatePattern, Locale.getDefault())
        val hijriMonthName = getString(
            resources.getIdentifier(
                "hijriMonth" + hijriNow.monthOfYear,
                "string",
                ctx.applicationInfo.packageName
            )
        )

        return originalHijriDate.replace("^(.+) (.+) (.+)$".toRegex(), "$1 $hijriMonthName $3")
    }

    private fun loadDefaultTextColor() =
        context?.themeColor(android.R.attr.textColorSecondary) ?: resources.getColor(R.color.black)


    private fun loadRedTextColor(): Int =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            resources.getColor(R.color.red)
        } else {
            resources.getColor(R.color.red, requireActivity().theme)
        }

    companion object {
        private const val fullDatePattern = "dd MMMM YYYY"
        private val fullDateFormatter: DateTimeFormatter = DateTimeFormat.forPattern(fullDatePattern)
    }
}