package com.invex.app.supplement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.supplement.SupplementResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupplementUiState(
    val summary: SheetSummary = SheetSummary(),
    val query: String = "",
    val roster: List<RosterStudent> = emptyList(),
    val allStudents: List<StudentSheet> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /** True while a sheet is being issued, so a double tap cannot issue two. */
    val isIssuing: Boolean = false,
    /** Shown in a small dialog when issuing failed (out of stock, no internet). */
    val issueError: String? = null,
) {
    val visibleStudents: List<StudentSheet>
        get() = if (query.isBlank()) allStudents else allStudents.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.rollNumber.contains(query, ignoreCase = true) ||
                it.course.contains(query, ignoreCase = true)
        }
}

/** The exam (session) id comes from the route "supplement/{sessionId}". */
class SupplementViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId").orEmpty()

    private val _state = MutableStateFlow(SupplementUiState())
    val state: StateFlow<SupplementUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = it.allStudents.isEmpty() && it.roster.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.supplement.load(sessionId)) {
                is SupplementResult.Success -> _state.update {
                    it.copy(
                        summary = result.data.summary,
                        roster = result.data.roster,
                        allStudents = result.data.issued,
                        isLoading = false,
                    )
                }
                is SupplementResult.Failure -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun onDismissIssueError() = _state.update { it.copy(issueError = null) }

    /**
     * Issues one more sheet to a student. Product rule: a repeat request increments the student's
     * number instead of adding a row (the database does this). Request metadata (time, who) is stored
     * by the backend. [onResult] gets true when it worked, so the sign sheet can close.
     */
    fun issueSheet(seatId: String, onResult: (Boolean) -> Unit) {
        if (_state.value.isIssuing) return
        _state.update { it.copy(isIssuing = true, issueError = null) }
        viewModelScope.launch {
            val error = InvexBackend.supplement.issue(seatId)
            _state.update { it.copy(isIssuing = false, issueError = error) }
            if (error == null) refresh()
            onResult(error == null)
        }
    }
}
