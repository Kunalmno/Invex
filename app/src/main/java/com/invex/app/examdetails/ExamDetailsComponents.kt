package com.invex.app.examdetails

import com.invex.app.ui.theme.accentIcon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.PillGrey
import com.invex.app.ui.theme.SoftWhite

/** Chevron + orange exam icon (circle is part of the drawable) + "Exam Details" with an orange underline. */
@Composable
fun ExamDetailsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chevron_back),
            contentDescription = "Back",
            modifier = Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onBack),
        )
        Image(
            painter = painterResource(accentIcon(R.drawable.ic_exam_details)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(40.dp),
        )
        LineUnderlinedText(
            text = "Exam Details",
            style = InvexType.PageHeading,
            color = IconColor,
            underlineColor = Orange,
            underlineOffset = 4.dp,
            thickness = 4.dp,
        )
    }
}

@Composable
fun ExamSectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(text, style = InvexType.ExamSectionHeading, color = Color.Black, modifier = modifier)
}

/** 56dp light card: "Course I" + name on the left (with a small grey chip), "Students" + count on the right. */
@Composable
fun CourseCard(course: ExamCourse, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SoftWhite)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(course.label, style = InvexType.ExamSmallLabel, color = Color.Black)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(course.name, style = InvexType.Body16, color = Color.Black)
                // Small grey square in the design; its meaning is not known yet (placeholder).
                Box(Modifier.size(16.dp).background(PillGrey))
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Students", style = InvexType.ExamSmallLabel, color = Color.Black)
            Text(course.students.toString(), style = InvexType.Body16, color = Color.Black)
        }
    }
}

/** Student Exam Details: a 56dp light card with a small label and one value ("Building", "Date", "Time"). */
@Composable
fun ExamInfoCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SoftWhite)
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(label, style = InvexType.ExamSmallLabel, color = Color.Black)
        Text(value, style = InvexType.Body16, color = Color.Black)
    }
}

/** Isometric classroom drawing, centered. */
@Composable
fun RoomIllustration(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.ic_full_room),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(width = 184.dp, height = 180.dp),
        )
    }
}

/** "Block A    Room 317    Division B": small label with a big value, 48dp apart, centered. Division only when the room has one. */
@Composable
fun RoomInfoRow(room: RoomInfo, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
    ) {
        RoomStat("Block", room.block)
        RoomStat("Room", room.room)
        room.division?.let { RoomStat("Division", it) }
    }
}

@Composable
private fun RoomStat(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.Bottom) {
        Text(label, style = InvexType.ExamStatLabel, color = Color.Black, modifier = Modifier.padding(bottom = 4.dp))
        Text(value, style = InvexType.ExamStatValue, color = Color.Black)
    }
}

/** Four orange round service buttons (70dp, the circle is inside the drawable) with a 2-line label. */
@Composable
fun ServicesRow(onService: (ExamService) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        ExamService.entries.forEach { service ->
            Column(
                modifier = Modifier.width(70.dp).clickable { onService(service) },
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(painterResource(service.iconRes), contentDescription = service.label.replace("\n", " "), modifier = Modifier.size(70.dp))
                Text(
                    text = service.label,
                    style = InvexType.ServiceLabel,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

/** CANCEL (grey) and PROCEED (black), 52dp high, equal width, 16 apart. */
@Composable
fun ExamActionButtons(onCancel: () -> Unit, onProceed: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ActionButton("CANCEL", PillGrey, Color.Black, onCancel, Modifier.weight(1f))
        ActionButton("PROCEED", IconColor, SoftWhite, onProceed, Modifier.weight(1f))
    }
}

@Composable
private fun ActionButton(text: String, fill: Color, textColor: Color, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(fill)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = InvexType.ExamButton, color = textColor)
    }
}
