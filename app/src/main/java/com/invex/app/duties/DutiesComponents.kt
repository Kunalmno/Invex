package com.invex.app.duties

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.InvexHairline
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.components.ScreenHeader
import com.invex.app.ui.theme.DutyCardBackground
import com.invex.app.ui.theme.InvexAccent
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.LinkDark
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText
import androidx.compose.ui.graphics.Color

/** Chevron + orange briefcase circle + underlined "Duties" (shared header, same as the other teacher screens). */
@Composable
fun DutiesHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    ScreenHeader(
        title = if (InvexAccent.isStudent) "Exams" else "Duties",
        glyphRes = if (InvexAccent.isStudent) R.drawable.ic_duties_icon else R.drawable.ic_business_briefcase,
        glyphPadding = PaddingValues(6.dp),
        onBack = onBack,
        modifier = modifier,
    )
}

/** "Upcoming" / "Completed". Only the selected tab has the orange underline; the other is plain black. */
@Composable
fun DutiesTabRow(
    selected: DutiesTab,
    onTabSelected: (DutiesTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(40.dp)) {
        DutiesTab.entries.forEach { tab ->
            val click = Modifier.clickable { onTabSelected(tab) }
            if (tab == selected) {
                LineUnderlinedText(
                    text = tab.name,
                    style = InvexType.DutiesTabLabel,
                    color = Color.Black,
                    underlineColor = Orange,
                    underlineOffset = 4.dp,
                    modifier = click,
                )
            } else {
                Text(tab.name, style = InvexType.DutiesTabLabel, color = Color.Black, modifier = click)
            }
        }
    }
}

/**
 * White card (radius 16, 12dp padding): Building / Date / Time rows on the left, the classroom drawing
 * at the top right, and the black "view full room seating" button across the bottom.
 */
@Composable
fun DutyCard(
    duty: DutyItem,
    onViewRoomSeating: (DutyItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DutyCardBackground)
            .border(InvexHairline, shape),
    ) {
        // Drawing keeps its own 126 x 171 ratio (it was squashed to 126 x 120 before). Sits flush to the right edge.
        Image(
            painter = painterResource(R.drawable.ic_room_card_background),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 28.dp)
                .size(width = 126.dp, height = 171.dp),
        )
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetaBlock(R.drawable.ic_building, "Building", duty.building, InvexType.DutyTitle)
            MetaBlock(R.drawable.ic_date, "Date", duty.date, InvexType.DutyValue)
            MetaBlock(R.drawable.ic_clock, "Time", duty.timeRange, InvexType.DutyValue)
            ViewRoomSeatingButton(onClick = { onViewRoomSeating(duty) }, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun MetaBlock(
    iconRes: Int,
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, style = InvexType.DutyMetaLabel, color = TableHeaderText)
        }
        Text(value, style = valueStyle, color = Color.Black)
    }
}

/** Black pill, 48dp high: door icon, pixel-font label, white circle with arrow. */
@Composable
fun ViewRoomSeatingButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(LinkDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.ic_view_seat_door), contentDescription = null, modifier = Modifier.size(32.dp))
        Text(if (InvexAccent.isStudent) "View Your Seat" else "view full room seating", style = InvexType.DutyButton, color = SoftWhite)
        Image(painterResource(R.drawable.ic_view_seat_navigation), contentDescription = null, modifier = Modifier.size(32.dp))
    }
}

/** Shown when the selected tab has no duties (not in the design; kept minimal). */
@Composable
fun DutiesEmptyState(tab: DutiesTab, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        Text(
            text = (if (tab == DutiesTab.Upcoming) "No upcoming " else "No completed ") +
                if (InvexAccent.isStudent) "exams" else "duties",
            style = InvexType.Body16,
            color = TableHeaderText,
        )
    }
}
