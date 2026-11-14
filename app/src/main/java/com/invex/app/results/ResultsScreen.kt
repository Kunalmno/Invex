package com.invex.app.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.R
import com.invex.app.ui.components.ServiceHeader
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.theme.InvexAccent
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.MobileBackground

@Composable
fun ResultsRoute(
    onBack: () -> Unit,
    viewModel: ResultsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    ResultsScreen(
        state = state,
        onBack = onBack,
        onDropdownToggle = viewModel::onDropdownToggle,
        onSemesterSelected = viewModel::onSemesterSelected,
        onRetry = viewModel::refresh,
    )
}

@Composable
fun ResultsScreen(
    state: ResultsUiState,
    onBack: () -> Unit,
    onDropdownToggle: (Boolean) -> Unit,
    onSemesterSelected: (Int) -> Unit,
    onRetry: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ServiceHeader(title = "Results", iconRes = R.drawable.ic_results, onBack = onBack)
        when {
            state.isLoading -> StatusMessage("Loading your results...")
            state.error != null -> StatusMessage(state.error, actionLabel = "Try again", onAction = onRetry)
            state.semesters.isEmpty() -> ResultsEmpty()
            else -> {
                SemesterDropdown(
                    semesters = state.semesters,
                    selected = state.selected,
                    open = state.dropdownOpen,
                    onToggle = onDropdownToggle,
                    onSelect = onSemesterSelected,
                )
                state.selected?.let { semester ->
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SemesterHeading(semester.heading)
                        ResultsTableHeader()
                        Column {
                            semester.subjects.forEach { ResultRow(it) }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ResultsPreview() {
    InvexAccent.setForRole("student")
    InvexTheme {
        ResultsScreen(
            state = ResultsUiState(
                semesters = listOf(
                    SemesterResult(1, List(5) { SubjectResult("Subject ${it + 1}", "CS10${it + 1}", 9.4) }),
                ),
                selectedSemester = 1,
                isLoading = false,
            ),
            onBack = {}, onDropdownToggle = {}, onSemesterSelected = {},
        )
    }
}
