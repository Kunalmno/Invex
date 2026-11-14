package com.invex.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.TableHeaderText

/**
 * Header of the pages opened from Settings: grey "Settings /" breadcrumb, then chevron + the
 * page title in the pixel font with an orange underline. Used by Account Information and Notifications.
 */
@Composable
fun SettingsSubHeader(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Settings /",
            style = InvexType.Breadcrumb,
            color = TableHeaderText,
            modifier = Modifier.clickable(onClick = onBack),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_chevron_back),
                contentDescription = "Back",
                modifier = Modifier.size(32.dp).clip(CircleShape).clickable(onClick = onBack),
            )
            LineUnderlinedText(
                text = title,
                style = InvexType.PageHeading,
                color = IconColor,
                underlineColor = Orange,
                underlineOffset = 4.dp,
                thickness = 4.dp,
            )
        }
    }
}
