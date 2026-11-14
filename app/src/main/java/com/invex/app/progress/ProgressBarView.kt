package com.invex.app.progress

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.PillGrey

/**
 * Loading screen: the "InvEx" wordmark with a progress bar below it, centered on the screen.
 * [progress] runs from 0f to 1f and the fill animates to it.
 */
@Composable
fun ProgressBarView(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "progress")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MobileBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Text("InvEx", style = InvexType.Wordmark, color = Color.Black)
            ProgressTrack(progress = animated)
        }
    }
}

/** 200 x 8dp track with a 4dp-rounded fill. Both ends are rounded the same (4dp grid). */
@Composable
fun ProgressTrack(progress: Float, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .width(200.dp)
            .height(8.dp)
            .clip(shape)
            .background(PillGrey),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Orange, shape),
        )
    }
}

// 0.32f = 64dp of the 200dp track, the demo value from the design (65dp rounded to the grid).
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ProgressBarViewPreview() {
    InvexTheme { ProgressBarView(progress = 0.32f) }
}
