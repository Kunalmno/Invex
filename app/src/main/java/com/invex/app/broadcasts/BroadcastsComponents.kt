package com.invex.app.broadcasts

import com.invex.app.ui.theme.InvexTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.invex.app.ui.components.InfoModalSheet
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.components.SheetBody
import com.invex.app.ui.components.SheetListItem
import com.invex.app.ui.components.SheetTitle
import com.invex.app.ui.theme.InvexType

/**
 * The bottom sheet that sits over the Dashboard (the grey area above it in the Figma frames is the Dashboard
 * behind the scrim). It shows either the Broadcasts list or one broadcast, depending on the state.
 * Nothing is drawn while the sheet is Closed.
 */
@Composable
fun BroadcastsSheetHost(
    state: BroadcastsUiState,
    onDismiss: () -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    if (state.sheet == BroadcastSheet.Closed) return
    InfoModalSheet(onDismiss = onDismiss) {
        when (val sheet = state.sheet) {
            BroadcastSheet.List -> BroadcastListContent(state.broadcasts, onOpenDetail, state.isLoading, state.error)
            is BroadcastSheet.Detail -> state.find(sheet.id)?.let { BroadcastDetailContent(it) }
                ?: SheetBody { StatusMessage(if (state.isLoading) "Loading..." else state.error ?: "This broadcast is no longer available") }
            BroadcastSheet.Closed -> Unit
        }
    }
}

@Composable
fun BroadcastListContent(
    broadcasts: List<Broadcast>,
    onOpen: (String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
) {
    SheetBody {
        SheetTitle("Broadcasts")
        if (broadcasts.isEmpty()) {
            StatusMessage(error ?: if (isLoading) "Loading broadcasts..." else "No broadcasts yet")
        }
        Column {
            broadcasts.forEach { item ->
                SheetListItem(item.heading, item.subheading, item.snippet, onClick = { onOpen(item.id) })
                HorizontalDivider(thickness = 1.dp, color = Color.Black)
            }
        }
    }
}

/** One broadcast: title, indented text, and who sent it. Body is left aligned (the design was justified). */
@Composable
fun BroadcastDetailContent(item: Broadcast) {
    SheetBody {
        SheetTitle(item.heading)
        Text(
            text = item.body,
            style = InvexType.BroadcastText,
            color = Color.Black,
            modifier = Modifier.padding(start = 24.dp, end = 48.dp, top = 12.dp),
        )
        Text(
            text = "Broadcasted By : ${item.broadcastedBy}",
            style = InvexType.BroadcastText,
            color = Color.Black,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 560)
@Composable
private fun BroadcastListPreview() {
    InvexTheme { BroadcastListContent(sampleBroadcasts(), onOpen = {}) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 560)
@Composable
private fun BroadcastDetailPreview() {
    InvexTheme { BroadcastDetailContent(sampleBroadcasts().first()) }
}
