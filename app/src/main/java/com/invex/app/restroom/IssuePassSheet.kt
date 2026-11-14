package com.invex.app.restroom

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import com.invex.app.supplement.RosterStudent
import com.invex.app.ui.components.InvexSearchBar
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.components.StudentRow
import com.invex.app.ui.theme.ComboText
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText

/**
 * Pick the student who leaves the hall. Opens from "Issue a Restroom Pass". Not in the Figma design yet:
 * it reuses the search bar and student rows of the Supplement sign sheet. Students who already have a
 * pass running are left out, and tapping a student issues the pass.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssuePassSheet(
    students: List<RosterStudent>,
    query: String,
    onQueryChange: (String) -> Unit,
    onPick: (RosterStudent) -> Unit,
    onDismiss: () -> Unit,
) {
    val matches = if (query.isBlank()) emptyList() else students.filter {
        it.name.contains(query, ignoreCase = true) ||
            it.rollNumber.contains(query, ignoreCase = true) ||
            it.course.contains(query, ignoreCase = true)
    }.take(5)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SoftWhite,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LineUnderlinedText(
                text = "Restroom Pass",
                style = InvexType.SheetHeading,
                color = ComboText,
                underlineColor = Orange,
                underlineOffset = 4.dp,
                thickness = 4.dp,
            )
            InvexSearchBar(query = query, onQueryChange = onQueryChange)
            if (query.isBlank()) {
                Text("Search a student by name or registration number, then tap the student to issue the pass.", style = InvexType.SubLabel, color = TableHeaderText)
            }
            matches.forEach { student ->
                StudentRow(
                    name = student.name,
                    meta = student.meta,
                    count = null,
                    tag = "Issue",
                    onClick = { onPick(student) },
                )
            }
            if (query.isNotBlank() && matches.isEmpty()) {
                Text("No students found", style = InvexType.SubLabel, color = TableHeaderText)
            }
        }
    }
}
