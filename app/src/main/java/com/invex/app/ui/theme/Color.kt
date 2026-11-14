package com.invex.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Figma variables
val MobileBackground = Color(0xFFEBEBEB)
val OutlineShadow = Color(0xFFE8E8E8)
val HoverSelect = Color(0xFFF5F5F5)
val TableRowInteraction = Color(0xFFFAFAFA)

/**
 * The accent colour of the signed-in role: orange for faculty, blue for students.
 * [Orange] keeps its old name so no screen has to change; it simply returns the accent of whoever is
 * logged in. It is Compose state, so screens redraw if it changes. Set at login, at session restore
 * and cleared at log out (see [InvexAccent]).
 */
object InvexAccent {
    val FacultyOrange = Color(0xFFED451F)
    val StudentBlue = Color(0xFF0070BB)

    var current by mutableStateOf(FacultyOrange)
        private set

    val isStudent: Boolean get() = current == StudentBlue

    /** [roleDbValue] is "faculty", "student" or "admin" (the value stored in the database). */
    fun setForRole(roleDbValue: String?) {
        current = if (roleDbValue == "student") StudentBlue else FacultyOrange
    }

    fun reset() {
        current = FacultyOrange
    }
}

val Orange: Color get() = InvexAccent.current
val SoftWhite = Color(0xFFF7F9F7)
val IconColor = Color(0xFF171717)
val LightBrown = Color(0xFF6E473B)
val Mist = Color(0xFFBEB5A9)
val PaleBrown = Color(0xFFA78D78)
val BlueWhale = Color(0xFF0070BB)

// Hard-coded in Figma (not variables)
val StatusGreen = Color(0xFF66CD4C)
val StatusYellow = Color(0xFFFFDE7C)
val StatusRed = Color(0xFFF24E51)
val StatusTeal = Color(0xFF4EF2D1)
val PillGrey = Color(0xFFD9D9D9)
val SectionHeadingColor = Color(0xFF040404)
val CardLabelColor = Color(0xFF0D0D0D)
val LegendGrey = Color(0xFFD2D1D1)
val ChartLabelGrey = Color(0xFFB4B4B4)
val IndicatorInactive = Color(0xFFECECEC)

// Sign sheet. ComboText and TableHeaderText are Figma variables; the rest are hard-coded in Figma.
val ComboText = Color(0xFF0A0A0A)
val TableHeaderText = Color(0xFF737373)
val TabBlue = BlueWhale
val SignatureBoxGrey = Color(0xFFEAEAEA)
val WarningRed = StatusRed // one red for the whole app: same as the status dots
// Baseline = TableHeaderText grey (designer). Off-switch = PillGrey grey.
val SignatureLine = Color(0xFF737373)
val SwitchOffTrack = Color(0xFFD9D9D9)
val FloatingNavBackground = Color(0xFFCCCCCC)
val LinkDark = Color(0xFF151515)

// Duties screen tokens
val DutyCardBackground = Color(0xFFFFFFFF)   // white card fill
val TabActiveUnderline = Orange              // reuses Orange token

// Room View tokens
val SeatTileBackground = Color(0xFFFDDDD8)   // pinkish seat tile fill
val CourseLegendGreen  = Color(0xFF1A6B3A)   // sofa icon tint inside course legend cards

// Restroom Pass tokens
val TicketBorder = Color(0xFFB2AFAF)
val TimeRemainingBg = Color(0xFFF7EFEB)
val ReportPillBg = Color(0xFFBDB6B3)
val ReportPillText = Color(0xFF333333)
val ActiveBadgeBg = Color(0xFF151515)

// Account Information form fields (estimated from the screenshot)
val FieldFill = Color(0xFFEEEEEE)
val FieldBorder = Color(0xFFC9C9C9)
