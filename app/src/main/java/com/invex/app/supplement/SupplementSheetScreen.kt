package com.invex.app.supplement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.R
import com.invex.app.sign.SignRequestActions
import com.invex.app.sign.SignRequestSheet
import com.invex.app.sign.SignRequestViewModel
import com.invex.app.ui.components.InvexSearchBar
import com.invex.app.ui.components.ScreenHeader
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.components.StudentRow
import com.invex.app.ui.components.sectionGap
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SectionHeadingColor

/** Stateful entry point: owns both ViewModels and the sign sheet that "Issue a Supplementary Sheet" opens. */
@Composable
fun SupplementSheetRoute(
    onBack: () -> Unit,
    viewModel: SupplementViewModel = viewModel(),
    signViewModel: SignRequestViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val signState by signViewModel.state.collectAsState()
    var sheetOpen by rememberSaveable { mutableStateOf(false) }

    SupplementSheetScreen(
        state = state,
        onBack = onBack,
        onIssueSheet = {
            signViewModel.reset()
            sheetOpen = true
        },
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::refresh,
    )

    state.issueError?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::onDismissIssueError,
            text = { Text(message, style = InvexType.Body16) },
            confirmButton = {
                TextButton(onClick = viewModel::onDismissIssueError) { Text("OK", color = Orange) }
            },
        )
    }

    if (sheetOpen) {
        SignRequestSheet(
            state = signState,
            roster = state.roster,
            issued = state.allStudents,
            actions = SignRequestActions(
                onQueryChange = signViewModel::onQueryChange,
                onSelectStudent = signViewModel::onSelectStudent,
                onTabChange = signViewModel::onTabChange,
                onStrokeStart = signViewModel::onStrokeStart,
                onStrokeMove = signViewModel::onStrokeMove,
                onBack = signViewModel::onBack,
                onClear = signViewModel::onClear,
                onTypedNameChange = signViewModel::onTypedNameChange,
                onSaveSignature = signViewModel::onSaveSignature,
                onConsentChange = signViewModel::onConsentChange,
                onCancel = { sheetOpen = false },
                onSign = {
                    // A repeat request for a listed student increments their number (see issueSheet).
                    // The sheet closes only when the database accepted it.
                    signViewModel.submit()?.let { studentId ->
                        viewModel.issueSheet(studentId) { issued -> if (issued) sheetOpen = false }
                    }
                },
            ),
        )
    }
}

/** Stateless screen: easy to preview and test. */
@Composable
fun SupplementSheetScreen(
    state: SupplementUiState,
    onBack: () -> Unit,
    onIssueSheet: () -> Unit,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding(),
        // The Figma frame uses 60dp top padding including its fake status bar,
        // so we use the real status bar inset plus a small gap instead.
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        // 12dp between items; sectionGap() makes it 24dp between sections (see InvexComponents.kt).
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Supplements",
                glyphRes = R.drawable.ic_supplement_sheet,
                glyphPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                onBack = onBack,
            )
        }
        sectionGap()
        item { StatusGrid(state.summary) }
        sectionGap()
        item { IssueSheetButton(onClick = onIssueSheet) }
        sectionGap()
        item { Text("Sheets Issued", style = InvexType.SectionHeading, color = SectionHeadingColor) }
        item { InvexSearchBar(query = state.query, onQueryChange = onQueryChange) }
        if (state.isLoading) {
            item { StatusMessage("Loading supplement sheets...") }
        } else if (state.error != null) {
            item { StatusMessage(state.error, actionLabel = "Try again", onAction = onRetry) }
        } else if (state.visibleStudents.isEmpty()) {
            item { StatusMessage("No sheets issued yet") }
        }
        itemsIndexed(state.visibleStudents, key = { _, s -> s.id }) { index, student ->
            StudentRow(
                name = student.name,
                meta = student.meta,
                count = student.sheetCount,
                tag = student.status.label,
                // 12dp item spacing + 4dp = the 16dp gap between the search bar and the first row.
                modifier = if (index == 0) Modifier.padding(top = 4.dp) else Modifier,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SupplementSheetPreview() {
    InvexTheme {
        SupplementSheetScreen(
            state = SupplementUiState(
                summary = SheetSummary(issued = 3, awaitingReturn = 2, returned = 1, inStock = 7),
                allStudents = List(3) { i ->
                    StudentSheet(
                        id = "s$i",
                        name = "Lorem Ipsum Dorem",
                        course = "CS101",
                        seat = "Row ${i + 1}, Seat 1",
                        rollNumber = "STU00$i",
                        sheetCount = 1,
                        status = if (i == 0) SheetStatus.RESOLVED else SheetStatus.PENDING,
                    )
                },
                isLoading = false,
            ),
            onBack = {},
            onIssueSheet = {},
            onQueryChange = {},
        )
    }
}
