package com.invex.app.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.R
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
import com.invex.app.ui.theme.TableHeaderText

/** Stateful entry point: owns the ViewModel. */
@Composable
fun AttendanceRoute(
    onBack: () -> Unit,
    viewModel: AttendanceViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    AttendanceScreen(
        state = state,
        onBack = onBack,
        onCourseSelected = viewModel::onCourseSelected,
        onQueryChange = viewModel::onQueryChange,
        onStudentClick = viewModel::onStudentClick,
        onRetry = viewModel::refresh,
    )
    AttendanceDialogs(
        state = state,
        onMark = viewModel::onMark,
        onDismissMark = viewModel::onDismissMark,
        onDismissSaveError = viewModel::onDismissSaveError,
    )
}

/** Stateless screen: easy to preview and test. */
@Composable
fun AttendanceScreen(
    state: AttendanceUiState,
    onBack: () -> Unit,
    onCourseSelected: (String) -> Unit,
    onQueryChange: (String) -> Unit,
    onStudentClick: (AttendanceStudent) -> Unit = {},
    onRetry: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        // 12dp between items; sectionGap() makes it 24dp between sections.
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ScreenHeader(
                title = "Attendance",
                glyphRes = R.drawable.ic_attendance,
                glyphPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                onBack = onBack,
            )
        }
        sectionGap()
        item {
            CourseSelector(
                selected = state.selectedCourse,
                courses = state.courses,
                onSelect = onCourseSelected,
            )
        }
        item { AttendanceStatusRow(state.summary) }
        sectionGap()
        item { Text("Analytics", style = InvexType.SectionHeadingBold, color = SectionHeadingColor) }
        item { AnalyticsCard(slices = state.chart, summary = state.summary) }
        sectionGap()
        item { Text("Students", style = InvexType.SectionHeading, color = SectionHeadingColor) }
        item { InvexSearchBar(query = state.query, onQueryChange = onQueryChange) }
        if (state.isLoading) {
            item { StatusMessage("Loading attendance...") }
        } else if (state.error != null) {
            item { StatusMessage(state.error, actionLabel = "Try again", onAction = onRetry) }
        } else if (state.visibleStudents.isEmpty()) {
            item { StatusMessage("No students found") }
        }
        itemsIndexed(state.visibleStudents, key = { _, s -> s.id }) { index, student ->
            StudentRow(
                name = student.name,
                meta = student.meta,
                count = null, // a count means nothing for attendance
                tag = student.mark.label,
                onClick = { onStudentClick(student) },
                // 12dp item spacing + 4dp = the 16dp gap between the search bar and the first row.
                modifier = if (index == 0) Modifier.padding(top = 4.dp) else Modifier,
            )
        }
    }
}

/**
 * Manual attendance: tap a student to mark present or absent. Not in the Figma design yet.
 * Shown over the screen; the LazyColumn above stays as designed.
 */
@Composable
fun AttendanceDialogs(
    state: AttendanceUiState,
    onMark: (present: Boolean) -> Unit,
    onDismissMark: () -> Unit,
    onDismissSaveError: () -> Unit,
) {
    state.studentToMark?.let { student ->
        AlertDialog(
            onDismissRequest = onDismissMark,
            title = { Text(student.name, style = InvexType.Body16) },
            text = { Text("${student.meta}\nNow: ${student.mark.label}", style = InvexType.RowMeta) },
            confirmButton = {
                Row { // Present / Absent side by side
                    TextButton(onClick = { onMark(false) }) { Text("Absent", color = TableHeaderText) }
                    TextButton(onClick = { onMark(true) }) { Text("Present", color = Orange) }
                }
            },
            dismissButton = { TextButton(onClick = onDismissMark) { Text("Cancel", color = TableHeaderText) } },
        )
    }
    state.saveError?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissSaveError,
            text = { Text(message, style = InvexType.Body16) },
            confirmButton = { TextButton(onClick = onDismissSaveError) { Text("OK", color = Orange) } },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1086)
@Composable
private fun AttendanceScreenPreview() {
    InvexTheme {
        AttendanceScreen(
            state = AttendanceUiState(
                courses = listOf(ALL_COURSES, "CS101"),
                allStudents = List(3) { i ->
                    AttendanceStudent(
                        id = "s$i",
                        name = "Lorem Ipsum Dorem",
                        course = "CS101",
                        rollNumber = "STU00$i",
                        seat = "Row ${i + 1}, Seat 1",
                        mark = AttendanceMark.entries[i],
                    )
                },
                isLoading = false,
            ),
            onBack = {},
            onCourseSelected = {},
            onQueryChange = {},
        )
    }
}
