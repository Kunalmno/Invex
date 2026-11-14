package com.invex.app.restroom

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.Orange

/**
 * Stateful container for RestroomPassScreen. Owns and drives the [RestroomPassViewModel]
 * (the exam id comes from the route "restroom_pass/{sessionId}").
 */
@Composable
fun RestroomPassRoute(
    onBack: () -> Unit,
    viewModel: RestroomPassViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    RestroomPassScreen(
        state = state,
        onBack = onBack,
        onIssuePassClick = viewModel::onIssuePassClick,
        onReportClick = viewModel::onReportClick,
        onInfoClick = viewModel::onInfoClick,
        onDownloadClick = viewModel::onDownloadClick,
        onDismissReportDialog = viewModel::onDismissReportDialog,
        onConfirmReport = viewModel::onConfirmReport,
        onMarkReturned = viewModel::onMarkReturned,
        onRetry = viewModel::refresh,
    )

    if (state.issueSheetOpen) {
        IssuePassSheet(
            students = state.students,
            query = state.issueQuery,
            onQueryChange = viewModel::onIssueQueryChange,
            onPick = viewModel::onIssueStudent,
            onDismiss = viewModel::onDismissIssueSheet,
        )
    }
}

/**
 * Stateless Restroom Pass management screen displaying summary indicators,
 * issuance action, active pass ticket cards with a live countdown, and recent pass history.
 */
@Composable
fun RestroomPassScreen(
    state: RestroomUiState,
    onBack: () -> Unit,
    onIssuePassClick: () -> Unit,
    onReportClick: (ActivePass) -> Unit,
    onInfoClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDismissReportDialog: () -> Unit,
    onConfirmReport: (String) -> Unit,
    modifier: Modifier = Modifier,
    onMarkReturned: (String) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // 1. Screen Header
            item(key = "header") {
                RestroomHeader(onBack = onBack)
            }

            // 2. Summary Status Cards Row (Out, Returned, Overdue)
            item(key = "status_row") {
                PassesStatusRow(summary = state.summary)
            }

            // 3. Main Action CTA Button (the Figma text says "Supplementary Sheet", a copy mistake)
            item(key = "issue_button") {
                IssuePassButton(
                    onClick = onIssuePassClick,
                    label = "Issue a Restroom Pass",
                )
            }

            if (state.isLoading) {
                item(key = "loading") { StatusMessage("Loading restroom passes...") }
            } else if (state.error != null) {
                item(key = "error") { StatusMessage(state.error, actionLabel = "Try again", onAction = onRetry) }
            } else {
                // 4. Active Pass Section
                item(key = "active_pass_heading") {
                    Text(
                        text = "Active Pass",
                        style = InvexType.SectionHeadingBold,
                        color = Color.Black,
                    )
                }

                if (state.activePasses.isEmpty()) {
                    item(key = "active_pass_empty") { StatusMessage("No student is out right now") }
                }
                items(items = state.activePasses, key = { "active_${it.id}" }) { pass ->
                    androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ActivePassCard(
                            pass = pass,
                            onReport = { onReportClick(pass) },
                            onInfo = onInfoClick,
                            onDownload = onDownloadClick,
                        )
                        // Not in the Figma design yet: the teacher closes the pass when the student is back.
                        Text(
                            text = "Mark as returned",
                            style = InvexType.Body16,
                            color = Orange,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onMarkReturned(pass.id) }
                                .padding(vertical = 8.dp),
                        )
                    }
                }

                // 5. Recent Pass Section
                item(key = "recent_pass_heading") {
                    Text(
                        text = "Recent Pass",
                        style = InvexType.SectionHeadingBold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                if (state.recentPasses.isEmpty()) {
                    item(key = "recent_pass_empty") { StatusMessage("No past passes yet") }
                }
                items(
                    items = state.recentPasses,
                    key = { it.id },
                ) { passItem ->
                    RecentPassCard(item = passItem)
                }
            }

            item(key = "bottom_spacer") {
                Spacer(Modifier.height(16.dp))
            }
        }

        // Report confirmation dialog
        state.reportPass?.let { pass ->
            ReportStudentDialog(
                studentName = pass.studentName,
                onConfirm = onConfirmReport,
                onDismiss = onDismissReportDialog,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 920)
@Composable
fun RestroomPassScreenPreview() {
    InvexTheme {
        RestroomPassScreen(
            state = RestroomUiState(
                summary = PassSummary(out = 1, returned = 15, overdue = 1),
                activePasses = listOf(ActivePass(id = "1")),
                recentPasses = listOf(
                    RecentPassItem(id = "1"),
                    RecentPassItem(id = "2", status = "Not returned", isReturned = false),
                ),
                isLoading = false,
            ),
            onBack = {},
            onIssuePassClick = {},
            onReportClick = {},
            onInfoClick = {},
            onDownloadClick = {},
            onDismissReportDialog = {},
            onConfirmReport = {},
        )
    }
}
