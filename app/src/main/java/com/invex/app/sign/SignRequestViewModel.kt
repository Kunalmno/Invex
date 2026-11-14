package com.invex.app.sign

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SignRequestViewModel : ViewModel() {

    private val _state = MutableStateFlow(SignRequestUiState())
    val state: StateFlow<SignRequestUiState> = _state.asStateFlow()

    /** Call every time the sheet opens. Starts blank, except for a signature saved earlier. */
    fun reset() = _state.update { s ->
        val saved = s.savedSignature
        SignRequestUiState(
            tab = saved?.tab ?: SignTab.DRAW,
            strokes = saved?.strokes.orEmpty(),
            typedName = saved?.typedName.orEmpty(),
            savedSignature = saved,
        )
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query, warning = null) }

    /** Tapping the selected student again deselects them. */
    fun onSelectStudent(id: String) = _state.update {
        it.copy(selectedStudentId = if (it.selectedStudentId == id) null else id, warning = null)
    }

    fun onTabChange(tab: SignTab) = _state.update { it.copy(tab = tab, warning = null) }

    // Drawing: each stroke is a list of points; a new drag starts a new stroke.
    fun onStrokeStart(point: Offset) = _state.update {
        it.copy(strokes = it.strokes + listOf(listOf(point)), warning = null)
    }

    fun onStrokeMove(point: Offset) = _state.update { s ->
        if (s.strokes.isEmpty()) s
        else s.copy(strokes = s.strokes.dropLast(1) + listOf(s.strokes.last() + point))
    }

    /** "Back": undo the last stroke (Draw) or the last character (Type). */
    fun onBack() = _state.update { s ->
        when (s.tab) {
            SignTab.DRAW -> s.copy(strokes = s.strokes.dropLast(1))
            SignTab.TYPE -> s.copy(typedName = s.typedName.dropLast(1))
        }
    }

    fun onClear() = _state.update { s ->
        when (s.tab) {
            SignTab.DRAW -> s.copy(strokes = emptyList())
            SignTab.TYPE -> s.copy(typedName = "")
        }
    }

    fun onTypedNameChange(name: String) = _state.update { it.copy(typedName = name, warning = null) }

    // In memory only for now: persist it (backend or DataStore) when accounts exist.
    fun onSaveSignature() = _state.update { s ->
        if (s.hasSignature) s.copy(savedSignature = SavedSignature(s.tab, s.strokes, s.typedName)) else s
    }

    fun onConsentChange(consent: Boolean) = _state.update { it.copy(consent = consent, warning = null) }

    /**
     * Validates the request. Returns the selected student's id when it can be issued,
     * otherwise null and the banner shows the first problem (student, signature, consent).
     */
    fun submit(): String? {
        val s = _state.value
        val warning = when {
            s.selectedStudentId == null -> SignWarning.NO_STUDENT
            !s.hasSignature -> SignWarning.NO_SIGNATURE
            !s.consent -> SignWarning.NO_CONSENT
            else -> null
        }
        _state.update { it.copy(warning = warning) }
        return if (warning == null) s.selectedStudentId else null
    }

    /** For sheets that need no student (the admit card): true when there is a signature and consent. */
    fun submitSignature(): Boolean {
        val s = _state.value
        val warning = when {
            !s.hasSignature -> SignWarning.NO_SIGNATURE
            !s.consent -> SignWarning.NO_CONSENT
            else -> null
        }
        _state.update { it.copy(warning = warning) }
        return warning == null
    }
}
