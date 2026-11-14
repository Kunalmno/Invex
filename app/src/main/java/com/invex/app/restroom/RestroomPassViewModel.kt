package com.invex.app.restroom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.restroom.PassRecord
import com.invex.app.data.restroom.RestroomData
import com.invex.app.data.restroom.RestroomResult
import com.invex.app.supplement.RosterStudent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The exam (session) id comes from the route "restroom_pass/{sessionId}". */
class RestroomPassViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val sessionId: String = savedStateHandle.get<String>("sessionId").orEmpty()

    private val _state = MutableStateFlow(RestroomUiState())
    val state: StateFlow<RestroomUiState> = _state.asStateFlow()

    private var passes: List<PassRecord> = emptyList()
    private var allStudents: List<RosterStudent> = emptyList()
    private var hasLoaded = false
    private var timerJob: Job? = null

    init {
        // Every second: recompute the countdowns from the loaded passes (no network).
        // Every 30 seconds: ask the server again, in case another teacher issued or returned a pass.
        timerJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                if (seconds % REFRESH_EVERY_SECONDS == 0) load(showError = !hasLoaded) else apply()
                delay(1000L)
                seconds++
            }
        }
    }

    private suspend fun load(showError: Boolean) {
        when (val result = InvexBackend.restroom.load(sessionId)) {
            is RestroomResult.Success -> {
                passes = result.data.passes
                allStudents = result.data.students
                hasLoaded = true
                apply()
            }
            is RestroomResult.Failure -> if (showError) {
                _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun refresh() {
        _state.update { it.copy(isLoading = !hasLoaded, error = null) }
        viewModelScope.launch { load(showError = true) }
    }

    /** Turns the loaded passes into what the screen shows for "now". */
    private fun apply() {
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val running = passes.filter { it.status == "running" }
        val active = running.map { it.toActive(now, zone) }
        val finished = passes.filter { it.status != "running" }.sortedByDescending { it.issuedAt }
        val seatsOut = running.map { it.seatId }.toSet()
        _state.update {
            it.copy(
                summary = PassSummary(
                    out = active.count { a -> !a.isOverdue },
                    returned = finished.count { p -> p.status == "returned" },
                    overdue = active.count { a -> a.isOverdue } + finished.count { p -> p.status == "not_returned" },
                ),
                activePasses = active,
                recentPasses = finished.take(RECENT_LIMIT).map { p -> p.toRecent(zone) },
                students = allStudents.filter { s -> s.id !in seatsOut },
                isLoading = false,
                error = null,
            )
        }
    }

    // ── Issue a pass ────────────────────────────────────────────────────────────

    fun onIssuePassClick() = _state.update { it.copy(issueSheetOpen = true, issueQuery = "") }

    fun onIssueQueryChange(query: String) = _state.update { it.copy(issueQuery = query) }

    fun onDismissIssueSheet() = _state.update { it.copy(issueSheetOpen = false) }

    fun onIssueStudent(student: RosterStudent) = runAction(
        action = { InvexBackend.restroom.issue(student.id) },
        onDone = { _state.update { s -> s.copy(issueSheetOpen = false) } },
    )

    // ── Return and report ───────────────────────────────────────────────────────

    fun onMarkReturned(passId: String) = runAction(
        action = { InvexBackend.restroom.markReturned(passId) },
        onDone = { _state.update { s -> s.copy(toastMessage = "Pass marked as returned") } },
    )

    fun onReportClick(pass: ActivePass) = _state.update { it.copy(reportPass = pass) }

    fun onDismissReportDialog() = _state.update { it.copy(reportPass = null) }

    /** "Report to Runner": raises a runner request for this exam's room. */
    fun onConfirmReport(reason: String) {
        _state.update { it.copy(reportPass = null) }
        runAction(
            action = { InvexBackend.restroom.reportToRunner(sessionId) },
            onDone = { _state.update { s -> s.copy(toastMessage = "The runner has been notified") } },
            reload = false,
        )
    }

    fun onInfoClick() {
        _state.update { it.copy(toastMessage = "Pass validity: $PASS_MINUTES-minute standard hall leave") }
    }

    fun onDownloadClick() {
        _state.update { it.copy(toastMessage = "Downloading the pass slip is not available yet") }
    }

    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }

    /** Runs a database action once at a time. A returned message is shown as a toast; null means it worked. */
    private fun runAction(action: suspend () -> String?, onDone: () -> Unit, reload: Boolean = true) {
        if (_state.value.isBusy) return
        _state.update { it.copy(isBusy = true) }
        viewModelScope.launch {
            val error = action()
            _state.update { it.copy(isBusy = false, toastMessage = error ?: it.toastMessage) }
            if (error == null) {
                onDone()
                if (reload) load(showError = false)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    // ── Formatting ──────────────────────────────────────────────────────────────

    private fun PassRecord.toActive(now: Instant, zone: ZoneId): ActivePass {
        val elapsed = Duration.between(issuedAt, now).seconds
        val validUntil = issuedAt.plusSeconds(PASS_MINUTES * 60L)
        return ActivePass(
            id = id,
            studentName = studentName,
            seat = seat,
            regNo = regNo,
            course = course,
            leftHallAt = issuedAt.atZone(zone).format(CLOCK),
            elapsedAt = validUntil.atZone(zone).format(CLOCK),
            remainingSeconds = (PASS_MINUTES * 60L - elapsed).coerceAtLeast(0),
            validUntil = "Valid Until " + validUntil.atZone(zone).format(LONG_STAMP),
            serialNumber = "No . $serial",
        )
    }

    private fun PassRecord.toRecent(zone: ZoneId): RecentPassItem {
        val returned = status == "returned"
        val validUntil = issuedAt.plusSeconds(PASS_MINUTES * 60L)
        val minutes = returnedAt?.let { Duration.between(issuedAt, it).toMinutes().coerceAtLeast(1) }
        return RecentPassItem(
            id = id,
            studentName = studentName,
            status = if (returned) "Returned" else "Not returned",
            validUntil = "Valid Until " + validUntil.atZone(zone).format(DAY),
            duration = if (minutes != null) "$minutes min" else "-",
            timeRange = issuedAt.atZone(zone).format(CLOCK_AMPM) + " - " +
                (returnedAt?.atZone(zone)?.format(CLOCK_AMPM) ?: "--"),
            isReturned = returned,
        )
    }

    private companion object {
        const val REFRESH_EVERY_SECONDS = 30
        const val RECENT_LIMIT = 20
        val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val CLOCK_AMPM: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
        val DAY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yy", Locale.ENGLISH)
        val LONG_STAMP: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yy HH:mm:ss", Locale.ENGLISH)
    }
}
