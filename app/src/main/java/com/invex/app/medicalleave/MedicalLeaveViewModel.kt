package com.invex.app.medicalleave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.medical.LeaveStudentResult
import com.invex.app.data.medical.LeaveSubmitResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class MedicalLeaveViewModel : ViewModel() {

    private val _state = MutableStateFlow(
        MedicalLeaveUiState(
            applicationDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yy, HH:mm:ss", Locale.ENGLISH)),
        ),
    )
    val state: StateFlow<MedicalLeaveUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.medicalLeave.loadStudent()) {
                is LeaveStudentResult.Success -> _state.update { it.copy(student = result.student, isLoading = false) }
                is LeaveStudentResult.Failure -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun onVerifiedChange(value: Boolean) = _state.update { it.copy(verified = value) }

    /** Reading screen -> form. */
    fun onProceedFromReading() {
        if (_state.value.verified && _state.value.student != null) _state.update { it.copy(step = LeaveStep.Form) }
    }

    fun onBackToReading() = _state.update { it.copy(step = LeaveStep.Reading, submitError = null) }

    fun onConditionMenu(open: Boolean) = _state.update { it.copy(conditionMenuOpen = open) }
    fun onConditionSelected(value: String) = _state.update { it.copy(condition = value, conditionMenuOpen = false) }
    fun onDetailsChange(value: String) = _state.update { it.copy(details = value.take(MAX_DETAILS_LENGTH)) }

    fun onDocumentPicked(document: PickedDocument) = _state.update { it.copy(document = document, documentError = null) }
    fun onDocumentRejected(message: String) = _state.update { it.copy(documentError = message) }
    fun onRemoveDocument() = _state.update { it.copy(document = null, documentError = null) }

    fun onSubmit() {
        val s = _state.value
        if (s.isSubmitting || s.details.isBlank()) return
        _state.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.medicalLeave.submit(s.condition, s.details, s.document)) {
                LeaveSubmitResult.Success -> _state.update { it.copy(isSubmitting = false, step = LeaveStep.Sent) }
                is LeaveSubmitResult.Failure -> _state.update { it.copy(isSubmitting = false, submitError = result.message) }
            }
        }
    }
}
