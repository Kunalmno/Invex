package com.invex.app.sign

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.app.R
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.SignatureBoxGrey
import com.invex.app.ui.theme.SignatureLine
import com.invex.app.ui.theme.TableHeaderText

/**
 * 350 x 160dp signature area. The baseline sits 80dp from the top and the
 * Back / Clear / Save row 16dp from the bottom, as in the Figma frame.
 */
@Composable
fun SignatureBox(
    tab: SignTab,
    strokes: List<List<Offset>>,
    typedName: String,
    isSaved: Boolean,
    onStrokeStart: (Offset) -> Unit,
    onStrokeMove: (Offset) -> Unit,
    onTypedNameChange: (String) -> Unit,
    onBack: () -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SignatureBoxGrey),
    ) {
        when (tab) {
            SignTab.DRAW -> DrawingSurface(
                strokes = strokes,
                onStart = onStrokeStart,
                onMove = onStrokeMove,
                modifier = Modifier.fillMaxSize(),
            )
            SignTab.TYPE -> TypedSignature(
                value = typedName,
                onValueChange = onTypedNameChange,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 52.dp),
            )
        }

        // Baseline: 248dp wide, centered, 80dp from the top.
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .width(248.dp)
                .height(1.dp)
                .background(SignatureLine),
        )

        // Controls: 312dp wide row, 16dp from the bottom.
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .width(312.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ControlButton(R.drawable.ic_undo, "Back", onBack)
                ControlButton(R.drawable.ic_eraser, "Clear", onClear)
            }
            Text(
                text = if (isSaved) "Saved" else "Save signature",
                style = InvexType.SignControl,
                color = TableHeaderText,
                modifier = Modifier.clickable(onClick = onSave),
            )
        }
    }
}

@Composable
private fun ControlButton(iconRes: Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Text(label, style = InvexType.SignControl, color = TableHeaderText)
    }
}

/** Finger drawing. Strokes come from the ViewModel so Back can remove the last one. */
@Composable
private fun DrawingSurface(
    strokes: List<List<Offset>>,
    onStart: (Offset) -> Unit,
    onMove: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentStart by rememberUpdatedState(onStart)
    val currentMove by rememberUpdatedState(onMove)

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { currentStart(it) },
                onDrag = { change, _ ->
                    change.consume()
                    currentMove(change.position)
                },
            )
        },
    ) {
        strokes.forEach { drawStroke(it, IconColor) }
    }
}

private fun DrawScope.drawStroke(points: List<Offset>, color: Color) {
    if (points.isEmpty()) return
    val width = 2.5.dp.toPx()
    if (points.size == 1) {
        drawCircle(color, radius = width / 2f, center = points[0])
        return
    }
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            val prev = points[i - 1]
            val cur = points[i]
            // Curve through midpoints so the line looks smooth, not segmented.
            quadraticBezierTo(prev.x, prev.y, (prev.x + cur.x) / 2f, (prev.y + cur.y) / 2f)
        }
        lineTo(points.last().x, points.last().y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
}

/** Type tab: follows the Draw layout. The name sits on the baseline like an underline. */
@Composable
private fun TypedSignature(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = TextStyle(
        fontFamily = FontFamily.Cursive,
        fontSize = 32.sp,
        textAlign = TextAlign.Center,
    )
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = style.copy(color = IconColor),
        cursorBrush = SolidColor(IconColor),
        modifier = modifier,
        decorationBox = { inner ->
            Box(
                modifier = Modifier.fillMaxSize().offset(y = 8.dp),
                contentAlignment = Alignment.BottomCenter,
            ) {
                if (value.isEmpty()) {
                    Text("Type your name", style = style, color = TableHeaderText.copy(alpha = 0.6f))
                }
                inner()
            }
        },
    )
}
