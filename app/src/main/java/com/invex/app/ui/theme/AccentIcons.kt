package com.invex.app.ui.theme

import androidx.annotation.DrawableRes
import com.invex.app.R

/**
 * Some icons have the orange circle drawn inside the file. For students they are swapped for a blue copy
 * (same file name + "_student"). Icons that only the teacher app uses are not listed.
 * Use it like: painterResource(accentIcon(R.drawable.ic_exam_details)).
 */
@DrawableRes
fun accentIcon(@DrawableRes faculty: Int): Int =
    if (InvexAccent.isStudent) studentIcons[faculty] ?: faculty else faculty

private val studentIcons: Map<Int, Int> = mapOf(
    R.drawable.ic_exam_details to R.drawable.ic_exam_details_student,
    R.drawable.ic_room_heading to R.drawable.ic_room_heading_student,
    R.drawable.ic_notification_bell to R.drawable.ic_notification_bell_student,
    R.drawable.ic_schedule_heading to R.drawable.ic_schedule_heading_student,
    R.drawable.ic_admit_card_heading to R.drawable.ic_admit_card_heading_student,
)
