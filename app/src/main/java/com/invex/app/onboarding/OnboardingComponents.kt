package com.invex.app.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.unit.dp
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.IndicatorInactive
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.PillGrey
import com.invex.app.ui.theme.SoftWhite

/**
 * Coloured circle with the illustration centered in it. Every circle is 248dp and every illustration
 * is fitted into the same 168 x 152dp box (a little wider than tall, because the second drawing is wide),
 * so the three screens look alike. The three drawables are cropped to the drawing, so Fit sizes the art itself
 * and the drawing is centered in the circle. Drawn areas come out within about 5% of each other. The drawing is tinted SoftWhite
 * (all three are single-colour line art on a dark circle). Remove the tint if an SVG has more than one colour.
 */
@Composable
fun IllustrationCircle(page: OnboardingPage, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(248.dp)
            .background(page.accent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(page.illustration),
            contentDescription = null,
            modifier = Modifier.size(
                width = 168.dp * page.illustrationScale,
                height = 152.dp * page.illustrationScale,
            ),
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(SoftWhite),
        )
    }
}

/** Three 20dp squares. The one for the current page is filled with the page accent. */
@Composable
fun OnboardingProgress(count: Int, current: Int, accent: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(32.dp)) {
        repeat(count) { index ->
            Box(
                Modifier
                    .size(20.dp)
                    .background(if (index == current) accent else IndicatorInactive, shape)
                    .border(1.dp, Color.Black, shape),
            )
        }
    }
}

/** Skip + Next on pages 1 and 2; a single Start on the last page. */
@Composable
fun OnboardingButtons(
    isLast: Boolean,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.widthIn(max = 320.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
    ) {
        if (!isLast) {
            OnboardingButton("Skip", PillGrey, IconColor, onSkip, Modifier.weight(1f))
        }
        OnboardingButton(
            label = if (isLast) "Start" else "Next",
            background = IconColor,
            textColor = Color.White,
            onClick = onNext,
            modifier = if (isLast) Modifier.width(140.dp) else Modifier.weight(1f),
        )
    }
}

@Composable
private fun OnboardingButton(
    label: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = InvexType.OnboardingButton, color = textColor, maxLines = 1)
    }
}
