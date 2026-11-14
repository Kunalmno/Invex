package com.invex.app.results

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.components.UpDownArrows
import com.invex.app.ui.components.dottedBorder
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.TableHeaderText

/** "Choose your Semester" and the dotted dropdown ("Semester one" with an up/down arrow). */
@Composable
fun SemesterDropdown(
    semesters: List<SemesterResult>,
    selected: SemesterResult?,
    open: Boolean,
    onToggle: (Boolean) -> Unit,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Choose your Semester", style = InvexType.ExamSmallLabel, color = Color.Black)
        Box {
            val shape = RoundedCornerShape(16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(shape)
                    .dottedBorder(16.dp)
                    .clickable { onToggle(true) }
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(selected?.label ?: "No semester", style = InvexType.Body18, color = Color.Black)
                UpDownArrows()
            }
            DropdownMenu(expanded = open, onDismissRequest = { onToggle(false) }) {
                semesters.forEach { s ->
                    DropdownMenuItem(
                        text = { Text(s.label, style = InvexType.Body16) },
                        onClick = { onSelect(s.semester) },
                    )
                }
            }
        }
    }
}

/** "Semester One" with a thin accent underline. */
@Composable
fun SemesterHeading(text: String, modifier: Modifier = Modifier) {
    LineUnderlinedText(
        text = text,
        style = InvexType.Body18,
        color = Color.Black,
        underlineColor = Orange,
        underlineOffset = 4.dp,
        thickness = 2.dp,
        modifier = modifier,
    )
}

/** "Subject" on the left, "Grading" on the right. */
@Composable
fun ResultsTableHeader(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Subject", style = InvexType.Body18Regular, color = Color.Black)
        Text("Grading", style = InvexType.Body18Regular, color = Color.Black)
    }
}

/** Subject name with the course code under it, the grade on the right and a black line below. */
@Composable
fun ResultRow(subject: SubjectResult, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(subject.name, style = InvexType.Body18, color = Color.Black)
                Text(subject.code, style = InvexType.Caption12, color = Color.Black)
            }
            Text(subject.gradeText, style = InvexType.Body18, color = Color.Black, modifier = Modifier.padding(start = 16.dp, end = 8.dp))
        }
        HorizontalDivider(thickness = 1.dp, color = Color.Black)
    }
}

@Composable
fun ResultsEmpty(modifier: Modifier = Modifier) {
    Text("No results have been published yet", style = InvexType.Body16, color = TableHeaderText, modifier = modifier.padding(vertical = 24.dp))
}
