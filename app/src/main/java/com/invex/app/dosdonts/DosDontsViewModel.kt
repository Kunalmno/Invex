package com.invex.app.dosdonts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.tips.TipsResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DosDontsViewModel : ViewModel() {

    private val _state = MutableStateFlow(DosDontsUiState())
    val state: StateFlow<DosDontsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.tips.load()) {
                is TipsResult.Success -> _state.update { it.copy(dos = result.dos, donts = result.donts, isLoading = false) }
                is TipsResult.Failure -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}
