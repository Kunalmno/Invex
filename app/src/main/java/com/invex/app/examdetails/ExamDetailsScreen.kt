package com.invex.app.examdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.theme.InvexAccent
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.MobileBackground

@Composable
fun ExamDetailsRoute(
    onBack: () -> Unit,
    onProceed: () -> Unit,
    onService: (ExamService) -> Unit,
    viewModel: ExamDetailsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    ExamDetailsScreen(state, onBack = onBack, onProceed = onProceed, onService = onService, onRetry = viewModel::refresh)
}

/**
 * Header and the CANCEL / PROCEED bar stay in place; the middle (courses, room, services) scrolls
 * on screens shorter than the 1012dp design.
 */
@Composable
fun ExamDetailsScreen(
    state: ExamDetailsUiState,
    onBack: () -> Unit,
    onProceed: () -> Unit,
    onService: (ExamService) -> Unit,
    onRetry: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 24.dp, bottom = 16.dp),
    ) {
        ExamDetailsHeader(onBack = onBack, modifier = Modifier.padding(horizontal = 16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExamSectionHeading("Courses Details")
                when {
                    state.isLoading -> StatusMessage("Loading exam details...")
                    state.error != null -> StatusMessage(state.error, actionLabel = "Try again", onAction = onRetry)
                    InvexAccent.isStudent -> {
                        // Student: where and when, not the course list.
                        ExamInfoCard("Building", state.building.orEmpty())
                        ExamInfoCard("Date", state.date.orEmpty())
                        ExamInfoCard("Time", state.time.orEmpty())
                    }
                    state.courses.isEmpty() -> StatusMessage("No courses in this exam yet")
                    else -> state.courses.forEach { CourseCard(it) }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExamSectionHeading("Room")
                RoomIllustration()
                state.room?.let { RoomInfoRow(it) }
            }
            if (!InvexAccent.isStudent) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ExamSectionHeading("Services")
                    ServicesRow(onService)
                }
            }
        }
        ExamActionButtons(
            onCancel = onBack,
            onProceed = onProceed,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1012)
@Composable
private fun ExamDetailsPreview() {
    InvexTheme {
        ExamDetailsScreen(
            ExamDetailsUiState(
                courses = listOf(
                    ExamCourse("1", "CS101", "Lorem Ipsum Dorem", 10),
                    ExamCourse("2", "CS102", "Lorem Ipsum Dorem", 10),
                ),
                room = RoomInfo(block = "A", room = "317", division = "B"),
                isLoading = false,
            ),
            onBack = {}, onProceed = {}, onService = {})
    }
}
