package com.invex.app.duties

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.invex.app.ui.theme.InvexAccent
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.TableHeaderText

@Composable
fun DutiesRoute(
    onBack: () -> Unit,
    onViewRoomSeating: (DutyItem) -> Unit,
    onNavTab: (DashboardNavTab) -> Unit,
    viewModel: DutiesViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    DutiesScreen(
        state = state,
        onBack = onBack,
        onTabSelected = viewModel::onTabSelected,
        onRetry = viewModel::refresh,
        onViewRoomSeating = onViewRoomSeating,
        onNavTab = onNavTab,
    )
}

/**
 * Header and tabs stay put; the cards scroll under them. The floating bottom bar sits over the list,
 * so the list has 80 + 16 + 16 of bottom padding to let the last card scroll clear of it.
 */
@Composable
fun DutiesScreen(
    state: DutiesUiState,
    onBack: () -> Unit,
    onTabSelected: (DutiesTab) -> Unit,
    onViewRoomSeating: (DutyItem) -> Unit,
    onNavTab: (DashboardNavTab) -> Unit,
    onRetry: () -> Unit = {},
) {
    Box(Modifier.fillMaxSize().background(MobileBackground)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            DutiesHeader(onBack = onBack, modifier = Modifier.padding(horizontal = 16.dp))
            DutiesTabRow(
                selected = state.selectedTab,
                onTabSelected = onTabSelected,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            val duties = state.visibleDuties
            if (state.isLoading) {
                DutiesMessage(if (InvexAccent.isStudent) "Loading your exams..." else "Loading your duties...")
            } else if (state.error != null) {
                DutiesMessage(state.error, actionLabel = "Try again", onAction = onRetry)
            } else if (duties.isEmpty()) {
                DutiesEmptyState(state.selectedTab)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    items(duties, key = { it.id }) { duty ->
                        DutyCard(duty = duty, onViewRoomSeating = onViewRoomSeating)
                    }
                }
            }
        }
        FloatingBottomNavBar(
            selectedTab = DashboardNavTab.DUTIES,
            onTabSelected = onNavTab,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 928)
@Composable
private fun DutiesScreenPreview() {
    InvexTheme {
        DutiesScreen(
            state = DutiesUiState(
                upcomingDuties = listOf(
                    DutyItem(id = "1"),
                    DutyItem(id = "2"),
                ),
                isLoading = false,
            ),
            onBack = {},
            onTabSelected = {},
            onViewRoomSeating = {},
            onNavTab = {},
        )
    }
}

/** Centered message for loading and load errors, with an optional orange action ("Try again"). */
@Composable
private fun DutiesMessage(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = text, style = InvexType.Body16, color = TableHeaderText)
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = InvexType.Body16,
                color = Orange,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}
