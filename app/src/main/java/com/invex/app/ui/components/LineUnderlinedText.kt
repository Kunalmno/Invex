package com.invex.app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Text with a coloured line under every wrapped line of text. The line is drawn behind the text, so the
 * layout size does not change. [underlineOffset] is the distance below each baseline.
 * Used by the onboarding titles (offset 8) and the login labels and "Sign up" (offset 4).
 */
@Composable
fun LineUnderlinedText(
    text: String,
    style: TextStyle,
    color: Color,
    underlineColor: Color,
    modifier: Modifier = Modifier,
    underlineOffset: Dp = 8.dp,
    thickness: Dp = 2.dp,
) {
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        style = style,
        color = color,
        onTextLayout = { layout = it },
        modifier = modifier.drawBehind {
            val result = layout ?: return@drawBehind
            for (line in 0 until result.lineCount) {
                val left = result.getLineLeft(line)
                val right = result.getLineRight(line)
                drawRect(
                    color = underlineColor,
                    topLeft = Offset(left, result.getLineBaseline(line) + underlineOffset.toPx()),
                    size = Size(right - left, thickness.toPx()),
                )
            }
        },
    )
}
