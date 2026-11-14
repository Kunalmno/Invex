package com.invex.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite

/**
 * Shared pieces of the information sheets that open over the Dashboard (Broadcasts and Circulars):
 * the sheet itself, its title, its scrolling body and a tappable list row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoModalSheet(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SoftWhite,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) { content() }
}

/** Pixel-font title with the orange underline. */
@Composable
fun SheetTitle(text: String) {
    LineUnderlinedText(
        text = text,
        style = InvexType.PageHeading,
        color = IconColor,
        underlineColor = Orange,
        underlineOffset = 4.dp,
        thickness = 4.dp,
    )
}

/** The sheet is 58% of the screen high (491 of 844 in the design) and scrolls if the content is longer. */
@Composable
fun SheetBody(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.58f)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

/** Heading, sub line, and a snippet line with an arrow at the end. The whole row is tappable. */
@Composable
fun SheetListItem(
    heading: String,
    subheading: String,
    snippet: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 8.dp, top = 20.dp, bottom = 16.dp),
    ) {
        Text(heading, style = InvexType.BroadcastHeading, color = Color.Black)
        Text(subheading, style = InvexType.BroadcastText, color = Color.Black)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(snippet, style = InvexType.BroadcastText, color = Color.Black, modifier = Modifier.weight(1f))
            Image(painterResource(R.drawable.ic_arrow_over), contentDescription = "Open", modifier = Modifier.size(24.dp))
        }
    }
}
