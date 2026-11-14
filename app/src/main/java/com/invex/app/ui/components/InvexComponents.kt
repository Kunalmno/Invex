package com.invex.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.theme.CardLabelColor
import com.invex.app.ui.theme.HoverSelect
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.OutlineShadow
import com.invex.app.ui.theme.PillGrey
import com.invex.app.ui.theme.TableRowInteraction

internal val InvexHairline = BorderStroke(1.dp, OutlineShadow)

/**
 * Extra breathing room between two top-level sections of a LazyColumn.
 * The screens use spacedBy(12.dp) between items; this empty item makes the gap 24dp
 * (12 + 0 + 12), which is the gap between sections in the Figma frames.
 */
fun LazyListScope.sectionGap() {
    item { Spacer(Modifier.height(0.dp)) }
}

/**
 * Chevron + orange icon circle + underlined pixel-font title.
 * [glyphPadding] positions the white glyph inside the 40dp circle (it differs per screen).
 */
@Composable
fun ScreenHeader(
    title: String,
    @DrawableRes glyphRes: Int,
    glyphPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_chevron_back),
            contentDescription = "Back",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Orange),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(glyphRes),
                    contentDescription = null,
                    modifier = Modifier.padding(glyphPadding),
                )
            }
            Text(
                text = title,
                style = InvexType.PageHeading,
                color = IconColor,
                textDecoration = TextDecoration.Underline,
            )
        }
    }
}

/** Count card: status dot, label, big number. Width comes from [modifier]; height is fixed at 70dp. */
@Composable
fun StatusCard(label: String, count: Int, dotColor: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(shape)
            .background(HoverSelect)
            .border(InvexHairline, shape),
    ) {
        Box(
            modifier = Modifier
                .padding(start = 16.dp, top = 8.dp)
                .size(12.dp)
                .background(dotColor, RoundedCornerShape(4.dp))
                .border(1.dp, Color.Black, RoundedCornerShape(4.dp)),
        )
        Text(
            text = label,
            style = InvexType.CardLabel,
            color = CardLabelColor,
            modifier = Modifier.padding(start = 32.dp, top = 8.dp),
        )
        Text(
            text = "%02d".format(count),
            style = InvexType.CardCount,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp, top = 32.dp),
        )
    }
}

/** Search field built from BasicTextField so Material's default TextField padding/height can't interfere. */
@Composable
fun InvexSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(TableRowInteraction)
            .border(InvexHairline, shape)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = "Search",
            modifier = Modifier.size(32.dp),
        )
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text("Search for the students", style = InvexType.Body16, color = Color.Black)
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = InvexType.Body16.copy(color = Color.Black),
                cursorBrush = SolidColor(Color.Black),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** One student row: name + meta on the left, count + status pill on the right. */
@Composable
fun StudentRow(
    name: String,
    meta: String,
    // Null hides the number (rows where a count means nothing, like Attendance).
    count: Int?,
    tag: String,
    modifier: Modifier = Modifier,
    // Null = display-only row (the "Sheets Issued" and "Students" lists). Set only where a row is selectable.
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(TableRowInteraction)
            .border(InvexHairline, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Text(
            text = name,
            style = InvexType.Body16,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
        )
        Text(
            text = meta,
            style = InvexType.RowMeta,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp, top = 36.dp),
        )
        if (count != null) {
            Text(
                text = "%02d".format(count),
                style = InvexType.RowCount,
                color = Color.Black,
                modifier = Modifier.align(Alignment.TopEnd).padding(end = 16.dp, top = 8.dp),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 32.dp)
                .size(width = 60.dp, height = 16.dp)
                .background(PillGrey, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = tag, style = InvexType.Pill, color = Color.Black)
        }
    }
}
