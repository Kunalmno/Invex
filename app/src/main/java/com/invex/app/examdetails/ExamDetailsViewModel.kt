package com.invex.app.examdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.exam.ExamDetailsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Read-only: nothing on this screen edits data. The exam (session) id comes from the navigation route
 * ("exam_details/{sessionId}"), which the Dashboard fills from the tapped room chip.
 */
class ExamDetailsViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId").orEmpty()

    private val _state = MutableStateFlow(ExamDetailsUiState())
    val state: StateFlow<ExamDetailsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.examDetails.load(sessionId)) {
                is ExamDetailsResult.Success -> _state.update {
                    it.copy(
                        courses = result.courses,
                        room = result.room,
                        building = result.building,
                        date = result.date,
                        time = result.time,
                        isLoading = false,
                    )
                }
                is ExamDetailsResult.Failure -> _state.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}
