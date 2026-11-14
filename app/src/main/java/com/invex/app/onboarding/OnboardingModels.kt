package com.invex.app.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.invex.app.R
import com.invex.app.ui.theme.BlueWhale
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.Orange

/**
 * One onboarding screen. [accent] colours the circle, the active indicator square and the title underline.
 * [illustrationScale] is a quick per-illustration tweak (1f = the shared 168 x 152dp box) in case one drawing
 * still looks bigger or smaller than the others on a real device.
 */
data class OnboardingPage(
    @DrawableRes val illustration: Int,
    val accent: Color,
    val title: String,
    val body: String,
    val illustrationScale: Float = 1f,
)

val onboardingPages = listOf(
    OnboardingPage(
        illustration = R.drawable.ic_onboarding_1,
        accent = BlueWhale,
        title = "Track your Exams and seats",
        body = "Stay informed about your upcoming exams, schedules, and seating arrangements.",
    ),
    OnboardingPage(
        illustration = R.drawable.ic_onboarding_2,
        accent = IconColor,
        title = "Every Exam Well Organized",
        body = "Keep exam schedules, room assignments, and invigilation duties organized in one place.",
    ),
    OnboardingPage(
        illustration = R.drawable.ic_onboarding_3,
        accent = Orange,
        title = "Your duties, right on time",
        body = "Get timely updates on invigilation assignments, room details, and important exam announcements.",
    ),
)
