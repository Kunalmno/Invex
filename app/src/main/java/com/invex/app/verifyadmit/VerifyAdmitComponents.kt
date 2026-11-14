package com.invex.app.verifyadmit

import com.invex.app.ui.theme.accentIcon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.OutlineShadow
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText
import com.invex.app.ui.theme.WarningRed
import com.invex.app.data.verify.VerifyResult

/** Chevron (shared drawable) + admit card icon (circle is inside the drawable) + underlined title. */
@Composable
fun VerifyAdmitHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chevron_back),
            contentDescription = "Back",
            modifier = Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onBack),
        )
        Image(
            painter = painterResource(accentIcon(R.drawable.ic_admit_card_heading)),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
        )
        Box(Modifier.width(4.dp))
        LineUnderlinedText(
            text = "Verify Admit Card",
            style = InvexType.PageHeading,
            color = IconColor,
            underlineColor = Orange,
            underlineOffset = 4.dp,
            thickness = 4.dp,
        )
    }
}

/** Grey tray with two 130 x 40 tabs: the selected one is orange, the other is SoftWhite. */
@Composable
fun VerifyTabs(selected: VerifyTab, onSelect: (VerifyTab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(OutlineShadow)
            .height(52.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VerifyTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Orange else SoftWhite)
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(tab.label, style = InvexType.VerifyTab, color = if (isSelected) SoftWhite else Color.Black)
            }
        }
    }
}

/**
 * The scan area: a 220 x 240 rounded window that is a live camera while scanning (it replaces the old
 * static SoftWhite box). When the camera is off or has no permission it shows the old box look:
 * orange viewfinder icon + a line of text.
 */
@Composable
fun ScanWindow(
    hasCameraPermission: Boolean,
    scanning: Boolean,
    onQrDetected: (String) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier.size(width = 220.dp, height = 240.dp).clip(shape).background(SoftWhite),
        contentAlignment = Alignment.Center,
    ) {
        if (hasCameraPermission && scanning) {
            QrCameraView(active = true, onQrDetected = onQrDetected, modifier = Modifier.matchParentSize())
            // Viewfinder corners on top of the live picture
            Image(
                painter = painterResource(R.drawable.ic_qr_scanning),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (!hasCameraPermission) Modifier.clickable(onClick = onRequestPermission) else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Image(painterResource(R.drawable.ic_qr_scanning), contentDescription = null, modifier = Modifier.size(96.dp))
                Text(
                    text = if (hasCameraPermission) "QR Code scanned" else "Allow camera access",
                    style = InvexType.Body16,
                    color = Color.Black,
                )
            }
        }
    }
}

/** Orange full-width button: QR icon + "Scan the Student" centered together. */
@Composable
fun ScanStudentButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Orange)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_qrcode),
            contentDescription = null,
            colorFilter = ColorFilter.tint(SoftWhite),
            modifier = Modifier.size(32.dp),
        )
        Text("Scan the Student", style = InvexType.ScanButton, color = SoftWhite)
    }
}

@Composable
fun ScanHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = InvexType.ScanHint,
        color = Color.Black,
        textAlign = TextAlign.Start,
        modifier = modifier,
    )
}

/** Outer faint card that holds the scan window and its hint. */
@Composable
fun ScanCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(OutlineShadow.copy(alpha = 0.5f))
            .border(1.dp, OutlineShadow, shape)
            .padding(horizontal = 20.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) { content() }
}

/**
 * Result of a scan: green-free and simple. Verified shows the student's name, ID and seat;
 * a rejected card shows the reason in red. Not in the Figma design yet.
 */
@Composable
fun VerifyResultCard(result: VerifyResult, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(SoftWhite)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        when (result) {
            is VerifyResult.Verified -> {
                val s = result.student
                Text(
                    text = if (s.alreadyVerified) "Already verified earlier" else "Admit card verified",
                    style = InvexType.ExamSmallLabel,
                    color = TableHeaderText,
                )
                Text(s.studentName, style = InvexType.Body16, color = Color.Black)
                Text(
                    text = "${s.registrationNumber}  |  Row ${s.seatRow}, Seat ${s.seatCol}",
                    style = InvexType.ExamSmallLabel,
                    color = Color.Black,
                )
            }
            is VerifyResult.Rejected -> Text(result.message, style = InvexType.Body16, color = WarningRed)
        }
    }
}
