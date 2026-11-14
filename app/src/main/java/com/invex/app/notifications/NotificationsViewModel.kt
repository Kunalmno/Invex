package com.invex.app.notifications

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NotificationsViewModel : ViewModel() {

    // TODO: load the saved settings from the backend.
    private var saved = NotificationSettings()

    private val _state = MutableStateFlow(saved)
    val state: StateFlow<NotificationSettings> = _state.asStateFlow()

    fun onEmailChange(on: Boolean) = _state.update { it.copy(emailEnabled = on) }
    fun onNewExamsChange(on: Boolean) = _state.update { it.copy(newExamsAndUpdates = on) }
    fun onChannelsChange(on: Boolean) = _state.update { it.copy(notificationChannels = on) }
    fun onFeedbackChange(on: Boolean) = _state.update { it.copy(feedbackNotifications = on) }

    /** SAVE keeps the choices. TODO: send them to the backend. */
    fun save() {
        saved = _state.value
    }

    /** CANCEL drops unsaved changes. */
    fun cancel() {
        _state.value = saved
    }
}
