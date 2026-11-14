package com.invex.app.rooms

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.rooms.RoomLayoutResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The exam (session) id comes from the route "room_view/{sessionId}". Read-only: the admin decides the seats. */
class RoomsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId").orEmpty()

    private val _state = MutableStateFlow(RoomsUiState())
    val state: StateFlow<RoomsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = it.seats.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.roomLayout.load(sessionId)) {
                is RoomLayoutResult.Success -> {
                    val layout = result.layout
                    val taken = layout.seats.associateBy { it.row to it.col }
                    // Every cell of the room: a student's seat, or an empty one.
                    val seats = (1..layout.seatRows).flatMap { r ->
                        (1..layout.seatCols).map { c ->
                            val seat = taken[r to c]
                            SeatData(
                                row = rowLetter(r),
                                col = c,
                                state = when {
                                    seat == null -> SeatState.Empty
                                    seat.status == "present" -> SeatState.Verified
                                    seat.status == "absent" -> SeatState.Absent
                                    else -> SeatState.Assigned
                                },
                                studentName = seat?.studentName,
                                registrationNumber = seat?.registrationNumber,
                                course = seat?.courseCode,
                                isMine = seat?.isMine == true,
                            )
                        }
                    }
                    _state.update {
                        it.copy(
                            roomInfo = RoomInfo(
                                roomNumber = layout.roomNumber,
                                block = layout.block,
                                // A student sees their own course; a teacher all the courses of the room.
                                course = layout.seats.firstOrNull { s -> s.isMine }?.courseCode
                                    ?: layout.courses.joinToString(", ").ifEmpty { "-" },
                                division = layout.division?.takeIf { d -> d.isNotBlank() },
                            ),
                            seats = seats,
                            courses = layout.courses.map { code -> CourseItem(code) },
                            isLoading = false,
                        )
                    }
                }
                is RoomLayoutResult.Failure -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    /** Tap a seat: shows who sits there (read-only; seats cannot be changed here). */
    fun onSeatClick(seat: SeatData) = _state.update { it.copy(selectedSeat = seat) }

    fun onDismissSeat() = _state.update { it.copy(selectedSeat = null) }
}
