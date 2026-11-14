package com.invex.app.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.results.ResultsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Read-only: the admin enters the grades. Opens on the latest semester that has grades. */
class ResultsViewModel : ViewModel() {

    private val _state = MutableStateFlow(ResultsUiState())
    val state: StateFlow<ResultsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.results.load()) {
                is ResultsResult.Success -> _state.update {
                    it.copy(
                        semesters = result.semesters,
                        selectedSemester = it.selectedSemester?.takeIf { s -> result.semesters.any { r -> r.semester == s } }
                            ?: result.semesters.lastOrNull()?.semester,
                        isLoading = false,
                    )
                }
                is ResultsResult.Failure -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onDropdownToggle(open: Boolean) = _state.update { it.copy(dropdownOpen = open) }

    fun onSemesterSelected(semester: Int) =
        _state.update { it.copy(selectedSemester = semester, dropdownOpen = false) }
}
