package com.invex.app.dosdonts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.R
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.components.ServiceHeader
import com.invex.app.ui.components.StatusMessage
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.TableHeaderText

@Composable
fun DosDontsRoute(
    onBack: () -> Unit,
    viewModel: DosDontsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    DosDontsScreen(state = state, onBack = onBack, onRetry = viewModel::refresh)
}

@Composable
fun DosDontsScreen(
    state: DosDontsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ServiceHeader(title = "Do’s and Don’ts", iconRes = R.drawable.ic_dos_donts, onBack = onBack)
        when {
            state.isLoading -> StatusMessage("Loading...")
            state.error != null -> StatusMessage(state.error, actionLabel = "Try again", onAction = onRetry)
            state.dos.isEmpty() && state.donts.isEmpty() -> StatusMessage("Nothing has been added yet")
            else -> {
                if (state.dos.isNotEmpty()) TipSection("DO", state.dos)
                if (state.donts.isNotEmpty()) TipSection("DON’T", state.donts)
                Text(
                    text = "Invex uses basic RAG to rate topics, and can make mistakes.",
                    style = InvexType.Caption12,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun TipSection(title: String, groups: List<TipGroup>) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        LineUnderlinedText(
            text = title,
            style = InvexType.Body18,
            color = Color.Black,
            underlineColor = Color.Black,
            underlineOffset = 2.dp,
            thickness = 2.dp,
        )
        groups.forEach { group ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(group.unit, style = InvexType.Body16, color = Color.Black)
                group.topics.forEach { topic ->
                    Column {
                        Text(topic.heading, style = InvexType.Body16Regular, color = Color.Black, modifier = Modifier.padding(bottom = 4.dp))
                        topic.tips.forEach { tip -> TipRow(tip) }
                    }
                }
            }
        }
    }
}

/** Sub topic on the left, one blue star per rating point on the right, a black line below. */
@Composable
private fun TipRow(tip: Tip) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(tip.subTopic, style = InvexType.Body18, color = Color.Black, modifier = Modifier.weight(1f).padding(end = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(end = 32.dp)) {
                repeat(tip.rating) {
                    Image(painterResource(R.drawable.ic_star), contentDescription = null, modifier = Modifier.size(width = 18.dp, height = 17.dp))
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Color.Black)
    }
}
