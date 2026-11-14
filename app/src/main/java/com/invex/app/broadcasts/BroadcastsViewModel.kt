package com.invex.app.broadcasts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.notices.NoticeKind
import com.invex.app.data.notices.NoticesResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BroadcastsViewModel : ViewModel() {

    private val _state = MutableStateFlow(BroadcastsUiState())
    val state: StateFlow<BroadcastsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    /** Loads the broadcasts. Runs at start and every time a sheet opens, so new ones show up. */
    fun refresh() {
        _state.update { it.copy(isLoading = it.broadcasts.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.notices.load(NoticeKind.BROADCAST)) {
                is NoticesResult.Success -> _state.update {
                    it.copy(
                        broadcasts = result.notices.map { n ->
                            Broadcast(
                                id = n.id,
                                heading = n.heading,
                                subheading = n.subheading,
                                snippet = n.snippet,
                                body = n.body,
                                broadcastedBy = n.authorName,
                            )
                        },
                        isLoading = false,
                    )
                }
                is NoticesResult.Failure -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    /** Tap on the "Broadcasts" heading on the Dashboard. */
    fun openList() {
        _state.update { it.copy(sheet = BroadcastSheet.List) }
        refresh()
    }

    /** Tap on a broadcast row, on the Dashboard or inside the list. Opens that broadcast's own page. */
    fun openDetail(id: String) {
        _state.update { it.copy(sheet = BroadcastSheet.Detail(id)) }
        refresh()
    }

    /** Swipe down or tap outside. Always goes back to the Dashboard. */
    fun dismiss() = _state.update { it.copy(sheet = BroadcastSheet.Closed) }
}
