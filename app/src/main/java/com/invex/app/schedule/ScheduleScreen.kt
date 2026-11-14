package com.invex.app.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.dashboard.DashboardNavTab
import com.invex.app.dashboard.FloatingBottomNavBar
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.MobileBackground
import java.time.LocalDate

@Composable
fun ScheduleRoute(
    onBack: () -> Unit,
    onNavTab: (DashboardNavTab) -> Unit,
    viewModel: ScheduleViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    ScheduleScreen(
        state = state,
        onBack = onBack,
        onDateSelected = viewModel::onDateSelected,
        onPreviousWeek = viewModel::onPreviousWeek,
        onNextWeek = viewModel::onNextWeek,
        onNavTab = onNavTab,
        onRetry = viewModel::refresh,
    )
}

/** Header and week strip stay fixed; the timeline scrolls under the floating bottom bar (112dp bottom space). */
@Composable
fun ScheduleScreen(
    state: ScheduleUiState,
    onBack: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onNavTab: (DashboardNavTab) -> Unit,
    onRetry: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize().background(MobileBackground)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ScheduleHeader(onBack = onBack, modifier = Modifier.padding(horizontal = 16.dp))
            WeekStrip(
                days = state.weekDays,
                selected = state.selectedDate,
                onSelect = onDateSelected,
                onPrevious = onPreviousWeek,
                onNext = onNextWeek,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                when {
                    state.isLoading -> StatusMessage("Loading your schedule...", Modifier.padding(horizontal = 16.dp))
                    state.error != null -> StatusMessage(state.error, Modifier.padding(horizontal = 16.dp), actionLabel = "Try again", onAction = onRetry)
                }
                DayTimeline(hours = state.hours, events = state.events)
                Spacer(Modifier.height(112.dp))
            }
        }
        FloatingBottomNavBar(
            selectedTab = DashboardNavTab.SCHEDULE,
            onTabSelected = onNavTab,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SchedulePreview() {
    InvexTheme {
        ScheduleScreen(ScheduleUiState(), onBack = {}, onDateSelected = {}, onPreviousWeek = {}, onNextWeek = {}, onNavTab = {})
    }
}
