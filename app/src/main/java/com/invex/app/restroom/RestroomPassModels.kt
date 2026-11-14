package com.invex.app.restroom

import com.invex.app.supplement.RosterStudent

/** A pass lasts 15 minutes (the "15 Minutes" in the design). After that it counts as overdue. */
const val PASS_MINUTES = 15

/** Summary counts shown at the top of the Restroom Pass screen. */
data class PassSummary(
    /** Students out now, within their 15 minutes. */
    val out: Int = 0,
    /** Passes that came back. */
    val returned: Int = 0,
    /** Still out past 15 minutes, or never came back before the exam ended. */
    val overdue: Int = 0,
)

/** A pass that is out right now, displayed as a ticket card. */
data class ActivePass(
    val id: String = "",
    val studentName: String = "John Doe",
    val seat: String = "R2 S3",
    val regNo: String = "7890",
    val course: String = "B. tech",
    val leftHallAt: String = "09:00",
    val elapsedAt: String = "09:15",
    val durationLabel: String = "$PASS_MINUTES Minutes",
    val remainingSeconds: Long = PASS_MINUTES * 60L,
    val validUntil: String = "Valid Until 01 Jan 27 09:15:00",
    val serialNumber: String = "No . 1234567890",
    val isActive: Boolean = true,
) {
    val isOverdue: Boolean get() = remainingSeconds <= 0

    /** Formats remaining seconds as "15 minutes 00 second" matching Figma mockup. */
    val remainingFormatted: String
        get() {
            val minutes = remainingSeconds / 60
            val seconds = remainingSeconds % 60
            return "%d minutes %02d second".format(minutes, seconds)
        }
}

/** Historical item displayed in the "Recent Pass" feed. */
data class RecentPassItem(
    val id: String,
    val studentName: String = "Lorem Ipsum",
    val status: String = "Returned",
    val validUntil: String = "Valid Until 01 Jan 26",
    val duration: String = "5 min",
    val timeRange: String = "09:00 AM - 09:15 AM",
    val isReturned: Boolean = true, // true = ic_returned (check), false = ic_notreturned (alert)
)

/** Full UI state for the Restroom Pass screen. */
data class RestroomUiState(
    val summary: PassSummary = PassSummary(),
    /** Every pass that is out right now (the design shows one card; more students out means more cards). */
    val activePasses: List<ActivePass> = emptyList(),
    val recentPasses: List<RecentPassItem> = emptyList(),
    /** Students who can be given a pass: everyone in the exam who is not out right now. */
    val students: List<RosterStudent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val issueSheetOpen: Boolean = false,
    val issueQuery: String = "",
    /** True while a pass is being issued, returned or reported, so a double tap does nothing twice. */
    val isBusy: Boolean = false,
    /** The pass the "Report Student" dialog is about. Null = dialog closed. */
    val reportPass: ActivePass? = null,
    val toastMessage: String? = null,
) {
    val showReportDialog: Boolean get() = reportPass != null
}
