package com.darekbx.gcalendarsniffer

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darekbx.gcalendarsniffer.calendar.EventsProvider
import com.darekbx.gcalendarsniffer.model.CalendarEvent
import com.darekbx.gcalendarsniffer.ui.GCalendarSnifferTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        CoroutineScope(Dispatchers.IO).launch {
           val calendarEvents = loadCalendarEvents()
            withContext(Dispatchers.Main) {
                displayCalendarEventsList(calendarEvents)
            }
        }
    }

    private fun loadCalendarEvents() =
            EventsProvider(this as Context).readEvents()

    private fun displayCalendarEventsList(calendarEvents: List<CalendarEvent>) {
        setContent {
            GCalendarSnifferTheme {
                Surface(color = MaterialTheme.colors.background) {
                    CalendarEventsList(
                            modifier = Modifier.fillMaxSize(),
                            calendarEvents,
                    )
                }
            }
        }
    }
}


@Composable
fun CalendarEventsList(
        modifier: Modifier = Modifier,
        simpleListDataItems: List<CalendarEvent>
) {
    LazyColumn(modifier = modifier) {
        items(simpleListDataItems) { data ->
            CalendarEventListItem(item = data)
        }
    }
}

val smallTextStyle = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
)
val smallTextStyleBold = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp
)

@Composable
fun CalendarEventListItem(item: CalendarEvent) {
    Card(
            shape = RoundedCornerShape(8.dp),
            backgroundColor = Color(item.calendarColor),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
    ) {
        val openDialog = remember { mutableStateOf(false)  }
        Box(modifier = Modifier.padding(16.dp).clickable(onClick = { openDialog.value = true  })) {
            Column {
                Row {
                    Text(stringResource(id = R.string.from), color = Color.White, style = smallTextStyle)
                    Text(item.formattedStartDate, color = Color.White, style = smallTextStyleBold)
                    Text(stringResource(id = R.string.to), color = Color.White, style = smallTextStyle)
                    Text(item.formattedEndDate, color = Color.White, style = smallTextStyleBold)
                    Text(" (${item.durationFormatted})", color = Color.White, style = smallTextStyle)
                }
                Text(item.title, color = Color.White, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                Text(item.displayName,
                        color = Color.White,
                        style = smallTextStyle
                )
                if (openDialog.value) {
                    detailsDialog(item, openDialog)
                }
            }
        }
    }
}

@Composable
private fun detailsDialog(item: CalendarEvent, openDialog: MutableState<Boolean>) {
    AlertDialog(
            onDismissRequest = { },
            title = { Text(item.title) },
            text = {
                ScrollableColumn(modifier = Modifier.height(300.dp)) {
                    Text(item.description)
                }
            },
            confirmButton = { },
            dismissButton = {
                Button(onClick = { openDialog.value = false }) {
                    Text(stringResource(id = R.string.close))
                }
            }
    )
}
