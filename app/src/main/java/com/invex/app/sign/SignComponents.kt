package com.invex.app.sign

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.InvexHairline
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.SwitchOffTrack
import com.invex.app.ui.theme.TabBlue
import com.invex.app.ui.theme.WarningRed

/**
 * Text with a coloured underline of a chosen thickness. TextDecoration.Underline can't set either,
 * and the Figma frame uses an orange heading line and a blue tab line.
 */
@Composable
fun UnderlinedText(
    text: String,
    style: TextStyle,
    color: Color,
    underlineColor: Color,
    thickness: Dp,
    gap: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier.width(IntrinsicSize.Max)) {
        Text(text = text, style = style, color = color, maxLines = 1, softWrap = false)
        Spacer(Modifier.height(gap))
        Box(Modifier.fillMaxWidth().height(thickness).background(underlineColor))
    }
}

/** "Draw" | "Type". The active tab has the blue underline; the other keeps the same height. */
@Composable
fun SignTabs(selected: SignTab, onSelect: (SignTab) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        SignTab.entries.forEach { tab ->
            UnderlinedText(
                text = tab.label,
                style = InvexType.TabLabel,
                color = Color.Black,
                underlineColor = if (tab == selected) TabBlue else Color.Transparent,
                thickness = 2.dp,
                gap = 4.dp,
                modifier = Modifier.clickable { onSelect(tab) },
            )
        }
    }
}

/** 20 x 12dp switch. Off: grey track, knob on the left. On: dark track, knob on the right. */
@Composable
fun ConsentSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 20.dp, height = 12.dp)
            .background(if (checked) IconColor else SwitchOffTrack, RoundedCornerShape(6.dp))
            .padding(horizontal = 2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(8.dp).background(Color.White, CircleShape))
    }
}

/** White card: the consent switch with its text, plus the red banner when a sign attempt is refused. */
@Composable
fun ConsentCard(
    consent: Boolean,
    onConsentChange: (Boolean) -> Unit,
    warning: SignWarning?,
    modifier: Modifier = Modifier,
    text: String = "By signing, I confirm that the student requires an additional answer sheet " +
        "and this request is made under my supervision.",
) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color.White)
            .border(InvexHairline, shape)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = consent, role = Role.Switch, onValueChange = onConsentChange)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ConsentSwitch(consent)
            Text(
                text = text,
                style = InvexType.Consent,
                color = Color.Black,
                textAlign = TextAlign.Justify,
                modifier = Modifier.weight(1f),
            )
        }
        if (warning != null) WarningBanner(warning.message)
    }
}

@Composable
private fun WarningBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(WarningRed)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_alert),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = message,
            style = InvexType.Warning,
            color = Color.Black,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Cancel (white) and "Sign the Supplement Sheets" (dark), 30dp tall. */
@Composable
fun SheetButtons(
    onCancel: () -> Unit,
    onSign: () -> Unit,
    modifier: Modifier = Modifier,
    signLabel: String = "Sign the Supplement Sheets",
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SheetButton("Cancel", Color.White, Color.Black, onCancel, border = InvexHairline)
        SheetButton(signLabel, IconColor, SoftWhite, onSign)
    }
}

@Composable
private fun SheetButton(
    label: String,
    background: Color,
    textColor: Color,
    onClick: () -> Unit,
    border: BorderStroke? = null,
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(shape)
            .background(background)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = InvexType.SheetButton, color = textColor, maxLines = 1)
    }
}
