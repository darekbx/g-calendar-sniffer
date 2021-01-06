package com.darekbx.gcalendarsniffer.model

import com.darekbx.gcalendarsniffer.DateTimeUtils

class CalendarEvent(
        val title: String,
        val description: String,
        val calendarColor: Int,
        val displayName: String,
        val dateTimeStart: Long,
        val dateTimeEnd: Long,
        val isOrganizer: Boolean,
        val organizer: String,
) {

    val durationFormatted: String
        get() {
            val duration = dateTimeEnd - dateTimeStart
            val durationMinutes = duration / 1000 / 60

            if (durationMinutes > 120 /* two hours */) {
                val durationHours = durationMinutes / 60
                val minutes = durationMinutes - durationHours * 60
                if (minutes > 0) {
                    return "$durationHours hours $minutes mins"
                }else {
                    return "$durationHours hours"
                }
            } else {
                return "$durationMinutes minutes"
            }
        }

    val groupKey: String = "${title}${dateTimeStart}${dateTimeEnd}"
    val formattedStartDate: String = DateTimeUtils.formatter.format(dateTimeStart)
    val formattedEndDate: String = DateTimeUtils.formatter.format(dateTimeEnd)

}
