package com.darekbx.gcalendarsniffer.calendar

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import com.darekbx.gcalendarsniffer.R
import com.darekbx.gcalendarsniffer.model.CalendarEvent
import java.time.Instant
import java.util.*

class EventsProvider(val context: Context) {

    companion object {
        private val EVENTS_URI = Uri.parse("content://com.android.calendar/events")
        private val SNIFF_START_DATE = Date.from(Instant.parse("2021-01-01T00:00:00.00Z")).time
        private val projection = arrayOf(
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.CALENDAR_COLOR,
                CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.IS_ORGANIZER,
                CalendarContract.Events.ORGANIZER,
        )
        private const val SORTING_ORDER = "${CalendarContract.Events._ID} DESC"
    }

    fun readEvents(): List<CalendarEvent> {
        val calendarEvents = mutableListOf<CalendarEvent>()
        queryEvents(context.contentResolver)?.use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    calendarEvents.add(createCalendarEvent(cursor))
                } while (cursor.moveToNext())
            }
        }

        val grouppedEvents = groupByDisplayName(calendarEvents)
        return grouppedEvents
    }

    private fun groupByDisplayName(calendarEvents: MutableList<CalendarEvent>): List<CalendarEvent> {
        return calendarEvents
                .groupBy { it.groupKey }
                .map { entry ->
                    val firstEvent = entry.value.first()
                    val displayNamesList = prepareNamesList(entry)
                    val displayNames = createNamesToDisplay(displayNamesList)
                    CalendarEvent(
                            firstEvent.title,
                            firstEvent.description,
                            firstEvent.calendarColor,
                            displayNames,
                            firstEvent.dateTimeStart,
                            firstEvent.dateTimeEnd,
                            firstEvent.isOrganizer,
                            firstEvent.organizer)
                }
    }

    private fun createNamesToDisplay(displayNamesList: List<String>) = when {
        displayNamesList.size > 3 -> {
            val diff = displayNamesList.size - 3
            "${displayNamesList.take(3).joinToString()} +$diff"
        }
        else -> displayNamesList.joinToString()
    }

    private fun prepareNamesList(entry: Map.Entry<String, List<CalendarEvent>>) =
            entry.value
                    .sortedBy { it.isOrganizer }
                    .mapIndexed { index, item ->
                        when {
                            index == 0 -> "${item.organizer} ${context.getString(R.string.organizer)}"
                            else -> item.displayName
                        }
                    }

    private fun queryEvents(contentResolver: ContentResolver) =
            contentResolver.query(EVENTS_URI,
                    projection,
                    """
                        -- Show events which starts from ceratin date
                        ${CalendarContract.Events.DTSTART} > ? 
                        
                        -- Don't show all day events
                        AND ${CalendarContract.Events.ALL_DAY} = 0 
                        
                        -- Don't show reccuring events
                        AND ${CalendarContract.Events.RRULE} IS NULL AND ${CalendarContract.Events.ORIGINAL_INSTANCE_TIME} IS NULL 
                         
                        -- Don't show own events
                        AND ${CalendarContract.Events.ACCOUNT_NAME} != ${CalendarContract.Events.CALENDAR_DISPLAY_NAME} 
                    """,
                    arrayOf("$SNIFF_START_DATE"),
                    SORTING_ORDER)

    private fun createCalendarEvent(cursor: Cursor) = CalendarEvent(
            cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)),
            cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DESCRIPTION)),
            cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_COLOR)),
            cursor.getString(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_DISPLAY_NAME)),
            cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART)),
            cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND)),
            cursor.getInt(cursor.getColumnIndex(CalendarContract.Events.IS_ORGANIZER)) == 0,
            cursor.getString(cursor.getColumnIndex(CalendarContract.Events.ORGANIZER)),
    )
}
