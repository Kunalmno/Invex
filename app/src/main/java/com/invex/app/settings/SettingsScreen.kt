package com.invex.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.invex.app.dashboard.DashboardNavTab
import com.invex.app.dashboard.FloatingBottomNavBar
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.MobileBackground

/**
 * Settings has no state of its own yet (no toggles), so there is no ViewModel; the screen only reports taps.
 * [showBottomBar]: the Figma frame has no bottom bar, but every other tab screen does and Settings is a tab.
 */
@Composable
fun SettingsScreen(
    onItemClick: (SettingsItem) -> Unit,
    onLogOut: () -> Unit,
    onNavTab: (DashboardNavTab) -> Unit,
    showBottomBar: Boolean = true,
) {
    Box(Modifier.fillMaxSize().background(MobileBackground)) {
        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(top = 24.dp),
        ) {
            SettingsHeader(modifier = Modifier.padding(horizontal = 16.dp))
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SettingsItem.entries.forEach { item ->
                    SettingsRow(item = item, onClick = { onItemClick(item) })
                }
            }
            LogOutText(onClick = onLogOut, modifier = Modifier.padding(start = 8.dp, top = 36.dp))
        }
        if (showBottomBar) {
            FloatingBottomNavBar(
                selectedTab = DashboardNavTab.SETTINGS,
                onTabSelected = onNavTab,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SettingsPreview() {
    InvexTheme { SettingsScreen(onItemClick = {}, onLogOut = {}, onNavTab = {}) }
}
