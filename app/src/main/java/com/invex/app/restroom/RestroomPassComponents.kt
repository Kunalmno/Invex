package com.invex.app.restroom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.invex.app.R
import com.invex.app.ui.components.InvexHairline
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.ActiveBadgeBg
import com.invex.app.ui.theme.CardLabelColor
import com.invex.app.ui.theme.HoverSelect
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.Inter
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.OutlineShadow
import com.invex.app.ui.theme.ReportPillBg
import com.invex.app.ui.theme.ReportPillText
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.StatusGreen
import com.invex.app.ui.theme.StatusRed
import com.invex.app.ui.theme.StatusYellow
import com.invex.app.ui.theme.TableHeaderText
import com.invex.app.ui.theme.TableRowInteraction
import com.invex.app.ui.theme.TicketBorder
import com.invex.app.ui.theme.TimeRemainingBg

/**
 * Custom ticket shape that renders rounded exterior corners and semicircular cutout
 * notches on both left and right edges at a configurable vertical position.
 */
class TicketCardShape(
    private val cornerRadius: Dp = 10.dp,
    private val notchRadius: Dp = 12.dp,
    private val notchYOffset: Dp? = null,
    private val notchYFraction: Float = 0.69f,
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cr = with(density) { cornerRadius.toPx() }
        val nr = with(density) { notchRadius.toPx() }
        val w = size.width
        val h = size.height
        val notchY = notchYOffset?.let { with(density) { it.toPx() } } ?: (h * notchYFraction)

        val path = Path().apply {
            reset()
            // Top-left to top-right
            moveTo(cr, 0f)
            lineTo(w - cr, 0f)
            quadraticBezierTo(w, 0f, w, cr)

            // Right edge down to notch
            lineTo(w, notchY - nr)
            // Right edge semicircular cutout inward (into card)
            arcTo(
                rect = Rect(
                    left = w - nr,
                    top = notchY - nr,
                    right = w + nr,
                    bottom = notchY + nr,
                ),
                startAngleDegrees = -90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false,
            )
            // Right edge down to bottom-right corner
            lineTo(w, h - cr)
            quadraticBezierTo(w, h, w - cr, h)

            // Bottom edge to bottom-left corner
            lineTo(cr, h)
            quadraticBezierTo(0f, h, 0f, h - cr)

            // Left edge up to notch
            lineTo(0f, notchY + nr)
            // Left edge semicircular cutout inward (into card)
            arcTo(
                rect = Rect(
                    left = -nr,
                    top = notchY - nr,
                    right = nr,
                    bottom = notchY + nr,
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false,
            )
            // Left edge up to top-left corner
            lineTo(0f, cr)
            quadraticBezierTo(0f, 0f, cr, 0f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * Top bar header with Back chevron, Orange circle enclosing a ticket badge glyph,
 * and "Restroom Pass" with orange underline.
 */
@Composable
fun RestroomHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chevron_back),
            contentDescription = "Back",
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Orange),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_ticket_badge),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
        LineUnderlinedText(
            text = "Restroom Pass",
            style = InvexType.PageHeading,
            color = IconColor,
            underlineColor = Orange,
            underlineOffset = 4.dp,
            thickness = 3.dp,
        )
    }
}

/**
 * Single summary card: status dot with 1px border, label, and large count.
 */
@Composable
fun PassesStatusCard(
    label: String,
    count: Int,
    dotColor: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .height(70.dp)
            .clip(shape)
            .background(HoverSelect)
            .border(InvexHairline, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(dotColor, RoundedCornerShape(4.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(4.dp)),
            )
            Text(
                text = label,
                style = InvexType.CardLabel,
                color = CardLabelColor,
            )
        }
        Text(
            text = "%02d".format(count),
            style = InvexType.CardCount,
            color = Color.Black,
            modifier = Modifier.align(Alignment.BottomStart),
        )
    }
}

/** Row of 3 summary cards: Out, Returned, Overdue. */
@Composable
fun PassesStatusRow(
    summary: PassSummary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PassesStatusCard(
            label = "Out",
            count = summary.out,
            dotColor = StatusGreen,
            modifier = Modifier.weight(1f),
        )
        PassesStatusCard(
            label = "Returned",
            count = summary.returned,
            dotColor = StatusYellow,
            modifier = Modifier.weight(1f),
        )
        PassesStatusCard(
            label = "Overdue",
            count = summary.overdue,
            dotColor = StatusRed,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Primary Orange action CTA button. */
@Composable
fun IssuePassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Issue a Supplementary Sheet",
) {
    val shape = RoundedCornerShape(15.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(shape)
            .background(Orange)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_circle_plus),
            contentDescription = null,
            modifier = Modifier.size(50.dp),
        )
        Text(
            text = label,
            style = InvexType.ButtonLabel,
            color = Color.White,
        )
    }
}

/** Dotted line for timeline tracks and perforation dividers. */
@Composable
fun DottedLine(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFB2AFAF),
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 4.dp,
    gapLength: Dp = 4.dp,
) {
    Canvas(modifier = modifier.height(strokeWidth)) {
        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f,
        )
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = pathEffect,
        )
    }
}

/**
 * 358 x 325 dp Active Pass Ticket Card with runner timeline, remaining timer banner,
 * student metadata, perforation divider, and QR verification footer.
 */
@Composable
fun ActivePassCard(
    pass: ActivePass,
    onReport: () -> Unit,
    onInfo: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val ticketShape = TicketCardShape(
        cornerRadius = 10.dp,
        notchRadius = 12.dp,
        notchYFraction = 0.70f,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(ticketShape)
            .background(TableRowInteraction)
            .border(BorderStroke(2.dp, TicketBorder), ticketShape)
            .padding(top = 16.dp, bottom = 14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // 1. Timeline Row (Left Hall At - Runner Center - Elapsed at)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left departure time
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Left Hall At",
                        style = InvexType.RestroomFieldHeader,
                        color = TableHeaderText,
                    )
                    Text(
                        text = pass.leftHallAt,
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                        ),
                        color = Color.Black,
                    )
                }

                // Dotted track leading to runner
                DottedLine(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                )

                // Central runner node
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_running),
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                    Text(
                        text = pass.durationLabel,
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                        ),
                        color = Color.Black,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ActiveBadgeBg)
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Active",
                            style = TextStyle(
                                fontFamily = Inter,
                                fontWeight = FontWeight.Medium,
                                fontSize = 10.sp,
                                lineHeight = 10.sp,
                            ),
                            color = SoftWhite,
                        )
                    }
                }

                // Dotted track following runner
                DottedLine(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                )

                // Right elapsed time
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Elapsed at",
                        style = InvexType.RestroomFieldHeader,
                        color = TableHeaderText,
                    )
                    Text(
                        text = pass.elapsedAt,
                        style = TextStyle(
                            fontFamily = Inter,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 24.sp,
                        ),
                        color = Color.Black,
                    )
                }
            }

            // 2. Time Remaining Banner (with Report action)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(TimeRemainingBg)
                    .border(InvexHairline, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Column(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Time Remaining",
                        style = InvexType.RestroomFieldHeader,
                        color = TableHeaderText,
                    )
                    Text(
                        text = pass.remainingFormatted,
                        style = InvexType.RestroomCountdown,
                        color = Color.Black,
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ReportPillBg)
                        .clickable(onClick = onReport)
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Report",
                        style = InvexType.RestroomReportButton,
                        color = ReportPillText,
                    )
                }
            }

            // 3. Student Metadata 4-Column Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetadataColumn(label = "Student", value = pass.studentName)
                MetadataColumn(label = "Seat", value = pass.seat)
                MetadataColumn(label = "Reg No", value = pass.regNo)
                MetadataColumn(label = "Course", value = pass.course)
            }

            Spacer(Modifier.height(4.dp))

            // 4. Perforation Dotted Divider
            DottedLine(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Color(0xFFB2AFAF),
                strokeWidth = 1.dp,
                dashLength = 3.dp,
                gapLength = 3.dp,
            )

            // 5. Bottom Verification Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Info block
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.clickable(onClick = onInfo),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_info),
                        contentDescription = "Info",
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Info",
                        style = InvexType.RestroomSmallMeta,
                        color = Color.Black,
                    )
                    Text(
                        text = pass.validUntil,
                        style = InvexType.RestroomMicro,
                        color = TableHeaderText,
                    )
                }

                // QR Code block
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_qrcode),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(width = 35.dp, height = 34.dp),
                    )
                    Text(
                        text = pass.serialNumber,
                        style = InvexType.RestroomMicro,
                        color = Color.Black,
                    )
                }

                // Download block
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.clickable(onClick = onDownload),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_download),
                        contentDescription = "Download",
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Download",
                        style = InvexType.RestroomSmallMeta,
                        color = Color.Black,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataColumn(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            style = InvexType.RestroomFieldHeader,
            color = TableHeaderText,
        )
        Text(
            text = value,
            style = InvexType.RestroomFieldValue,
            color = Color.Black,
        )
    }
}

/**
 * Historical Pass item with side notches, status glyph, name, validity, and duration.
 */
@Composable
fun RecentPassCard(
    item: RecentPassItem,
    modifier: Modifier = Modifier,
) {
    val cardShape = TicketCardShape(
        cornerRadius = 15.dp,
        notchRadius = 10.dp,
        notchYFraction = 0.5f,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(cardShape)
            .background(TableRowInteraction)
            .border(InvexHairline, cardShape)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status icon (check or warning hazard)
            Image(
                painter = painterResource(
                    if (item.isReturned) R.drawable.ic_returned else R.drawable.ic_notreturned,
                ),
                contentDescription = item.status,
                modifier = Modifier.size(50.dp),
            )

            // Student and status details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = item.studentName,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    ),
                    color = Color.Black,
                )
                Text(
                    text = item.status,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                    ),
                    color = TableHeaderText,
                )
                Text(
                    text = item.validUntil,
                    style = InvexType.RestroomSmallMeta,
                    color = TableHeaderText,
                )
            }

            // Duration and time slot
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = item.duration,
                    style = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 20.sp,
                    ),
                    color = Color.Black,
                )
                Text(
                    text = item.timeRange,
                    style = InvexType.RestroomSmallMeta,
                    color = TableHeaderText,
                )
            }
        }
    }
}

/** Confirmation dialog when reporting an overdue or non-compliant pass. */
@Composable
fun ReportStudentDialog(
    studentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Report Student",
                style = InvexType.CourseTitle,
                color = Color.Black,
            )
        },
        text = {
            Text(
                text = "Notify the hall runner and invigilator supervisor that $studentName has exceeded the allowed restroom leave time or requires assistance?",
                style = InvexType.Body16,
                color = TableHeaderText,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm("Time limit exceeded") }) {
                Text("Report to Runner", color = Orange, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TableHeaderText)
            }
        },
        containerColor = SoftWhite,
        shape = RoundedCornerShape(16.dp),
    )
}
