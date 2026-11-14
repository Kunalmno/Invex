package com.invex.app.examdetails

import androidx.annotation.DrawableRes
import com.invex.app.R

data class ExamCourse(
    val id: String,
    val label: String,      // small line above the name: the course code, e.g. "CS101"
    val name: String,
    val students: Int,
)

/** [division] is null for rooms that are not split into divisions. */
data class RoomInfo(
    val block: String,
    val room: String,
    val division: String? = null,
)

/** The four service buttons. Labels use the corrected spelling ("Supplement Sheet"). */
enum class ExamService(val label: String, @DrawableRes val iconRes: Int) {
    VERIFY_ADMIT_CARD("Verify\nAdmit Card", R.drawable.ic_service_admit_card),
    SUPPLEMENT_SHEET("Supplement\nSheet", R.drawable.ic_service_supplement_sheet),
    RESTROOM_PASS("Restroom\nPass", R.drawable.ic_service_restroom_pass),
    ATTENDANCE("Attendance", R.drawable.ic_service_attendance),
}

data class ExamDetailsUiState(
    val courses: List<ExamCourse> = emptyList(),
    /** Null until the exam is loaded. */
    val room: RoomInfo? = null,
    /** Student only: shown as three cards instead of the courses. */
    val building: String? = null,
    val date: String? = null,
    val time: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)
