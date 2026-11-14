package com.invex.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.invex.app.attendance.AttendanceRoute
import com.invex.app.data.InvexBackend
import com.invex.app.login.UserRole
import com.invex.app.ui.theme.InvexAccent
import com.invex.app.progress.ProgressBarView
import kotlinx.coroutines.launch
import com.invex.app.dashboard.DashboardNavTab
import com.invex.app.dashboard.DashboardRoute
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.invex.app.duties.DutiesRoute
import com.invex.app.accountinfo.AccountInfoRoute
import com.invex.app.notifications.NotificationsRoute
import com.invex.app.schedule.ScheduleRoute
import com.invex.app.settings.SettingsItem
import com.invex.app.settings.SettingsScreen
import com.invex.app.examdetails.ExamDetailsRoute
import com.invex.app.examdetails.ExamService
import com.invex.app.verifyadmit.VerifyAdmitRoute
import com.invex.app.login.LoginRoute
import com.invex.app.register.RegisterRoute
import com.invex.app.restroom.RestroomPassRoute
import com.invex.app.admitcard.AdmitCardRoute
import com.invex.app.dosdonts.DosDontsRoute
import com.invex.app.medicalleave.MedicalLeaveRoute
import com.invex.app.results.ResultsRoute
import com.invex.app.rooms.RoomsRoute
import com.invex.app.dashboard.StudentService
import com.invex.app.onboarding.OnboardingScreen
import com.invex.app.supplement.SupplementSheetRoute

object Route {
    /** Exam Details for one exam (session). The id comes from the Dashboard room chip. */
    fun examDetails(sessionId: String) = "exam_details/$sessionId"

    /** Attendance of one exam (session). */
    fun attendance(sessionId: String) = "attendance/$sessionId"

    /** Supplement sheets of one exam (session). */
    fun supplement(sessionId: String) = "supplement/$sessionId"

    /** Restroom passes of one exam (session). */
    fun restroomPass(sessionId: String) = "restroom_pass/$sessionId"

    /** Seat layout of one exam (session). */
    fun roomView(sessionId: String) = "room_view/$sessionId"

    const val Onboarding  = "onboarding"
    const val Login       = "login"
    const val Register    = "register"
    const val Dashboard   = "dashboard"
    const val Duties      = "duties"
    const val Results     = "results"
    const val MedicalLeave = "medical_leave"
    const val AdmitCard   = "admit_card"
    const val DosDonts    = "dos_donts"
    const val Schedule    = "schedule"
    const val Settings    = "settings"
    const val AccountInfo = "account_info"
    const val Notifications = "notifications"
    const val ExamDetails = "exam_details/{sessionId}"
    const val VerifyAdmit = "verify_admit"
    const val Attendance  = "attendance/{sessionId}"
    const val Supplement  = "supplement/{sessionId}"
    const val RestroomPass = "restroom_pass/{sessionId}"
    const val RoomView    = "room_view/{sessionId}"
}

/**
 * App navigation. Onboarding (Skip or Start) always goes to Login, and Onboarding is removed from the
 * back stack so Back from Login leaves the app instead of returning to the intro.
 *
 * Needs: implementation("androidx.navigation:navigation-compose:<version>")
 */
@Composable
fun InvexApp(
    onExit: () -> Unit = {},
    /** Pass a route to preview one screen; leave null to decide from the saved login. */
    startDestination: String? = null,
) {
    // Saved login (faculty) -> straight to the Dashboard. Otherwise Onboarding -> Login.
    var start by remember { mutableStateOf(startDestination) }
    LaunchedEffect(Unit) {
        if (start == null) {
            val profile = InvexBackend.auth.restoreSession()
            InvexAccent.setForRole(profile?.role)
            val hasScreens = profile?.role == UserRole.FACULTY.dbValue || profile?.role == UserRole.STUDENT.dbValue
            start = if (hasScreens) Route.Dashboard else Route.Onboarding
        }
    }
    val firstRoute = start
    if (firstRoute == null) {
        ProgressBarView(progress = 0.5f) // checking the saved login
        return
    }

    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    NavHost(
        navController = nav,
        startDestination = firstRoute,
        modifier = Modifier,
    ) {
        composable(Route.Onboarding) {
            OnboardingScreen(
                onFinished = {
                    nav.navigate(Route.Login) {
                        popUpTo(Route.Onboarding) { inclusive = true }
                    }
                },
            )
        }
        composable(Route.Login) {
            LoginRoute(
                onLoggedIn = {
                    nav.navigate(Route.Dashboard) {
                        popUpTo(Route.Login) { inclusive = true }
                    }
                },
                onSignUp = { nav.navigate(Route.Register) },
            )
        }
        composable(Route.Register) {
            RegisterRoute(
                onRegistered = {
                    nav.navigate(Route.Dashboard) {
                        popUpTo(Route.Register) { inclusive = true }
                    }
                },
                onSignIn = { nav.popBackStack() },
            )
        }
        composable(Route.Dashboard) {
            DashboardRoute(
                onNavigateToRooms = { sessionId -> nav.navigate(Route.roomView(sessionId)) },
                onNavigateToDuties = { nav.navigate(Route.Duties) },
                onNavigateToSchedule = { nav.navigate(Route.Schedule) },
                onNavigateToSettings = { nav.navigate(Route.Settings) },
                onNavigateToExamDetails = { sessionId -> nav.navigate(Route.examDetails(sessionId)) },
                onNavigateToService = { service ->
                    when (service) {
                        StudentService.RESULTS -> nav.navigate(Route.Results)
                        StudentService.MEDICAL_LEAVE -> nav.navigate(Route.MedicalLeave)
                        StudentService.ADMIT_CARD -> nav.navigate(Route.AdmitCard)
                        StudentService.DOS_DONTS -> nav.navigate(Route.DosDonts)
                    }
                },
            )
        }
        composable(Route.Results) { ResultsRoute(onBack = { nav.popBackStack() }) }
        composable(Route.DosDonts) { DosDontsRoute(onBack = { nav.popBackStack() }) }
        composable(Route.AdmitCard) { AdmitCardRoute(onBack = { nav.popBackStack() }) }
        composable(Route.MedicalLeave) { MedicalLeaveRoute(onBack = { nav.popBackStack() }) }
        composable(
            route = Route.ExamDetails,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { entry ->
            val sessionId = entry.arguments?.getString("sessionId").orEmpty()
            ExamDetailsRoute(
                onBack = { nav.popBackStack() },
                onProceed = { nav.navigate(Route.roomView(sessionId)) },
                onService = { service ->
                    when (service) {
                        ExamService.SUPPLEMENT_SHEET -> nav.navigate(Route.supplement(sessionId))
                        ExamService.RESTROOM_PASS -> nav.navigate(Route.restroomPass(sessionId))
                        ExamService.ATTENDANCE -> nav.navigate(Route.attendance(sessionId))
                        ExamService.VERIFY_ADMIT_CARD -> nav.navigate(Route.VerifyAdmit)
                    }
                },
            )
        }
        composable(Route.VerifyAdmit) { VerifyAdmitRoute(onBack = { nav.popBackStack() }) }
        composable(Route.Settings) {
            SettingsScreen(
                onItemClick = { item ->
                    when (item) {
                        SettingsItem.ACCOUNT_INFORMATION -> nav.navigate(Route.AccountInfo)
                        SettingsItem.NOTIFICATIONS -> nav.navigate(Route.Notifications)
                    }
                },
                onLogOut = {
                    scope.launch {
                        InvexBackend.auth.logout()
                        InvexAccent.reset()
                        // Clear everything so Back cannot return to a logged-in screen.
                        nav.navigate(Route.Login) { popUpTo(0) { inclusive = true } }
                    }
                },
                onNavTab = { tab -> nav.goToTab(tab, current = DashboardNavTab.SETTINGS) },
            )
        }
        composable(Route.Notifications) { NotificationsRoute(onBack = { nav.popBackStack() }) }
        composable(Route.AccountInfo) { AccountInfoRoute(onBack = { nav.popBackStack() }) }
        composable(Route.Schedule) {
            ScheduleRoute(
                onBack = { nav.popBackStack() },
                onNavTab = { tab -> nav.goToTab(tab, current = DashboardNavTab.SCHEDULE) },
            )
        }
        composable(Route.Duties) {
            DutiesRoute(
                onBack = { nav.popBackStack() },
                onViewRoomSeating = { duty -> nav.navigate(Route.roomView(duty.id)) },
                onNavTab = { tab -> nav.goToTab(tab, current = DashboardNavTab.DUTIES) },
            )
        }
        composable(
            route = Route.Attendance,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { AttendanceRoute(onBack = { nav.popBackStack() }) }
        composable(
            route = Route.Supplement,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { SupplementSheetRoute(onBack = { nav.popBackStack() }) }
        composable(
            route = Route.RestroomPass,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { RestroomPassRoute(onBack = { nav.popBackStack() }) }
        composable(
            route = Route.RoomView,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType }),
        ) { RoomsRoute(onBack = { nav.popBackStack() }) }
    }
}

/** Bottom bar navigation shared by the tab screens. Keeps the back stack flat: Dashboard, then one tab screen. */
private fun NavController.goToTab(tab: DashboardNavTab, current: DashboardNavTab) {
    if (tab == current) return
    when (tab) {
        DashboardNavTab.HOME -> popBackStack(Route.Dashboard, inclusive = false)
        DashboardNavTab.DUTIES -> navigate(Route.Duties) {
            popUpTo(Route.Dashboard) { inclusive = false }
            launchSingleTop = true
        }
        DashboardNavTab.SCHEDULE -> navigate(Route.Schedule) {
            popUpTo(Route.Dashboard) { inclusive = false }
            launchSingleTop = true
        }
        DashboardNavTab.SETTINGS -> navigate(Route.Settings) {
            popUpTo(Route.Dashboard) { inclusive = false }
            launchSingleTop = true
        }
    }
}
