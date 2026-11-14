package com.invex.app.rooms

// ── Seat state ────────────────────────────────────────────────────────────────

/**
 * Visual state of a single seat tile.
 * [Assigned] = a student sits here (pink, as in the mockup), [Empty] = nobody is assigned,
 * [Absent] / [Verified] = the student is marked absent / present (admit card scanned or marked by hand).
 */
enum class SeatState { Assigned, Empty, Absent, Verified }

/** A single seat in the grid, e.g. row = 'A', col = 1  → label "A1". */
data class SeatData(
    val row: Char,
    val col: Int,
    val state: SeatState = SeatState.Assigned,
    /** Only filled for the seats the viewer may see details of (a teacher: all, a student: their own). */
    val studentName: String? = null,
    val registrationNumber: String? = null,
    val course: String? = null,
    /** True for the signed-in student's own seat (student app). */
    val isMine: Boolean = false,
) {
    val label: String get() = "$row$col"
}

// ── Room meta ─────────────────────────────────────────────────────────────────

/** High-level room information shown in the subtitle meta line. [division] only when the room has one. */
data class RoomInfo(
    val roomNumber: String = "317",
    val block: String = "B",
    val course: String = "OOPS",
    val division: String? = null,
)

/** A course entry shown in the legend at the bottom of the screen. */
data class CourseItem(val name: String)

// ── UI state ──────────────────────────────────────────────────────────────────

/** Full UI state for the Room View screen. */
data class RoomsUiState(
    val roomInfo: RoomInfo = RoomInfo(),
    val seats: List<SeatData> = emptyList(),
    val courses: List<CourseItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /** The seat whose details dialog is open. */
    val selectedSeat: SeatData? = null,
)

/** Sample layout for previews only: 6 rows x 6 columns, rows A to F. */
fun sampleSeats(): List<SeatData> =
    ('A'..'F').flatMap { row -> (1..6).map { col -> SeatData(row, col) } }

/** Row number to letter: 1 = A, 2 = B ... */
fun rowLetter(row: Int): Char = 'A' + (row - 1).coerceIn(0, 25)
