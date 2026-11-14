package com.invex.app.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.accountinfo.AccountActionButtons
import com.invex.app.ui.components.SettingsSubHeader
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.TableHeaderText

/** SAVE keeps the choices and goes back; CANCEL drops them and goes back. */
@Composable
fun NotificationsRoute(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    NotificationsScreen(
        state = state,
        onBack = onBack,
        onEmailChange = viewModel::onEmailChange,
        onNewExamsChange = viewModel::onNewExamsChange,
        onChannelsChange = viewModel::onChannelsChange,
        onFeedbackChange = viewModel::onFeedbackChange,
        onSave = {
            viewModel.save()
            onBack()
        },
        onCancel = {
            viewModel.cancel()
            onBack()
        },
    )
}

@Composable
fun NotificationsScreen(
    state: NotificationSettings,
    onBack: () -> Unit,
    onEmailChange: (Boolean) -> Unit,
    onNewExamsChange: (Boolean) -> Unit,
    onChannelsChange: (Boolean) -> Unit,
    onFeedbackChange: (Boolean) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SettingsSubHeader(title = "Notifications", onBack = onBack)
        Text(
            "Get notification about exams, you can turn that off any time.",
            style = InvexType.NotifSubtitle,
            color = TableHeaderText,
        )
        EmailNotificationSection(enabled = state.emailEnabled, onEnabledChange = onEmailChange)
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            NotificationOption(
                title = "New Exams and Updates Setting",
                description = "The latest info about the new exams, significant changes in exam schedule",
                checked = state.newExamsAndUpdates,
                onCheckedChange = onNewExamsChange,
            )
            NotificationOption(
                title = "Notification Channels",
                description = "Choose Preferred notification channels, including sms, mobile app, to receive updates conveniently.",
                checked = state.notificationChannels,
                onCheckedChange = onChannelsChange,
            )
            NotificationOption(
                title = "Feedback Notifications",
                description = "Receive notifications for app feedback, reviews or surveys to improvise user experience.",
                checked = state.feedbackNotifications,
                onCheckedChange = onFeedbackChange,
            )
        }
        AccountActionButtons(onSave = onSave, onCancel = onCancel)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun NotificationsPreview() {
    InvexTheme {
        NotificationsScreen(NotificationSettings(), {}, {}, {}, {}, {}, {}, {})
    }
}
