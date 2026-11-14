package com.invex.app.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.duties.DutyRecordsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Teacher: the rooms they are on duty in. Student: the exams they sit. Both come from the same place as the
 * Duties / Exams tab (`duty_overview` or `student_exam_overview`), all days at once; the strip just picks a day.
 */
class ScheduleViewModel : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState())
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.duties.loadRecords()) {
                is DutyRecordsResult.Success -> {
                    val zone = ZoneId.systemDefault()
                    val events = result.records.map { record ->
                        val start = record.startsAt.atZone(zone)
                        val end = record.endsAt.atZone(zone)
                        val startHour = start.hour
                        val endHour = (end.hour + if (end.minute > 0) 1 else 0).coerceIn(startHour + 1, 23)
                        ScheduleEvent(
                            id = record.sessionId,
                            title = record.building,
                            startHour = startHour,
                            endHour = endHour,
                            date = start.toLocalDate(),
                        )
                    }
                    _state.update { it.copy(allEvents = events, isLoading = false) }
                }
                is DutyRecordsResult.Failure -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onDateSelected(date: LocalDate) = _state.update { it.copy(selectedDate = date) }

    /** The chevrons move the strip by a week and keep the same weekday selected. */
    fun onPreviousWeek() = shiftWeek(-7)

    fun onNextWeek() = shiftWeek(7)

    private fun shiftWeek(days: Long) = _state.update {
        it.copy(weekStart = it.weekStart.plusDays(days), selectedDate = it.selectedDate.plusDays(days))
    }
}
