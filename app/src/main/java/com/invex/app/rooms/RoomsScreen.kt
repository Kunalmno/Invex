package com.invex.app.rooms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.InvexAccent
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.TableHeaderText

// ── Stateful route ────────────────────────────────────────────────────────────

/**
 * Stateful entry-point for the Room View screen. Owns [RoomsViewModel].
 *
 * Call [viewModel].loadRoom() from the host before or after navigation to inject
 * real room data; the ViewModel defaults to "317 / B / OOPS" for preview use.
 */
@Composable
fun RoomsRoute(
    onBack: () -> Unit,
    viewModel: RoomsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    RoomsScreen(
        state = state,
        onBack = onBack,
        onSeatClick = viewModel::onSeatClick,
        onRetry = viewModel::refresh,
    )
    SeatDetailsDialog(seat = state.selectedSeat, onDismiss = viewModel::onDismissSeat)
}

// ── Stateless screen ──────────────────────────────────────────────────────────

/**
 * Stateless Room View screen.
 *
 * Layout (top-to-bottom):
 * 1. [RoomsHeader]       — Back + icon badge + "Room" heading
 * 2. [RoomSubtitleLine]  — "Room : 317 | Block : B | Course : OOPS"
 * 3. [SeatGridCard]      — 7-row × 6-column pink seat tiles
 * 4. [CourseLegendGrid]  — 2 × 2 course legend cards
 */
@Composable
fun RoomsScreen(
    state: RoomsUiState,
    onBack: () -> Unit,
    onSeatClick: (SeatData) -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 20.dp,
            bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Header
        item(key = "header") {
            RoomsHeader(onBack = onBack)
        }

        if (state.isLoading) {
            item(key = "loading") { StatusMessage("Loading the room...") }
        } else if (state.error != null) {
            item(key = "error") { StatusMessage(state.error, actionLabel = "Try again", onAction = onRetry) }
        } else {
            // 2. Subtitle meta line
            item(key = "subtitle") {
                RoomSubtitleLine(info = state.roomInfo)
            }

            // 3. Seat grid card
            item(key = "seat_grid") {
                SeatGridCard(
                    seats = state.seats,
                    onSeatClick = onSeatClick,
                )
            }

            // 4. Legend: courses (teacher) or "Your Seat" (student)
            item(key = "course_legend") {
                if (InvexAccent.isStudent) YourSeatLegend() else CourseLegendGrid(courses = state.courses)
            }
        }
    }
}

/**
 * Who sits on a seat. Read-only and not in the Figma design yet. The database only sends details the viewer
 * may see, so an empty seat or another student's seat (student app) shows no name.
 */
@Composable
fun SeatDetailsDialog(seat: SeatData?, onDismiss: () -> Unit) {
    if (seat == null) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seat ${seat.label}", style = InvexType.Body16) },
        text = {
            val lines = when {
                seat.state == SeatState.Empty -> "Empty seat"
                seat.studentName == null -> "Occupied"
                else -> listOfNotNull(
                    seat.studentName,
                    seat.registrationNumber,
                    seat.course,
                    when (seat.state) {
                        SeatState.Verified -> "Present"
                        SeatState.Absent -> "Absent"
                        else -> "Attendance not marked"
                    },
                ).joinToString("\n")
            }
            Text(lines, style = InvexType.RowMeta, color = TableHeaderText)
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK", color = Orange) } },
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RoomsScreenPreview() {
    InvexTheme {
        RoomsScreen(
            state = RoomsUiState(
                roomInfo = RoomInfo(roomNumber = "317", block = "B", course = "CS101, CS102"),
                seats = sampleSeats(),
                courses = listOf(CourseItem("CS101"), CourseItem("CS102")),
                isLoading = false,
            ),
            onBack = {},
            onSeatClick = {},
        )
    }
}
