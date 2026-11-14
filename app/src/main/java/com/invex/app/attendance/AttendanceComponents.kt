package com.invex.app.attendance

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.InvexHairline
import com.invex.app.ui.components.StatusCard
import com.invex.app.ui.theme.ChartLabelGrey
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.LegendGrey
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.SectionHeadingColor
import com.invex.app.ui.theme.StatusGreen
import com.invex.app.ui.theme.StatusRed
import com.invex.app.ui.theme.StatusYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** "All Courses" title with the up/down arrow. Tapping opens a menu of courses. */
@Composable
fun CourseSelector(
    selected: String,
    courses: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(selected, style = InvexType.CourseTitle, color = SectionHeadingColor)
            Image(
                painter = painterResource(R.drawable.ic_unfold_more),
                contentDescription = "Choose course",
                modifier = Modifier.size(24.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            courses.forEach { course ->
                DropdownMenuItem(
                    text = { Text(course, style = InvexType.Body16) },
                    onClick = {
                        onSelect(course)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Total / Present / Absent cards in one row. */
@Composable
fun AttendanceStatusRow(summary: AttendanceSummary, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatusCard("Total", summary.total, StatusGreen, Modifier.weight(1f))
        StatusCard("Present", summary.present, StatusYellow, Modifier.weight(1f))
        StatusCard("Absent", summary.absent, StatusRed, Modifier.weight(1f))
    }
}

/**
 * Donut chart drawn from data (not the exported SVG), 268 x 220dp like the Figma chart.
 * Slices run clockwise from 12 o'clock. Each slice gets a short leader line and its value label.
 * The ring is 192dp wide with a 48dp stroke, so the hole is 96dp (half the width).
 */
@Composable
fun DonutChart(slices: List<DonutSlice>, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val labelStyle = InvexType.ChartLabel.copy(color = ChartLabelGrey)

    Canvas(modifier = modifier.size(width = 268.dp, height = 220.dp)) {
        val total = slices.sumOf { it.value.toDouble() }.toFloat()
        if (total <= 0f) return@Canvas

        val outerR = 96.dp.toPx()
        val ringW = 48.dp.toPx()
        val midR = outerR - ringW / 2
        val innerR = outerR - ringW
        val center = Offset(size.width / 2f, 4.dp.toPx() + outerR)

        // 1. Arcs
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = 360f * slice.value / total
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - midR, center.y - midR),
                size = Size(midR * 2, midR * 2),
                style = Stroke(width = ringW),
            )
            startAngle += sweep
        }

        // 2. White separators at every slice boundary
        var boundary = -90f
        slices.forEach { slice ->
            val rad = boundary * PI.toFloat() / 180f
            drawLine(
                color = Color.White,
                start = Offset(center.x + cos(rad) * innerR, center.y + sin(rad) * innerR),
                end = Offset(center.x + cos(rad) * outerR, center.y + sin(rad) * outerR),
                strokeWidth = 1.5.dp.toPx(),
            )
            boundary += 360f * slice.value / total
        }

        // 3. Leader lines and value labels at each slice's mid angle
        var cursor = -90f
        slices.forEach { slice ->
            val sweep = 360f * slice.value / total
            val rad = (cursor + sweep / 2f) * PI.toFloat() / 180f
            val dx = cos(rad)
            val dy = sin(rad)
            val lineStart = Offset(center.x + dx * (outerR + 4.dp.toPx()), center.y + dy * (outerR + 4.dp.toPx()))
            val lineEnd = Offset(center.x + dx * (outerR + 16.dp.toPx()), center.y + dy * (outerR + 16.dp.toPx()))
            drawLine(ChartLabelGrey, lineStart, lineEnd, strokeWidth = 1.dp.toPx())

            val layout = measurer.measure(slice.value.toInt().toString(), labelStyle)
            val gap = 4.dp.toPx()
            val x = if (dx >= 0f) lineEnd.x + gap else lineEnd.x - gap - layout.size.width
            drawText(layout, topLeft = Offset(x, lineEnd.y - layout.size.height / 2f))
            cursor += sweep
        }
    }
}

/** Legend: "Students" caption over three label + count rows. */
@Composable
fun AttendanceLegend(summary: AttendanceSummary, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.width(232.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Students", style = InvexType.LegendCaption, color = SectionHeadingColor)
        LegendRow("Total", summary.total)
        LegendRow("Present", summary.present)
        LegendRow("Absent", summary.absent)
    }
}

// Swatches are grey in the Figma frame. Pass chart colors here once the legend should match the slices.
@Composable
private fun LegendRow(label: String, count: Int, swatch: Color = LegendGrey) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(12.dp).background(swatch, RoundedCornerShape(4.dp)))
            Text(label, style = InvexType.LegendLabel, color = SectionHeadingColor)
        }
        Text(count.toString(), style = InvexType.LegendCount, color = SectionHeadingColor)
    }
}

/** The bordered card holding the donut chart and its legend. */
@Composable
fun AnalyticsCard(
    slices: List<DonutSlice>,
    summary: AttendanceSummary,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MobileBackground)
            .border(InvexHairline, shape)
            .padding(horizontal = 40.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DonutChart(slices)
        AttendanceLegend(summary)
    }
}
