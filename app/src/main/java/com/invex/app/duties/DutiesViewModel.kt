package com.invex.app.duties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.duties.DutiesResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DutiesViewModel : ViewModel() {

    private val _state = MutableStateFlow(DutiesUiState())
    val state: StateFlow<DutiesUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun onTabSelected(tab: DutiesTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    /** Loads the duties again (first open, and the "Try again" button). */
    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.duties.load()) {
                is DutiesResult.Success -> _state.update {
                    it.copy(
                        upcomingDuties = result.upcoming,
                        completedDuties = result.completed,
                        isLoading = false,
                    )
                }
                is DutiesResult.Failure -> _state.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
        }
    }
}
