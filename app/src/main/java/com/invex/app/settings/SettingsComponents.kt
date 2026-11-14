package com.invex.app.settings

import com.invex.app.ui.theme.accentIcon
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.OutlineShadow
import com.invex.app.ui.theme.WarningRed

/** "Settings" with an orange underline. No back arrow: Settings is a bottom-bar tab. */
@Composable
fun SettingsHeader(modifier: Modifier = Modifier) {
    LineUnderlinedText(
        text = "Settings",
        style = InvexType.PageHeading,
        color = IconColor,
        underlineColor = Orange,
        underlineOffset = 4.dp,
        thickness = 4.dp,
        modifier = modifier,
    )
}

/**
 * 48dp row: round orange icon, title, chevron. While pressed it gets the soft grey fill
 * (the filled "Notifications" row in the design is that pressed state).
 */
@Composable
fun SettingsRow(
    item: SettingsItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (pressed) OutlineShadow else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(item)
        Text(
            text = item.title,
            style = InvexType.SettingsRow,
            color = Color.Black,
            modifier = Modifier.weight(1f),
        )
        Image(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(24.dp),
        )
    }
}

/** 32dp icon. The bell drawable has its own orange circle; the user glyph is drawn on an orange circle here. */
@Composable
private fun SettingsIcon(item: SettingsItem) {
    when (item) {
        SettingsItem.NOTIFICATIONS -> Image(
            painter = painterResource(accentIcon(R.drawable.ic_notification_bell)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(32.dp),
        )
        SettingsItem.ACCOUNT_INFORMATION -> Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Orange),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_user_information),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(width = 20.dp, height = 24.dp),
            )
        }
    }
}

@Composable
fun LogOutText(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = "LOG OUT",
        style = InvexType.LogOut,
        color = WarningRed,
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 4.dp, horizontal = 8.dp),
    )
}
