package com.invex.app.circulars

import com.invex.app.ui.theme.InvexTheme
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.InfoModalSheet
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.components.SheetBody
import com.invex.app.ui.components.SheetListItem
import com.invex.app.ui.components.SheetTitle
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange

/** The Circulars sheet over the Dashboard: the list, or one circular. Nothing is drawn while Closed. */
@Composable
fun CircularsSheetHost(
    state: CircularsUiState,
    onDismiss: () -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    if (state.sheet == CircularSheet.Closed) return
    InfoModalSheet(onDismiss = onDismiss) {
        when (val sheet = state.sheet) {
            CircularSheet.List -> CircularListContent(state.circulars, onOpenDetail, state.isLoading, state.error)
            is CircularSheet.Detail -> state.find(sheet.id)?.let { CircularDetailContent(it) }
                ?: SheetBody { StatusMessage(if (state.isLoading) "Loading..." else state.error ?: "This circular is no longer available") }
            CircularSheet.Closed -> Unit
        }
    }
}

@Composable
fun CircularListContent(
    circulars: List<Circular>,
    onOpen: (String) -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
) {
    SheetBody {
        SheetTitle("Circulars")
        if (circulars.isEmpty()) {
            StatusMessage(error ?: if (isLoading) "Loading circulars..." else "No circulars yet")
        }
        Column {
            circulars.forEach { item ->
                SheetListItem(item.heading, item.subheading, item.snippet, onClick = { onOpen(item.id) })
                HorizontalDivider(thickness = 1.dp, color = Color.Black)
            }
        }
    }
}

/** One circular: title, indented text, the attached document (if any), and who circulated it. */
@Composable
fun CircularDetailContent(item: Circular) {
    SheetBody {
        SheetTitle(item.heading)
        Text(
            text = item.body,
            style = InvexType.BroadcastText,
            color = Color.Black,
            modifier = Modifier.padding(start = 24.dp, end = 48.dp, top = 12.dp),
        )
        item.attachment?.let { AttachmentRow(it, modifier = Modifier.padding(top = 20.dp)) }
        Text(
            text = "Circulated By : ${item.circulatedBy}",
            style = InvexType.BroadcastText,
            color = Color.Black,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

/**
 * Orange 60dp file tile with the PDF mark, then "Attached Document", the file name and the date.
 * Tapping opens the file's link in whatever app handles it (browser or PDF viewer).
 */
@Composable
fun AttachmentRow(attachment: CircularAttachment, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Row(
        modifier = modifier
            .clickable(enabled = attachment.url.isNotBlank()) {
                // No app may be able to open it; do nothing instead of crashing.
                runCatching { uriHandler.openUri(attachment.url) }
            }
            .padding(start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)).background(Orange),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(R.drawable.ic_pdf), contentDescription = "PDF", modifier = Modifier.size(40.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Attached Document", style = InvexType.AttachmentMeta, color = Color.Black)
            Text(attachment.fileName, style = InvexType.Body16, color = Color.Black)
            Text("Date : ${attachment.dateText}", style = InvexType.AttachmentMeta, color = Color.Black)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 560)
@Composable
private fun CircularListPreview() {
    InvexTheme { CircularListContent(sampleCirculars(), onOpen = {}) }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 560)
@Composable
private fun CircularDetailPreview() {
    InvexTheme { CircularDetailContent(sampleCirculars().first()) }
}
