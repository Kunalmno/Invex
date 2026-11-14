package com.invex.app.notifications

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.PillGrey
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.TableHeaderText

/**
 * "Email Notification" block: heading, grey explanation, and the switch.
 */
@Composable
fun EmailNotificationSection(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Email Notification", style = InvexType.NotifHeading, color = Color.Black)
        Text(
            "Invex can send you email notification for new exams.",
            style = InvexType.NotifEmailText,
            color = TableHeaderText,
        )
        InvexSwitch(checked = enabled, onCheckedChange = onEnabledChange, modifier = Modifier.padding(top = 4.dp))
    }
}

/** 84 x 36 grey pill with a black outline and a white round thumb (right = on). */
@Composable
fun InvexSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val thumbX by animateDpAsState(if (checked) 48.dp else 0.dp, label = "thumb")
    Box(
        modifier = modifier
            .size(width = 84.dp, height = 36.dp)
            .clip(CircleShape)
            .background(PillGrey)
            .border(1.5.dp, Color.Black, CircleShape)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(3.dp),
    ) {
        Box(
            Modifier
                .offset(x = thumbX)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.5.dp, Color.Black, CircleShape),
        )
    }
}

/** Title with a checkbox at the end, and a grey description below (left aligned, max 300 wide). */
@Composable
fun NotificationOption(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = InvexType.OptionTitle, color = Color.Black, modifier = Modifier.weight(1f))
            InvexCheckbox(checked = checked, onCheckedChange = onCheckedChange)
        }
        Text(
            text = description,
            style = InvexType.OptionDesc,
            color = TableHeaderText,
            modifier = Modifier.widthIn(max = 300.dp),
        )
    }
}

/** 28dp square, 2dp black outline, radius 8. Ticked = black fill with a light check mark. */
@Composable
fun InvexCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(shape)
            .background(if (checked) Color.Black else Color.Transparent)
            .border(2.dp, Color.Black, shape)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Canvas(Modifier.size(28.dp)) {
                val u = size.width / 28f
                val stroke = Stroke(width = 3f * u, cap = StrokeCap.Round)
                drawLine(SoftWhite, Offset(7f * u, 14f * u), Offset(12f * u, 19f * u), stroke.width, StrokeCap.Round)
                drawLine(SoftWhite, Offset(12f * u, 19f * u), Offset(21f * u, 9f * u), stroke.width, StrokeCap.Round)
            }
        }
    }
}
