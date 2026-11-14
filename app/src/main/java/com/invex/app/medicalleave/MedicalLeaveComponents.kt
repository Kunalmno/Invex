package com.invex.app.medicalleave

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.selection.toggleable
import com.invex.app.ui.components.UpDownArrows
import com.invex.app.ui.components.dottedBorder
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite

private val ErrorRed = Color(0xFFA11B1B)

/** Reading screen: label on top, value below, a black line under it. Empty value shows "-". */
@Composable
fun LeaveField(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Text(label, style = InvexType.Body16Regular, color = Color.Black)
        Text(
            text = value.ifBlank { "-" },
            style = InvexType.Body18,
            color = Color.Black,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        HorizontalDivider(thickness = 1.dp, color = Color.Black)
    }
}

/** 16dp square checkbox with the confirmation text next to it. */
@Composable
fun VerifyCheckRow(checked: Boolean, onChange: (Boolean) -> Unit, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.toggleable(value = checked, role = Role.Checkbox, onValueChange = onChange),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val shape = RoundedCornerShape(2.dp)
        Box(
            Modifier.size(16.dp).clip(shape).background(if (checked) Color.Black else Color.Transparent).border(1.dp, Color.Black, shape),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Canvas(Modifier.size(16.dp)) {
                    val u = size.width / 16f
                    drawLine(SoftWhite, Offset(4f * u, 8f * u), Offset(7f * u, 11f * u), 2f * u, StrokeCap.Round)
                    drawLine(SoftWhite, Offset(7f * u, 11f * u), Offset(12f * u, 5f * u), 2f * u, StrokeCap.Round)
                }
            }
        }
        Text(text, style = InvexType.Caption12, color = Color.Black)
    }
}

@Composable
fun FormLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = InvexType.Caption12, color = Color.Black, modifier = modifier)
}

/** Dotted dropdown: current value + up/down arrows, a menu with all options. */
@Composable
fun ConditionDropdown(
    value: String,
    options: List<String>,
    open: Boolean,
    onToggle: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .dottedBorder(16.dp)
                .clickable { onToggle(true) }
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(value, style = InvexType.Body18, color = Color.Black)
            UpDownArrows()
        }
        DropdownMenu(expanded = open, onDismissRequest = { onToggle(false) }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option, style = InvexType.Body16) }, onClick = { onSelect(option) })
            }
        }
    }
}

/** Dotted, multi-line text box (180dp high) with a centered hint while empty. */
@Composable
fun DetailsBox(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .dottedBorder(16.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onChange,
            textStyle = InvexType.Body18Regular.copy(color = Color.Black, textAlign = if (value.isEmpty()) TextAlign.Center else TextAlign.Start),
            cursorBrush = SolidColor(Color.Black),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text("Enter the Medical Details.", style = InvexType.Body18Regular, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                inner()
            },
        )
    }
}

/** "Choose Files" button, the chosen file (tap "Remove" to clear) and the red rule line. */
@Composable
fun DocumentPicker(
    fileName: String?,
    error: String?,
    onChoose: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier.width(160.dp).height(52.dp).clip(RoundedCornerShape(12.dp)).background(Orange).clickable(onClick = onChoose),
            contentAlignment = Alignment.Center,
        ) { Text("Choose Files", style = InvexType.Body16, color = SoftWhite) }
        if (fileName != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(fileName, style = InvexType.Caption12, color = Color.Black, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                Text("Remove", style = InvexType.Caption12, color = Orange, modifier = Modifier.clickable(onClick = onRemove).padding(4.dp))
            }
        }
        Text(
            text = error ?: "Valid files : JPG, PDF, DOC, TXT under 10 Mb size.",
            style = InvexType.Caption12,
            color = ErrorRed,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SubmitErrorText(text: String, modifier: Modifier = Modifier) {
    Text(text, style = InvexType.Caption12, color = ErrorRed, modifier = modifier)
}

@Composable
fun ReadingHint(text: String, modifier: Modifier = Modifier) {
    Text(text, style = InvexType.Caption12, color = Color.Black, modifier = modifier)
}
