package com.invex.app.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.attendance.RosterResult
import com.invex.app.data.attendance.RosterRow
import com.invex.app.ui.theme.LightBrown
import com.invex.app.ui.theme.Mist
import com.invex.app.ui.theme.PaleBrown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val ALL_COURSES = "All Courses"

data class AttendanceUiState(
    val courses: List<String> = listOf(ALL_COURSES),
    val selectedCourse: String = ALL_COURSES,
    val query: String = "",
    val allStudents: List<AttendanceStudent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /** The student whose "Mark present / absent" dialog is open. */
    val studentToMark: AttendanceStudent? = null,
    /** Shown when saving a mark failed. */
    val saveError: String? = null,
) {
    /** Students of the chosen course (all courses by default). Counts and the chart follow this. */
    private val inCourse: List<AttendanceStudent>
        get() = if (selectedCourse == ALL_COURSES) allStudents else allStudents.filter { it.course == selectedCourse }

    val summary: AttendanceSummary
        get() = AttendanceSummary(
            total = inCourse.size,
            present = inCourse.count { it.mark == AttendanceMark.Present },
            absent = inCourse.count { it.mark == AttendanceMark.Absent },
        )

    /** Present / absent / not marked yet, in that order. Empty parts are left out. */
    val chart: List<DonutSlice>
        get() = listOf(
            DonutSlice(summary.present.toFloat(), LightBrown),
            DonutSlice(summary.absent.toFloat(), PaleBrown),
            DonutSlice((summary.total - summary.present - summary.absent).toFloat(), Mist),
        ).filter { it.value > 0f }

    val visibleStudents: List<AttendanceStudent>
        get() = if (query.isBlank()) inCourse else inCourse.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.rollNumber.contains(query, ignoreCase = true) ||
                it.course.contains(query, ignoreCase = true)
        }
}

/** The exam (session) id comes from the route "attendance/{sessionId}". */
class AttendanceViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId").orEmpty()

    private val _state = MutableStateFlow(AttendanceUiState())
    val state: StateFlow<AttendanceUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = it.allStudents.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.attendance.loadRoster(sessionId)) {
                is RosterResult.Success -> {
                    val students = result.rows.map { it.toStudent() }
                    _state.update {
                        val courses = listOf(ALL_COURSES) + students.map { s -> s.course }.distinct().sorted()
                        it.copy(
                            allStudents = students,
                            courses = courses,
                            selectedCourse = if (it.selectedCourse in courses) it.selectedCourse else ALL_COURSES,
                            isLoading = false,
                        )
                    }
                }
                is RosterResult.Failure -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun onCourseSelected(course: String) = _state.update { it.copy(selectedCourse = course) }

    fun onStudentClick(student: AttendanceStudent) =
        _state.update { it.copy(studentToMark = student, saveError = null) }

    fun onDismissMark() = _state.update { it.copy(studentToMark = null) }

    fun onDismissSaveError() = _state.update { it.copy(saveError = null) }

    /** Saves a manual mark, then reloads so the counts and the chart come from the database. */
    fun onMark(present: Boolean) {
        val student = _state.value.studentToMark ?: return
        _state.update { it.copy(studentToMark = null) }
        viewModelScope.launch {
            val error = InvexBackend.attendance.mark(student.id, present)
            if (error != null) _state.update { it.copy(saveError = error) } else refresh()
        }
    }

    private fun RosterRow.toStudent() = AttendanceStudent(
        id = seatId,
        name = studentName,
        course = courseCode,
        rollNumber = registrationNumber,
        seat = "Row $seatRow, Seat $seatCol",
        mark = when (status) {
            "present" -> AttendanceMark.Present
            "absent" -> AttendanceMark.Absent
            else -> AttendanceMark.NotMarked
        },
    )
}
