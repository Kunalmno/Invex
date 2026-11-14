package com.invex.app.notifications

/** Defaults match the design: email notification on, the three options not ticked. */
data class NotificationSettings(
    val emailEnabled: Boolean = true,
    val newExamsAndUpdates: Boolean = false,
    val notificationChannels: Boolean = false,
    val feedbackNotifications: Boolean = false,
)
