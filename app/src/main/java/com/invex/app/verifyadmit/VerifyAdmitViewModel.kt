package com.invex.app.verifyadmit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class VerifyAdmitViewModel : ViewModel() {

    private val _state = MutableStateFlow(VerifyAdmitUiState())
    val state: StateFlow<VerifyAdmitUiState> = _state.asStateFlow()

    fun onTabSelected(tab: VerifyTab) = _state.update { it.copy(tab = tab) }

    /** Camera found a QR code: stop scanning so one code is not handled many times, then verify it. */
    fun onQrScanned(value: String) {
        if (!_state.value.scanning) return
        _state.update { it.copy(scanning = false, lastScan = value, isVerifying = true, result = null) }
        viewModelScope.launch {
            val result = InvexBackend.verifyAdmit.verify(value)
            _state.update { it.copy(isVerifying = false, result = result) }
        }
    }

    /** "Scan the Student" button: look for the next code. */
    fun onScanStudent() = _state.update {
        it.copy(tab = VerifyTab.ScanQr, scanning = true, lastScan = null, isVerifying = false, result = null)
    }
}
