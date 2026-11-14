package com.invex.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite

/** Student service screens: back chevron + round icon (circle is part of the drawable) + title with an accent underline. */
@Composable
fun ServiceHeader(title: String, @DrawableRes iconRes: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
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
        Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(40.dp))
        LineUnderlinedText(
            text = title,
            style = InvexType.PageHeading,
            color = IconColor,
            underlineColor = Orange,
            underlineOffset = 4.dp,
            thickness = 4.dp,
        )
    }
}

/** 1dp dotted outline with rounded corners (dropdowns and text boxes of the student screens). */
fun Modifier.dottedBorder(radius: Dp): Modifier = this.drawBehind {
    val inset = 0.5.dp.toPx()
    drawRoundRect(
        color = Color.Black,
        topLeft = Offset(inset, inset),
        size = Size(size.width - 2 * inset, size.height - 2 * inset),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 2.dp.toPx()))),
    )
}

/** Small up and down chevrons, drawn here (no asset needed). */
@Composable
fun UpDownArrows(modifier: Modifier = Modifier) {
    Canvas(modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val sw = 2.dp.toPx()
        fun chevron(cy: Float, dir: Float) {
            val d = h * 0.08f * dir
            drawLine(Color.Black, Offset(w * 0.3f, cy + d), Offset(w * 0.5f, cy - d), sw, androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(Color.Black, Offset(w * 0.5f, cy - d), Offset(w * 0.7f, cy + d), sw, androidx.compose.ui.graphics.StrokeCap.Round)
        }
        chevron(h * 0.32f, 1f)  // up
        chevron(h * 0.68f, -1f) // down
    }
}

/** CANCEL (soft white, black text) and PROCEED (accent, light text), 52dp high, equal width. */
@Composable
fun AccentActionButtons(
    onCancel: () -> Unit,
    onProceed: () -> Unit,
    modifier: Modifier = Modifier,
    proceedEnabled: Boolean = true,
    proceedLabel: String = "PROCEED",
    cancelLabel: String = "CANCEL",
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp)).background(SoftWhite).clickable(onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) { Text(cancelLabel, style = InvexType.ExamButton, color = Color.Black) }
        Box(
            Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(12.dp))
                .background(if (proceedEnabled) Orange else Orange.copy(alpha = 0.4f))
                .clickable(enabled = proceedEnabled, onClick = onProceed),
            contentAlignment = Alignment.Center,
        ) { Text(proceedLabel, style = InvexType.ExamButton, color = SoftWhite) }
    }
}
