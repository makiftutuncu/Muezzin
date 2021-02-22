package com.mehmetakiftutuncu.muezzin.utilities

import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

object RemainingTime {
    val formatter: DateTimeFormatter               = DateTimeFormat.forPattern("HH:mm:ss")
    val formatterWithoutSeconds: DateTimeFormatter = DateTimeFormat.forPattern("HH:mm")

    private const val hoursInDay      = 24
    private const val minutesInHour   = 60
    private const val secondsInMinute = 60
    private const val secondsInHour   = 3600

    fun to(to: LocalTime): LocalTime {
        val now = LocalTime.now()

        val toSeconds  = to.hourOfDay * secondsInHour + to.minuteOfHour * 60 + to.secondOfMinute
        val nowSeconds = now.hourOfDay * secondsInHour + now.minuteOfHour * 60 + now.secondOfMinute

        val nowToMidnightSeconds = (hoursInDay - now.hourOfDay - 1) * secondsInHour + (minutesInHour - now.minuteOfHour - 1) * 60 + (secondsInMinute - now.secondOfMinute - 1)

        val diffSeconds = if (toSeconds > nowSeconds) toSeconds - nowSeconds else toSeconds + nowToMidnightSeconds

        val seconds = diffSeconds % secondsInMinute
        val minutes = (diffSeconds - seconds) / secondsInMinute % secondsInMinute
        val hours   = (diffSeconds - secondsInMinute * minutes - seconds) / secondsInHour % hoursInDay

        return LocalTime(hours, minutes, seconds, 0)
    }
}