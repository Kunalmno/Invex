package com.invex.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.TableHeaderText

/** Centered grey message for loading and load errors, with an optional orange action ("Try again"). */
@Composable
fun StatusMessage(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = text, style = InvexType.Body16, color = TableHeaderText)
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = InvexType.Body16,
                color = Orange,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}
