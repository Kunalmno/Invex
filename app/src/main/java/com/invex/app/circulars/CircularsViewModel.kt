package com.invex.app.circulars

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

class CircularsViewModel : ViewModel() {

    private val _state = MutableStateFlow(CircularsUiState())
    val state: StateFlow<CircularsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    /**
     * Loads the circulars. Runs at start and every time a sheet opens, so new ones show up
     * and the temporary links to the PDFs (valid 1 hour) are always fresh.
     */
    fun refresh() {
        _state.update { it.copy(isLoading = it.circulars.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.notices.load(NoticeKind.CIRCULAR)) {
                is NoticesResult.Success -> _state.update {
                    it.copy(
                        circulars = result.notices.map { n ->
                            Circular(
                                id = n.id,
                                heading = n.heading,
                                subheading = n.subheading,
                                snippet = n.snippet,
                                body = n.body,
                                circulatedBy = n.authorName,
                                attachment = n.attachmentName?.let { name ->
                                    CircularAttachment(fileName = name, dateText = n.publishedText, url = n.attachmentUrl.orEmpty())
                                },
                            )
                        },
                        isLoading = false,
                    )
                }
                is NoticesResult.Failure -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    /** Tap on the "Circulars" heading on the Dashboard. */
    fun openList() {
        _state.update { it.copy(sheet = CircularSheet.List) }
        refresh()
    }

    /** Tap on a circular row, on the Dashboard or inside the list. Opens that circular's own page. */
    fun openDetail(id: String) {
        _state.update { it.copy(sheet = CircularSheet.Detail(id)) }
        refresh()
    }

    /** Swipe down or tap outside. Always goes back to the Dashboard. */
    fun dismiss() = _state.update { it.copy(sheet = CircularSheet.Closed) }
}
