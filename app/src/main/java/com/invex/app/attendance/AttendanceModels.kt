package com.invex.app.attendance

import androidx.compose.ui.graphics.Color

data class AttendanceSummary(
    val total: Int = 0,
    val present: Int = 0,
    val absent: Int = 0,
)

/** Attendance of one student. [label] is shown in the small pill on the row. */
enum class AttendanceMark(val label: String) {
    Present("Present"),
    Absent("Absent"),
    NotMarked("Not marked"),
}

data class AttendanceStudent(
    val id: String,          // seat assignment id
    val name: String,
    val course: String,      // course code, e.g. "CS101"
    val rollNumber: String,  // registration number
    val seat: String,        // "Row 2, Seat 3"
    val mark: AttendanceMark,
) {
    val meta: String get() = "$course | $seat | $rollNumber"
}

/** One arc of the donut chart. Arcs are drawn clockwise from 12 o'clock, in list order. */
data class DonutSlice(val value: Float, val color: Color)
