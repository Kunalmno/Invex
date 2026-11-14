package com.invex.app.schedule

import com.invex.app.ui.theme.accentIcon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** One hour row is 56dp. The label and the line sit at the middle of the row. */
private val RowHeight = 56.dp

/** Chevron + orange calendar icon (circle is inside the drawable) + underlined "Schedule". */
@Composable
fun ScheduleHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chevron_back),
            contentDescription = "Back",
            modifier = Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onBack),
        )
        Image(painterResource(accentIcon(R.drawable.ic_schedule_heading)), contentDescription = null, modifier = Modifier.size(40.dp))
        LineUnderlinedText(
            text = "Schedule",
            style = InvexType.PageHeading,
            color = IconColor,
            underlineColor = Orange,
            underlineOffset = 4.dp,
            thickness = 4.dp,
        )
    }
}

/** White 80dp strip: previous-week chevron, seven day cells (40 wide), next-week chevron. */
@Composable
fun WeekStrip(
    days: List<LocalDate>,
    selected: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chevron_back),
            contentDescription = "Previous week",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(28.dp).clickable(onClick = onPrevious),
        )
        days.forEach { day -> DayCell(day, isSelected = day == selected, onClick = { onSelect(day) }) }
        Image(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = "Next week",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(28.dp).clickable(onClick = onNext),
        )
    }
}

@Composable
private fun DayCell(day: LocalDate, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(40.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Orange else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
            style = InvexType.ScheduleDayName,
            color = if (isSelected) SoftWhite else TableHeaderText,
        )
        Text(
            text = day.dayOfMonth.toString(),
            style = InvexType.ScheduleDayNumber,
            color = if (isSelected) SoftWhite else Color.Black,
        )
    }
}

/**
 * The day timeline. One row per hour: label on the left, a 2dp line on the right.
 * An event is a white card that spans from its start hour line to its end hour line (+8 above and below),
 * and covers the lines it passes over, like in the design.
 */
@Composable
fun DayTimeline(
    hours: List<Int>,
    events: List<ScheduleEvent>,
    modifier: Modifier = Modifier,
) {
    val firstHour = hours.first()
    fun centerOf(hour: Int): Dp = RowHeight * (hour - firstHour) + RowHeight / 2

    Box(modifier.fillMaxWidth().height(RowHeight * hours.size)) {
        hours.forEach { hour ->
            Text(
                text = formatHour(hour),
                style = InvexType.ScheduleHour,
                color = IconColor,
                modifier = Modifier.padding(start = 24.dp).offset(y = centerOf(hour) - 8.dp),
            )
            val covered = events.any { hour in it.startHour..it.endHour }
            if (!covered) {
                Box(
                    Modifier
                        .padding(start = 152.dp, end = 24.dp)
                        .offset(y = centerOf(hour) - 1.dp)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.Black),
                )
            }
        }
        events.forEach { event ->
            val top = centerOf(event.startHour) - 8.dp
            val bottom = centerOf(event.endHour) + 8.dp
            EventCard(
                event = event,
                modifier = Modifier
                    .padding(start = 144.dp, end = 16.dp)
                    .offset(y = top)
                    .fillMaxWidth()
                    .height(bottom - top),
            )
        }
    }
}

@Composable
private fun EventCard(event: ScheduleEvent, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(Color.White).padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(width = 4.dp, height = 48.dp).clip(RoundedCornerShape(2.dp)).background(Orange))
        Text(event.title, style = InvexType.ScheduleEvent, color = Color.Black)
    }
}

/** 9 -> "09:00 AM", 13 -> "01:00 PM", 12 -> "12:00 PM". */
fun formatHour(hour: Int): String {
    val suffix = if (hour < 12) "AM" else "PM"
    val h12 = if (hour % 12 == 0) 12 else hour % 12
    return "%02d:00 %s".format(h12, suffix)
}
