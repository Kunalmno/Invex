package com.invex.app.admitcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Reading -> (read, tick, E-Sign) -> card. The signing sheet itself is driven by SignRequestViewModel. */
class AdmitCardViewModel : ViewModel() {

    private val _state = MutableStateFlow(AdmitCardUiState())
    val state: StateFlow<AdmitCardUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            InvexBackend.admitCard.loadSubjects()
                .onSuccess { list -> _state.update { it.copy(subjects = list, isLoading = false) } }
                .onFailure { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
        }
    }

    fun onVerifiedChange(value: Boolean) = _state.update { it.copy(verified = value) }

    fun onOpenSignSheet() {
        val s = _state.value
        if (s.verified && s.subjects.isNotEmpty()) _state.update { it.copy(signSheetOpen = true, signError = null) }
    }

    fun onCloseSignSheet() = _state.update { it.copy(signSheetOpen = false) }

    /** Called with the signature PNG once the sheet accepted it (signature + consent). */
    fun onSigned(png: ByteArray, signedWith: String) {
        if (_state.value.isSigning) return
        _state.update { it.copy(isSigning = true, signError = null) }
        viewModelScope.launch {
            InvexBackend.admitCard.signAndLoadCard(png, signedWith)
                .onSuccess { card ->
                    _state.update { it.copy(isSigning = false, signSheetOpen = false, card = card, step = AdmitStep.Card) }
                }
                .onFailure { e -> _state.update { it.copy(isSigning = false, signError = e.message) } }
        }
    }

    fun onDismissSignError() = _state.update { it.copy(signError = null) }

    // ---- Report
    fun onOpenReport() = _state.update { it.copy(reportOpen = true, reportError = null) }
    fun onCloseReport() = _state.update { it.copy(reportOpen = false, reportText = "", reportError = null) }
    fun onReportTextChange(text: String) = _state.update { it.copy(reportText = text.take(1000)) }
    fun onDismissReportSent() = _state.update { it.copy(reportSent = false) }

    fun onSendReport() {
        val s = _state.value
        if (s.isReporting || s.reportText.isBlank()) return
        _state.update { it.copy(isReporting = true, reportError = null) }
        viewModelScope.launch {
            InvexBackend.admitCard.report(s.reportText)
                .onSuccess { _state.update { it.copy(isReporting = false, reportOpen = false, reportText = "", reportSent = true) } }
                .onFailure { e -> _state.update { it.copy(isReporting = false, reportError = e.message) } }
        }
    }

    // ---- Verify the card
    fun onVerifyCard() {
        val code = _state.value.card?.qrCode
        if (_state.value.isVerifying) return
        if (code == null) {
            _state.update { it.copy(verifyError = "You have no upcoming exam, so there is no code to verify.") }
            return
        }
        _state.update { it.copy(isVerifying = true, verifyError = null, verifiedCard = null) }
        viewModelScope.launch {
            InvexBackend.admitCard.verifyCard(code)
                .onSuccess { v -> _state.update { it.copy(isVerifying = false, verifiedCard = v) } }
                .onFailure { e -> _state.update { it.copy(isVerifying = false, verifyError = e.message) } }
        }
    }

    fun onDismissVerify() = _state.update { it.copy(verifiedCard = null, verifyError = null) }

    // ---- Download
    fun onDownloadMessage(message: String?) = _state.update { it.copy(downloadMessage = message) }
}
