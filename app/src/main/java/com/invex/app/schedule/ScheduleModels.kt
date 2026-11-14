package com.invex.app.schedule

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * One block on the day timeline. Hours are 24h clock whole hours: 11 to 12 is "11:00 AM to 12:00 PM".
 * An exam that starts or ends inside an hour covers that whole hour (10:30 - 13:15 -> 10 to 14).
 */
data class ScheduleEvent(
    val id: String,
    val title: String,
    val startHour: Int,
    val endHour: Int,
    val date: LocalDate = LocalDate.now(),
)

data class ScheduleUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    /** First day (Monday) of the week shown in the date strip. */
    val weekStart: LocalDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
    /** Every duty (teacher) or exam (student) of the signed-in user, all days. */
    val allEvents: List<ScheduleEvent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
) {
    val weekDays: List<LocalDate> get() = (0..6).map { weekStart.plusDays(it.toLong()) }

    /** The events of the selected day. */
    val events: List<ScheduleEvent> get() = allEvents.filter { it.date == selectedDate }

    /** The timeline shows at least 09:00 to 16:00 and grows when an event is earlier or later. */
    val firstHour: Int get() = minOf(DEFAULT_FIRST_HOUR, events.minOfOrNull { it.startHour } ?: DEFAULT_FIRST_HOUR)
    val lastHour: Int get() = maxOf(DEFAULT_LAST_HOUR, events.maxOfOrNull { it.endHour } ?: DEFAULT_LAST_HOUR)
    val hours: List<Int> get() = (firstHour..lastHour).toList()

    private companion object {
        const val DEFAULT_FIRST_HOUR = 9
        const val DEFAULT_LAST_HOUR = 16
    }
}
