package com.invex.app.sign

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.app.supplement.RosterStudent
import com.invex.app.supplement.SheetStatus
import com.invex.app.supplement.StudentSheet
import com.invex.app.ui.components.InvexSearchBar
import com.invex.app.ui.components.StudentRow
import com.invex.app.ui.theme.ComboText
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SectionHeadingColor
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText

/**
 * The "Sign the Request" bottom sheet that opens from "Issue a Supplementary Sheet".
 * [roster] is everyone who can be searched; [issued] gives each student's current sheet count.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignRequestSheet(
    state: SignRequestUiState,
    roster: List<RosterStudent>,
    issued: List<StudentSheet>,
    actions: SignRequestActions,
) {
    ModalBottomSheet(
        onDismissRequest = actions.onCancel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SoftWhite,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        SignRequestContent(state, roster, issued, actions)
    }
}

@Composable
fun SignRequestContent(
    state: SignRequestUiState,
    roster: List<RosterStudent>,
    issued: List<StudentSheet>,
    actions: SignRequestActions,
    modifier: Modifier = Modifier,
) {
    val selected = roster.firstOrNull { it.id == state.selectedStudentId }
    val matches = if (state.query.isBlank()) emptyList() else roster.filter {
        it.name.contains(state.query, ignoreCase = true) ||
            it.rollNumber.contains(state.query, ignoreCase = true) ||
            it.course.contains(state.query, ignoreCase = true)
    }.take(3)
    val shown = if (selected != null) listOf(selected) else matches

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UnderlinedText(
            text = "Supplement Sheets",
            style = InvexType.SheetHeading,
            color = ComboText,
            underlineColor = Orange,
            thickness = 4.dp,
            gap = 4.dp,
        )

        InvexSearchBar(query = state.query, onQueryChange = actions.onQueryChange)

        shown.forEach { student ->
            val sheet = issued.firstOrNull { it.id == student.id }
            StudentRow(
                name = student.name,
                meta = student.meta,
                count = sheet?.sheetCount ?: 0,
                tag = sheet?.status?.label ?: "Not issued",
                onClick = { actions.onSelectStudent(student.id) },
            )
        }
        if (state.query.isNotBlank() && selected == null && matches.isEmpty()) {
            Text("No students found", style = InvexType.SubLabel, color = TableHeaderText)
        }

        Text("Sign the Request", style = InvexType.SignTitle, color = SectionHeadingColor)
        SignTabs(selected = state.tab, onSelect = actions.onTabChange)

        SignatureBox(
            tab = state.tab,
            strokes = state.strokes,
            typedName = state.typedName,
            isSaved = state.isSignatureSaved,
            onStrokeStart = actions.onStrokeStart,
            onStrokeMove = actions.onStrokeMove,
            onTypedNameChange = actions.onTypedNameChange,
            onBack = actions.onBack,
            onClear = actions.onClear,
            onSave = actions.onSaveSignature,
        )

        Text(
            text = "This Supplement Sheet will be signed by and sent to subsequent signee.",
            style = InvexType.SubLabel,
            color = TableHeaderText,
        )

        ConsentCard(
            consent = state.consent,
            onConsentChange = actions.onConsentChange,
            warning = state.warning,
        )

        SheetButtons(onCancel = actions.onCancel, onSign = actions.onSign)
    }
}

@Preview(widthDp = 390, heightDp = 760)
@Composable
private fun SignRequestContentPreview() {
    InvexTheme {
        Box(Modifier.background(SoftWhite)) {
            SignRequestContent(
                state = SignRequestUiState(selectedStudentId = "s0", warning = SignWarning.NO_CONSENT),
                roster = listOf(RosterStudent("s0", "Lorem Ipsum Dorem", "CS101", "Row 1, Seat 1", "STU001")),
                issued = listOf(
                    StudentSheet("s0", "Lorem Ipsum Dorem", "CS101", "Row 1, Seat 1", "STU001", 1, SheetStatus.RESOLVED),
                ),
                actions = SignRequestActions(),
            )
        }
    }
}
