package com.invex.app.admitcard

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.medicalleave.semesterOrdinal
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText

/** The generated admit card: details, exam list, QR code and the student's signature. */
@Composable
fun AdmitCardView(card: AdmitCard, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, Color.Black, shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CardHeader(card.student)
        HorizontalDivider(thickness = 1.dp, color = TableHeaderText.copy(alpha = 0.5f))
        if (card.exams.isEmpty()) {
            Text("You have no upcoming exams.", style = InvexType.Body16, color = TableHeaderText)
        } else {
            Column {
                card.exams.forEach { ExamRow(it) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QrBox(card.qrCode)
            CardSignatureBox(card.signaturePng, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CardHeader(student: AdmitStudent) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.width(80.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Admit Card", style = InvexType.Body16, color = Color.Black)
            Image(
                painter = painterResource(R.drawable.ic_face_display),
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailItem("Name", student.name, Modifier.weight(1f))
                DetailItem("Enrollment Number", student.enrollmentNumber, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailItem("Course", student.course, Modifier.weight(1f))
                DetailItem("Specialization", student.specialization, Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailItem("Semester", semesterOrdinal(student.semester), Modifier.weight(1f))
                DetailItem("Branch", student.branch, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = InvexType.ExamSmallLabel, color = TableHeaderText)
        Text(value.ifBlank { "-" }, style = InvexType.Caption12, color = Color.Black)
    }
}

@Composable
private fun ExamRow(exam: AdmitExam) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Course", style = InvexType.ExamSmallLabel, color = TableHeaderText)
                Text(exam.courseTitle, style = InvexType.Caption12, color = Color.Black)
            }
            Column(Modifier.width(88.dp)) {
                Text("Date", style = InvexType.ExamSmallLabel, color = TableHeaderText)
                Text(exam.dateText, style = InvexType.Caption12, color = Color.Black)
            }
            Column(Modifier.width(32.dp)) {
                Text("Shift", style = InvexType.ExamSmallLabel, color = TableHeaderText)
                Text(exam.shift, style = InvexType.Caption12, color = Color.Black)
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Color.Black)
    }
}

/** Blue 96dp box. The QR is dark on white inside it, so the teacher's scanner can read it. */
@Composable
private fun QrBox(code: String?) {
    Box(
        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(8.dp)).background(Orange),
        contentAlignment = Alignment.Center,
    ) {
        if (code != null) {
            val qr = remember(code) { qrBitmap(code, 400).asImageBitmap() }
            Image(
                bitmap = qr,
                contentDescription = "Admit card QR code",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(4.dp)),
            )
        }
    }
}

/** Blue box with the saved signature in soft white. */
@Composable
private fun CardSignatureBox(png: ByteArray?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(96.dp).clip(RoundedCornerShape(8.dp)).background(Orange).padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = remember(png) { png?.let { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() } }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Your signature",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(SoftWhite),
                modifier = Modifier.fillMaxWidth().height(72.dp),
            )
        }
    }
}
