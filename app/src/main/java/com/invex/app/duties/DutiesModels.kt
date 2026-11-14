package com.invex.app.duties

// ── Tab ───────────────────────────────────────────────────────────────────────

enum class DutiesTab { Upcoming, Completed }

// ── Duty item ─────────────────────────────────────────────────────────────────

/**
 * A single examination duty assignment shown in the list.
 *
 * @param id         Unique identifier (the exam session id; used as LazyColumn key).
 * @param building   Hall / building name, e.g. "Examination Hall A".
 * @param date       Formatted date string, e.g. "21 Sep".
 * @param timeRange  Time window string, e.g. "11 AM - 01 PM".
 * @param isCompleted Whether this duty is in the Completed tab.
 */
data class DutyItem(
    val id: String,
    val building: String = "Examination Hall A",
    val date: String = "21 Sep",
    val timeRange: String = "11 AM - 01 PM",
    val isCompleted: Boolean = false,
)

// ── UI state ──────────────────────────────────────────────────────────────────

data class DutiesUiState(
    val selectedTab: DutiesTab = DutiesTab.Upcoming,
    val upcomingDuties: List<DutyItem> = emptyList(),
    val completedDuties: List<DutyItem> = emptyList(),
    /** True until the first answer from the server arrives. */
    val isLoading: Boolean = true,
    /** Set when the duties could not be loaded (no internet, logged out...). */
    val error: String? = null,
) {
    /** Returns the list that belongs to the currently selected tab. */
    val visibleDuties: List<DutyItem>
        get() = if (selectedTab == DutiesTab.Upcoming) upcomingDuties else completedDuties
}
