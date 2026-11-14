package com.invex.app.supplement

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.invex.app.R
import com.invex.app.ui.components.InvexHairline
import com.invex.app.ui.components.StatusCard
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.SoftWhite
import com.invex.app.ui.theme.StatusGreen
import com.invex.app.ui.theme.StatusRed
import com.invex.app.ui.theme.StatusTeal
import com.invex.app.ui.theme.StatusYellow

/** 2x2 grid of status cards. */
@Composable
fun StatusGrid(summary: SheetSummary, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            StatusCard("Sheet Issued", summary.issued, StatusGreen, Modifier.weight(1f))
            StatusCard("Awaiting Return", summary.awaitingReturn, StatusYellow, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            StatusCard("Returned", summary.returned, StatusRed, Modifier.weight(1f))
            StatusCard("Sheets in stock", summary.inStock, StatusTeal, Modifier.weight(1f))
        }
    }
}

/** Big orange primary action. */
@Composable
fun IssueSheetButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(shape)
            .background(Orange)
            .border(InvexHairline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_circle_plus),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
        Text(text = "Issue a Supplementary Sheet", style = InvexType.ButtonLabel, color = SoftWhite)
    }
}
