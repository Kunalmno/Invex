package com.invex.app.verifyadmit

import com.invex.app.data.verify.VerifyResult

enum class VerifyTab(val label: String) {
    ScanQr("Scan QR"),
    Students("Students"),
}

data class VerifyAdmitUiState(
    val tab: VerifyTab = VerifyTab.ScanQr,
    /** True while the camera is looking for a QR code. Starts on, stops after the first code is read. */
    val scanning: Boolean = true,
    /** Raw text of the last QR code read, or null. */
    val lastScan: String? = null,
    /** True while the code is being checked with the backend. */
    val isVerifying: Boolean = false,
    /** Outcome of the last scan. Null before the first scan. Not in the Figma design yet. */
    val result: VerifyResult? = null,
)
