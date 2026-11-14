package com.invex.app.rooms

import com.invex.app.ui.theme.accentIcon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.InvexHairline
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.CourseLegendGreen
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.InvexAccent
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.SeatTileBackground
import com.invex.app.ui.theme.StatusGreen
import com.invex.app.ui.theme.StatusRed
import com.invex.app.ui.theme.TableHeaderText
import com.invex.app.ui.theme.TableRowInteraction

// ── Header ────────────────────────────────────────────────────────────────────

/**
 * Back chevron + room heading icon (ic_room_heading already contains the orange
 * circle, so it is rendered directly at 50 dp) + "Room" title with orange underline.
 */
@Composable
fun RoomsHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chevron_back),
            contentDescription = "Back",
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
        )
        // ic_room_heading.svg already includes the orange circle — render at 50 dp as-is.
        Image(
            painter = painterResource(accentIcon(R.drawable.ic_room_heading)),
            contentDescription = null,
            modifier = Modifier.size(50.dp),
        )
        LineUnderlinedText(
            text = "Room",
            style = InvexType.PageHeading,
            color = IconColor,
            underlineColor = Orange,
            underlineOffset = 4.dp,
            thickness = 3.dp,
        )
    }
}

// ── Subtitle meta line ────────────────────────────────────────────────────────

/**
 * "Room : 317  |  Block : B  |  Course : OOPS"
 * All values come from [RoomInfo]; separators are literal pipe characters.
 */
@Composable
fun RoomSubtitleLine(
    info: RoomInfo,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Room : ${info.roomNumber}  |  Block : ${info.block}" +
            (info.division?.let { "  |  Division : $it" } ?: "") +
            "  |  Course : ${info.course}",
        style = InvexType.RoomSubtitle,
        color = TableHeaderText,
        modifier = modifier,
    )
}

// ── Seat tile ─────────────────────────────────────────────────────────────────

/**
 * Background colour for each [SeatState], following the spec's colour-coding plan.
 * Only [SeatState.Assigned] appears in the current mockup (pinkish `SeatTileBackground`).
 */
private fun seatBackgroundFor(state: SeatState): Color = when (state) {
    SeatState.Assigned -> SeatTileBackground          // #FDDDD8 pinkish
    SeatState.Empty    -> Color(0xFFE8E8E8)           // OutlineShadow grey
    SeatState.Absent   -> Color(0xFFF24E51).copy(alpha = 0.18f) // StatusRed tinted
    SeatState.Verified -> Color(0xFF66CD4C).copy(alpha = 0.18f) // StatusGreen tinted
}

/**
 * Individual seat tile: sofa icon (black) over a pink rounded card, seat label below.
 */
@Composable
fun SeatTile(
    seat: SeatData,
    onClick: (SeatData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    // Student: every seat is plain, only the student's own seat is highlighted in the accent (blue).
    val studentView = InvexAccent.isStudent
    val background = when {
        !studentView -> seatBackgroundFor(seat.state)
        seat.isMine -> Orange
        else -> SoftWhite
    }
    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .clickable { onClick(seat) }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_seat_room),
            contentDescription = seat.label,
            colorFilter = if (studentView && seat.isMine) ColorFilter.tint(SoftWhite) else null,
            modifier = Modifier.size(30.dp),
        )
        Text(
            text = seat.label,
            style = InvexType.SeatLabel,
            color = Color.Black,
        )
    }
}

// ── Seat grid card ────────────────────────────────────────────────────────────

/**
 * White rounded card housing the full 6-column seat grid.
 * Uses [LazyVerticalGrid] with a fixed 6-column layout.
 *
 * The grid's height is NOT constrained so it expands to fit all rows.
 * To avoid nested scrolling issues inside a [LazyColumn] on the host screen,
 * this composable disables LazyVerticalGrid's own scrolling by passing
 * `userScrollEnabled = false` and relies on the parent LazyColumn to scroll.
 */
@Composable
fun SeatGridCard(
    seats: List<SeatData>,
    onSeatClick: (SeatData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(TableRowInteraction)
            .border(InvexHairline, cardShape)
            .padding(12.dp),
    ) {
        // We use a non-lazy Grid here (Column of Rows) so it doesn't conflict
        // with the outer LazyColumn scroll and doesn't need a fixed height.
        val rows = seats.groupBy { it.row }.entries.sortedBy { it.key }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            rows.forEach { (_, rowSeats) ->
                val sorted = rowSeats.sortedBy { it.col }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    sorted.forEach { seat ->
                        SeatTile(
                            seat = seat,
                            onClick = onSeatClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

// ── Course legend card ────────────────────────────────────────────────────────

/**
 * Single course legend card: dark-green sofa icon + course name.
 * Note: the icon tint is [CourseLegendGreen] (`#1A6B3A`), distinguishing it
 * from the black sofa in the seat tiles.
 */
@Composable
fun CourseLegendCard(
    item: CourseItem,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(TableRowInteraction)
            .border(InvexHairline, shape)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_seat_room),
            contentDescription = null,
            colorFilter = ColorFilter.tint(CourseLegendGreen),
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = item.name,
            style = InvexType.CourseLegend,
            color = Color.Black,
        )
    }
}

/** Student legend: one card, a blue square with a white sofa and "Your Seat". */
@Composable
fun YourSeatLegend(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(TableRowInteraction)
            .border(InvexHairline, shape)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)).background(Orange),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_seat_room),
                contentDescription = null,
                colorFilter = ColorFilter.tint(SoftWhite),
                modifier = Modifier.size(24.dp),
            )
        }
        Text(text = "Your Seat", style = InvexType.CourseLegend, color = Color.Black)
    }
}

/**
 * 2 × 2 grid of course legend cards.
 * Pairs up the [courses] list two-per-row.
 */
@Composable
fun CourseLegendGrid(
    courses: List<CourseItem>,
    modifier: Modifier = Modifier,
) {
    val chunked = courses.chunked(2)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        chunked.forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                pair.forEach { item ->
                    CourseLegendCard(
                        item = item,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill the second slot if the last row has only 1 item
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
