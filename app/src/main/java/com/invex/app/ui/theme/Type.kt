package com.invex.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.invex.app.R

// Put these in res/font (lowercase, underscores only):
//   pixelify_sans_semibold.ttf, inter_regular.ttf, inter_medium.ttf,
//   inter_semibold.ttf, inter_bold.ttf
val PixelifySans = FontFamily(
    Font(R.font.pixelify_sans_medium, FontWeight.Medium),
    Font(R.font.pixelify_sans_semibold, FontWeight.SemiBold),
)
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

// Named after their role in the Figma frames (see SupplementSheet.md and Attendance.md, section 3).
object InvexType {
    val PageHeading = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 40.sp)
    val SectionHeading = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 32.sp)
    val SectionHeadingBold = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp)
    val CourseTitle = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 32.sp)
    val ButtonLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 32.sp)
    val CardLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 14.sp)
    val CardCount = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 24.sp)
    val LegendCaption = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 32.sp)
    val LegendLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 32.sp)
    val LegendCount = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 16.sp)
    val ChartLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 12.sp)
    val Body16 = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    // Results screen
    val Body16Regular = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp)
    val Body18 = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp)
    val Body18Regular = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 24.sp)
    val Caption12 = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
    val RowCount = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 20.sp)
    val RowMeta = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 10.sp)
    val Pill = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 8.sp, lineHeight = 8.sp)

    val Wordmark = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.SemiBold, fontSize = 64.sp, lineHeight = 64.sp)

    // Login (see Login.md, section 3). Sizes are estimated from the screenshot.
    val LoginWordmark = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.SemiBold, fontSize = 48.sp, lineHeight = 48.sp)
    val LoginSubtext = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 24.sp)
    val FieldLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp)
    val RoleLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)
    val LoginButton = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp)
    val Terms = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 16.sp)

    // Onboarding (see Onboarding.md, section 3). Letter spacing is 0 on every title (designer decision).
    val OnboardingTitle = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 40.sp)
    val OnboardingBody = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 25.sp)
    val OnboardingButton = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 25.sp)

    // Sign sheet (see the Sign the Request spec, section 4)
    val SheetHeading = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.SemiBold, fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-2).sp)
    val SignTitle = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 32.sp)
    val TabLabel = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 20.sp)
    val SignControl = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 15.sp, lineHeight = 15.sp)
    val SubLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 15.sp)
    val Consent = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 12.sp)
    val Warning = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 10.sp)
    val SheetButton = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 30.sp, letterSpacing = (-1).sp)

    // Dashboard (see Dashboard.md, section 3)
    val DashboardGreeting = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-2).sp)
    val DashboardSubhead = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 18.sp, letterSpacing = (-0.8).sp)
    val DashboardSectionHeading = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 32.sp, letterSpacing = (-1).sp)
    val HallTitle = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 24.sp)
    val CountdownBadge = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 14.sp)
    val TimelineLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
    val TimelineTime = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 26.sp)
    val EmergencyButtonLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 20.sp)
    val FeedHeading = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 16.sp)
    val FeedSnippet = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 14.sp)
    val FeedLink = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 12.sp)
    val NavLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 12.sp)

    // Room View (see Rooms.md, section 3)
    val RoomSubtitle   = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal,   fontSize = 13.sp, lineHeight = 18.sp)
    val CourseLegend   = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 20.sp)
    val SeatLabel      = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium,   fontSize = 10.sp, lineHeight = 12.sp)
    // Duties (see Duties.md). Sizes estimated from the screenshot.
    val DutiesTabLabel = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp)
    val DutyMetaLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 16.sp)
    val DutyTitle = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 24.sp)
    val DutyValue = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 16.sp)
    val DutyButton = TextStyle(fontFamily = PixelifySans, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 16.sp)

    // Exam Details (see ExamDetails.md). Sizes estimated from the screenshot.
    val ExamSectionHeading = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 32.sp)
    val ExamSmallLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, lineHeight = 12.sp)
    val ExamStatLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 16.sp)
    val ExamStatValue = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp)
    val ServiceLabel = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 16.sp)
    val ExamButton = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 24.sp)

    // Verify Admit Card (see VerifyAdmitCard.md)
    val VerifyTab = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp)
    val ScanHint = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
    val ScanButton = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp)

    // Schedule (see Schedule.md)
    val ScheduleDayName = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp)
    val ScheduleDayNumber = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.sp)
    val ScheduleHour = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 16.sp)
    val ScheduleEvent = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)

    // Settings (see Settings.md)
    val SettingsRow = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 24.sp)
    val LogOut = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)

    // Account Information (see AccountInformation.md)
    val Breadcrumb = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 16.sp)

    // Notifications (see Notifications.md)
    val NotifSubtitle = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
    val NotifHeading = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp)
    val NotifEmailText = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)
    val OptionTitle = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp)
    val OptionDesc = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 26.sp)

    // Broadcasts sheets (see Broadcasts.md)
    val BroadcastHeading = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp)
    val BroadcastText = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 22.sp)
    val AttachmentMeta = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 12.sp)

    // Restroom Pass (see RestroomPass.md, section 3)
    val RestroomCountdown = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 18.sp)
    val RestroomFieldHeader = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
    val RestroomFieldValue = TextStyle(fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp)
    val RestroomSmallMeta = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 9.sp, lineHeight = 12.sp)
    val RestroomMicro = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Normal, fontSize = 8.sp, lineHeight = 10.sp)
    val RestroomReportButton = TextStyle(fontFamily = Inter, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 12.sp)
}

// Material 3 base typography is left at defaults on purpose; the screens use InvexType.
val Typography = Typography()
