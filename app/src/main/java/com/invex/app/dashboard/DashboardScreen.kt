package com.invex.app.dashboard

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.duties.DutyRecord
import com.invex.app.data.duties.DutyRecordsResult
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.R
import com.invex.app.ui.components.InvexHairline
import com.invex.app.ui.components.LineUnderlinedText
import com.invex.app.ui.theme.FloatingNavBackground
import com.invex.app.ui.theme.IconColor
import com.invex.app.ui.theme.Inter
import com.invex.app.ui.theme.InvexAccent
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.InvexType
import com.invex.app.ui.theme.LinkDark
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.Orange
import com.invex.app.ui.theme.OutlineShadow
import com.invex.app.ui.theme.PillGrey
import com.invex.app.ui.theme.PixelifySans
import com.invex.app.ui.theme.TableHeaderText
import com.invex.app.ui.theme.TableRowInteraction
import com.invex.app.broadcasts.BroadcastsSheetHost
import com.invex.app.broadcasts.BroadcastsViewModel
import com.invex.app.circulars.CircularsSheetHost
import com.invex.app.circulars.CircularsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ============================================================================
// 1. DATA MODELS
// ============================================================================

data class TodayDuty(
    /** The exam (session) this card is about. "View Your Rooms" opens its room. */
    val sessionId: String = "",
    val hallName: String = "Examination Hall A",
    val courseId: String = "Course Id : 1234567890",
    val countdownMinutes: Int = 15,
    val startTime: String = "09:00",
    val endTime: String = "10:00",
    val completedDuties: Int = 1,
    val totalDuties: Int = 2,
)

data class RoomAssignment(
    val id: String,
    val roomId: String,
)

enum class FeedType {
    SOUND,
    ALERT,
    FLAG,
}

data class FeedItem(
    val id: String,
    val type: FeedType,
    val title: String = "Heading One",
    val snippet: String = "Lorem ipsum dolor sit amet, consectetur elit..",
)

enum class DashboardNavTab(private val facultyLabel: String, @DrawableRes val iconRes: Int) {
    HOME("Home", R.drawable.ic_home),
    DUTIES("Duties", R.drawable.ic_duties_icon), // "Exams" for students: same screen, same place
    SCHEDULE("Schedule", R.drawable.ic_schedule),
    SETTINGS("Settings", R.drawable.ic_settings);

    val label: String get() = if (this == DUTIES && InvexAccent.isStudent) "Exams" else facultyLabel
}

/** The student's four services on the Dashboard, in the order of the design. */
enum class StudentService(val label: String, @DrawableRes val iconRes: Int) {
    ADMIT_CARD("Admit Card", R.drawable.ic_admit_card),
    DOS_DONTS("Do’s & Don’ts", R.drawable.ic_dos_donts),
    RESULTS("Result", R.drawable.ic_results),
    MEDICAL_LEAVE("Medical Leave", R.drawable.ic_leave),
}

data class DashboardUiState(
    val greeting: String = "Stay ready,",
    val dutySummary: String = if (InvexAccent.isStudent) "Loading your exams..." else "Loading your duties...",
    /** Set when the duties could not be loaded; shown instead of the summary, tap to retry. */
    val dutyError: String? = null,
    val assignedRooms: List<RoomAssignment> = emptyList(),
    /** The live duty, else the next one today, else the last one today. Null when there is no duty today. */
    val activeDuty: TodayDuty? = null,
    /** The latest broadcasts and circulars for the feed cards. Filled from the sheets' data in DashboardRoute. */
    val broadcasts: List<FeedItem> = emptyList(),
    val circulars: List<FeedItem> = emptyList(),
    val selectedTab: DashboardNavTab = DashboardNavTab.HOME,
    val showMedicalConfirmDialog: Boolean = false,
    val runnerDispatchedMessage: String? = null,
)

// ============================================================================
// 2. VIEWMODEL
// ============================================================================

class DashboardViewModel : ViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    private var records: List<DutyRecord> = emptyList()
    private var hasLoaded = false

    init {
        // Loads the duties now, works the countdown out again every minute without the network,
        // and quietly asks the server again every 5 minutes.
        viewModelScope.launch {
            var minute = 0
            while (true) {
                if (minute % REFRESH_EVERY_MINUTES == 0) loadDuties(showError = !hasLoaded) else applyDuties()
                delay(60_000)
                minute++
            }
        }
    }

    fun onRetryDuties() {
        viewModelScope.launch {
            _state.update { it.copy(dutyError = null, dutySummary = if (InvexAccent.isStudent) "Loading your exams..." else "Loading your duties...") }
            loadDuties(showError = true)
        }
    }

    private suspend fun loadDuties(showError: Boolean) {
        when (val result = InvexBackend.duties.loadRecords()) {
            is DutyRecordsResult.Success -> {
                records = result.records
                hasLoaded = true
                applyDuties()
            }
            is DutyRecordsResult.Failure -> if (showError) {
                _state.update { it.copy(dutyError = "${result.message} Tap here to try again.") }
            }
        }
    }

    /** Turns the loaded duties into what the Dashboard shows for "now". */
    private fun applyDuties() {
        val zone = ZoneId.systemDefault()
        val now = Instant.now()
        val today = now.atZone(zone).toLocalDate()
        val todays = records.filter { it.startsAt.atZone(zone).toLocalDate() == today }

        val summary = when (todays.size) {
            0 -> if (InvexAccent.isStudent) "You have no exam today" else "You have no exam duty today"
            else -> if (InvexAccent.isStudent) "You have ${todays.size} exam’s today" else "You have ${todays.size} exam’s duty today"
        }
        val active = todays.firstOrNull { now >= it.startsAt && now < it.endsAt }
            ?: todays.firstOrNull { now < it.startsAt }
            ?: todays.lastOrNull()
        val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

        val activeCard = active?.let { d ->
            val minutesLeft = when {
                now < d.startsAt -> Duration.between(now, d.startsAt).toMinutes()
                now < d.endsAt -> Duration.between(now, d.endsAt).toMinutes()
                else -> 0L
            }.toInt()
            TodayDuty(
                sessionId = d.sessionId,
                hallName = d.building,
                courseId = "Course Id : " + d.courseCodes.joinToString(", ").ifEmpty { "-" },
                countdownMinutes = minutesLeft,
                startTime = d.startsAt.atZone(zone).format(timeFormat),
                endTime = d.endsAt.atZone(zone).format(timeFormat),
                completedDuties = todays.count { now >= it.endsAt },
                totalDuties = todays.size,
            )
        }
        _state.update {
            it.copy(
                dutySummary = summary,
                dutyError = null,
                assignedRooms = todays.map { d -> RoomAssignment(id = d.sessionId, roomId = d.roomLabel) },
                activeDuty = activeCard,
            )
        }
    }

    private companion object {
        const val REFRESH_EVERY_MINUTES = 5
    }

    fun onTabSelected(tab: DashboardNavTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    fun onEmergencyMedicalClick() {
        _state.update { it.copy(showMedicalConfirmDialog = true) }
    }

    fun onDismissMedicalDialog() {
        _state.update { it.copy(showMedicalConfirmDialog = false) }
    }

    fun onConfirmMedicalDispatch() {
        _state.update {
            it.copy(
                showMedicalConfirmDialog = false,
                runnerDispatchedMessage = "Medical runner has been dispatched to ${it.activeDuty?.hallName ?: "your room"}!",
            )
        }
    }

    fun onDismissDispatchedBanner() {
        _state.update { it.copy(runnerDispatchedMessage = null) }
    }
}

// ============================================================================
// 3. STATEFUL ROUTE & SCREEN
// ============================================================================

@Composable
fun DashboardRoute(
    /** Gets the exam (session) id of the Today's Duty card. */
    onNavigateToRooms: (String) -> Unit,
    onNavigateToDuties: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    /** Gets the exam (session) id of the tapped room chip. */
    onNavigateToExamDetails: (String) -> Unit = {},
    /** Student only: a tap on one of the four services. */
    onNavigateToService: (StudentService) -> Unit = {},
    viewModel: DashboardViewModel = viewModel(),
    broadcastsViewModel: BroadcastsViewModel = viewModel(),
    circularsViewModel: CircularsViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    val broadcastsState by broadcastsViewModel.state.collectAsState()
    val circularsState by circularsViewModel.state.collectAsState()

    // The feed cards show the latest 3 of the same data the sheets use (loaded from the backend).
    val dashboardState = state.copy(
        broadcasts = broadcastsState.broadcasts.take(3).map { FeedItem(it.id, FeedType.SOUND, it.heading, it.snippet) },
        circulars = circularsState.circulars.take(3).map { FeedItem(it.id, FeedType.FLAG, it.heading, it.snippet) },
    )

    DashboardScreen(
        state = dashboardState,
        onTabSelected = { tab ->
            when (tab) {
                DashboardNavTab.DUTIES -> onNavigateToDuties()
                DashboardNavTab.SCHEDULE -> onNavigateToSchedule()
                DashboardNavTab.SETTINGS -> onNavigateToSettings()
                else -> viewModel.onTabSelected(tab)
            }
        },
        onRoomClick = { onNavigateToExamDetails(it.id) },
        onRetryDuties = viewModel::onRetryDuties,
        onViewRoomsClick = { state.activeDuty?.let { onNavigateToRooms(it.sessionId) } },
        onEmergencyClick = viewModel::onEmergencyMedicalClick,
        onDismissMedicalDialog = viewModel::onDismissMedicalDialog,
        onConfirmMedicalDispatch = viewModel::onConfirmMedicalDispatch,
        onDismissDispatchedBanner = viewModel::onDismissDispatchedBanner,
        onServiceClick = onNavigateToService,
        onFeedItemClick = { circularsViewModel.openDetail(it.id) },
        onCircularsTitleClick = circularsViewModel::openList,
        onBroadcastsTitleClick = broadcastsViewModel::openList,
        onBroadcastItemClick = { broadcastsViewModel.openDetail(it.id) },
    )

    // Bottom sheets over the Dashboard: broadcasts and circulars, each a list or one item.
    CircularsSheetHost(
        state = circularsState,
        onDismiss = circularsViewModel::dismiss,
        onOpenDetail = circularsViewModel::openDetail,
    )
    BroadcastsSheetHost(
        state = broadcastsState,
        onDismiss = broadcastsViewModel::dismiss,
        onOpenDetail = broadcastsViewModel::openDetail,
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onTabSelected: (DashboardNavTab) -> Unit,
    onRoomClick: (RoomAssignment) -> Unit,
    onViewRoomsClick: () -> Unit,
    onEmergencyClick: () -> Unit,
    onDismissMedicalDialog: () -> Unit,
    onConfirmMedicalDispatch: () -> Unit,
    onDismissDispatchedBanner: () -> Unit,
    onFeedItemClick: (FeedItem) -> Unit,
    modifier: Modifier = Modifier,
    onBroadcastsTitleClick: () -> Unit = {},
    onCircularsTitleClick: () -> Unit = {},
    onBroadcastItemClick: (FeedItem) -> Unit = onFeedItemClick,
    onRetryDuties: () -> Unit = {},
    onServiceClick: (StudentService) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MobileBackground),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            // 1. Hero Greeting with Briefcase Badge
            item {
                HeroGreetingSection(greeting = state.greeting)
            }

            // 2. Exam Duty Summary & Circular Room Badges
            item {
                DutyNotificationSection(
                    dutySummary = state.dutyError ?: state.dutySummary,
                    rooms = state.assignedRooms,
                    onRoomClick = onRoomClick,
                    onSummaryClick = if (state.dutyError != null) onRetryDuties else null,
                )
            }

            // 3. Today's Duty Active Card (hidden when there is no duty today)
            state.activeDuty?.let { duty ->
                item {
                    TodayDutySection(
                        duty = duty,
                        onViewRoomsClick = onViewRoomsClick,
                    )
                }
            }

            // 4. Teacher: In-Seat Assistance & Emergency Call. Student: the four services.
            item {
                if (InvexAccent.isStudent) {
                    StudentServicesSection(onServiceClick = onServiceClick)
                } else {
                    InSeatAssistanceSection(
                        onEmergencyClick = onEmergencyClick,
                    )
                }
            }

            // 5. Broadcasts Feed Card
            item {
                FeedCardSection(
                    sectionTitle = "Broadcasts",
                    items = state.broadcasts,
                    onItemClick = onBroadcastItemClick,
                    onTitleClick = onBroadcastsTitleClick,
                )
            }

            // 6. Circulars Feed Card
            item {
                FeedCardSection(
                    sectionTitle = "Circulars",
                    items = state.circulars,
                    onItemClick = onFeedItemClick,
                    onTitleClick = onCircularsTitleClick,
                )
            }
        }

        // Floating Bottom Navigation Bar (Centered Overlay)
        FloatingBottomNavBar(
            selectedTab = state.selectedTab,
            onTabSelected = onTabSelected,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )

        // Emergency Dispatch Confirmation Dialog
        if (state.showMedicalConfirmDialog) {
            AlertDialog(
                onDismissRequest = onDismissMedicalDialog,
                title = {
                    Text(
                        text = "Emergency Medical Dispatch",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                },
                text = {
                    Text(
                        text = "Do you want to immediately dispatch a medical runner to ${state.activeDuty?.hallName ?: "your room"}?",
                        fontFamily = Inter,
                        fontSize = 14.sp,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onConfirmMedicalDispatch,
                        colors = ButtonDefaults.buttonColors(containerColor = Orange),
                    ) {
                        Text("Dispatch Runner", fontFamily = Inter, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissMedicalDialog) {
                        Text("Cancel", fontFamily = Inter, color = TableHeaderText)
                    }
                },
                containerColor = TableRowInteraction,
            )
        }
    }
}

// ============================================================================
// 4. SECTION COMPOSABLES
// ============================================================================

/**
 * Hero greeting: "Stay ready," on line 1, and "your [briefcase] duty begins." on line 2.
 */
@Composable
fun HeroGreetingSection(
    greeting: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Line 1: "Stay ready,"
        Text(
            text = greeting,
            style = InvexType.DashboardGreeting,
            color = IconColor,
        )

        // Line 2: "your [briefcase] duty begins."
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "your",
                style = InvexType.DashboardGreeting,
                color = Orange,
            )

            // Inline Briefcase Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .border(BorderStroke(1.5.dp, Color.Black), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_business_briefcase),
                    contentDescription = "Briefcase icon",
                    modifier = Modifier.size(33.dp),
                )
            }

            Text(
                text = if (InvexAccent.isStudent) "exam begins." else "duty begins.",
                style = InvexType.DashboardGreeting,
                color = Orange,
            )
        }
    }
}

/**
 * Duty summary caption + circular room chips ("B - 317").
 */
@Composable
fun DutyNotificationSection(
    dutySummary: String,
    rooms: List<RoomAssignment>,
    onRoomClick: (RoomAssignment) -> Unit,
    modifier: Modifier = Modifier,
    onSummaryClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = dutySummary,
            style = InvexType.DashboardSubhead,
            color = TableHeaderText,
            modifier = if (onSummaryClick != null) Modifier.clickable(onClick = onSummaryClick) else Modifier,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            rooms.forEach { room ->
                RoomChip(
                    room = room,
                    onClick = { onRoomClick(room) },
                )
            }
        }
    }
}

/**
 * 100x100 dp circular room chip with desk illustration and room number label.
 */
@Composable
fun RoomChip(
    room: RoomAssignment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(100.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Black circular badge with classroom desk glyph
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_notification),
                    contentDescription = "Room ${room.roomId}",
                    modifier = Modifier.size(50.dp),
                )
            }

//            Spacer(Modifier.height(4.dp))

            Text(
                text = room.roomId,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = Color.Black,
            )
        }
    }
}

/**
 * Today's Duty section with underlined header and active duty card.
 */
@Composable
fun TodayDutySection(
    duty: TodayDuty,
    onViewRoomsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Section Header with orange underline
        LineUnderlinedText(
            text = if (InvexAccent.isStudent) "Today’s Exam" else "Today’s Duty",
            style = InvexType.DashboardSectionHeading,
            color = Color.Black,
            underlineColor = Orange,
            underlineOffset = 6.dp,
            thickness = 3.dp,
        )

        // Main Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(TableRowInteraction)
                .border(InvexHairline, RoundedCornerShape(15.dp))
                .padding(18.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Top Row: Hall Name & Countdown Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = duty.hallName,
                            style = InvexType.HallTitle,
                            color = Color.Black,
                        )
                        Text(
                            text = duty.courseId,
                            style = InvexType.FeedSnippet,
                            color = TableHeaderText,
                        )
                    }

                    // Countdown Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PillGrey)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "⏱ ${duty.countdownMinutes / 60}H ${duty.countdownMinutes % 60} M",
                            style = InvexType.CountdownBadge,
                            color = Color.Black,
                        )
                    }
                }

                // Timeline graphic: Start 09:00 -> [Door Icon] -> 10:00 End
                DutyTimeline(
                    startTime = duty.startTime,
                    endTime = duty.endTime,
                )

                // Duty Progress Indicator
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Two-tone progress bar
                    val fraction = (duty.completedDuties.toFloat() / duty.totalDuties.coerceAtLeast(1))
                        .coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(OutlineShadow),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Orange),
                        )
                    }

                    // Completed count with orange square swatch
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Orange),
                        )
                        Text(
                            text = "${duty.completedDuties} / ${duty.totalDuties} " + if (InvexAccent.isStudent) "Exam completed" else "Duty completed",
                            style = InvexType.FeedSnippet,
                            color = Color.Black,
                        )
                    }
                }

                // "View Your Rooms" (teacher) / "View Your Seat" (student) button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black)
                        .clickable(onClick = onViewRoomsClick)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center, // This will center whatever is inside the Box
                ) {
                    // REMOVED fillMaxWidth() so this Row shrinks to fit its children snugly
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                    ) {
                        // Left: Door glyph + Text
                        Image(
                            painter = painterResource(R.drawable.ic_door),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            colorFilter = ColorFilter.tint(Color.White),
                        )

                        Row {
                            Text(
                                text = "View Your ",
                                fontFamily = PixelifySans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White,
                            )
                            Text(
                                text = if (InvexAccent.isStudent) "Seat" else "Rooms",
                                fontFamily = PixelifySans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White,
                                textDecoration = TextDecoration.Underline,
                            )
                        }

                        Image(painterResource(R.drawable.ic_view_seat_navigation), contentDescription = null, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

/**
 * Visual timeline connecting Start (09:00) through the central door icon to End (10:00).
 */
@Composable
fun DutyTimeline(
    startTime: String,
    endTime: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Start Time
        Column(horizontalAlignment = Alignment.Start) {
            Text(text = "Start", style = InvexType.TimelineLabel, color = TableHeaderText)
            Text(text = startTime, style = InvexType.TimelineTime, color = Color.Black)
        }

        // Timeline Path
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Orange Solid Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(Orange),
            )

            // Dotted Line 1
            Canvas(modifier = Modifier.weight(1f).height(2.dp).padding(horizontal = 4.dp)) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
                )
            }

            // Door Icon in Center
            Image(
                painter = painterResource(R.drawable.ic_door),
                contentDescription = "Door",
                modifier = Modifier.size(26.dp),
                colorFilter = ColorFilter.tint(Color.Black),
            )

            // Dotted Line 2
            Canvas(modifier = Modifier.weight(1f).height(2.dp).padding(horizontal = 4.dp)) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f),
                )
            }

            // Hollow Circle Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .border(BorderStroke(2.dp, Color.Black), CircleShape),
            )
        }

        // End Time
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "End", style = InvexType.TimelineLabel, color = TableHeaderText)
            Text(text = endTime, style = InvexType.TimelineTime, color = Color.Black)
        }
    }
}

/**
 * In-Seat Assistance divider line and Emergency Medical Call action button.
 */
@Composable
fun InSeatAssistanceSection(
    onEmergencyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Divider Header: "In-Seat Assistance" ─── "Tap to Dispatch Runner"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "In-Seat Assistance",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.Black,
            )

            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                thickness = 1.dp,
                color = OutlineShadow,
            )

            Text(
                text = "Tap to Dispatch Runner",
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TableHeaderText,
            )
        }

        // Orange Emergency Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Orange)
                .clickable(onClick = onEmergencyClick),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_alert_siren),
                    contentDescription = "Emergency siren",
                    modifier = Modifier.size(28.dp),
                    colorFilter = ColorFilter.tint(Color.White),
                )

                Spacer(Modifier.width(10.dp))

                Row {
                    Text(
                        text = "Emergency ",
                        style = InvexType.EmergencyButtonLabel,
                        color = Color.White,
                    )
                    Text(
                        text = "Medical",
                        style = InvexType.EmergencyButtonLabel,
                        color = Color.White,
                        textDecoration = TextDecoration.Underline,
                    )
                    Text(
                        text = " Call",
                        style = InvexType.EmergencyButtonLabel,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * Student Dashboard: "Services" heading and the four round service buttons (64dp, the circle is part of the drawable).
 */
@Composable
fun StudentServicesSection(
    onServiceClick: (StudentService) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Services",
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = Color.Black,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            StudentService.entries.forEach { service ->
                Column(
                    modifier = Modifier
                        .widthIn(min = 64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onServiceClick(service) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Image(
                        painter = painterResource(service.iconRes),
                        contentDescription = service.label,
                        modifier = Modifier.size(64.dp),
                    )
                    Text(
                        text = service.label,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.Black,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

/**
 * Feed card section used for both "Broadcasts" and "Circulars".
 */
@Composable
fun FeedCardSection(
    sectionTitle: String,
    items: List<FeedItem>,
    onItemClick: (FeedItem) -> Unit,
    modifier: Modifier = Modifier,
    onTitleClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        LineUnderlinedText(
            modifier = if (onTitleClick != null) Modifier.clickable(onClick = onTitleClick) else Modifier,
            text = sectionTitle,
            style = InvexType.DashboardSectionHeading,
            color = Color.Black,
            underlineColor = Orange,
            underlineOffset = 6.dp,
            thickness = 3.dp,
        )

        // Card containing items with dividers
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(TableRowInteraction)
                .border(InvexHairline, RoundedCornerShape(15.dp))
                .padding(vertical = 4.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (items.isEmpty()) {
                    Text(
                        text = "Nothing here yet",
                        style = InvexType.FeedSnippet,
                        color = TableHeaderText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                items.forEachIndexed { index, item ->
                    FeedRowItem(
                        item = item,
                        onClick = { onItemClick(item) },
                    )
                    if (index < items.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = OutlineShadow,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single feed row item (sound, alert, or flag icon + title + description + "view ↗").
 */
@Composable
fun FeedRowItem(
    item: FeedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val iconRes = when (item.type) {
        FeedType.SOUND -> R.drawable.ic_sound
        FeedType.ALERT -> R.drawable.ic_alert_circle
        FeedType.FLAG -> R.drawable.ic_flag
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Leading Icon
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(Color.Black),
        )

        // Content Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                style = InvexType.FeedHeading,
                color = Color.Black,
                textDecoration = TextDecoration.Underline,
            )
            Text(
                text = item.snippet,
                style = InvexType.FeedSnippet,
                color = TableHeaderText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Action Link: "view ↗"
        Text(
            text = "view ↗",
            style = InvexType.FeedLink,
            color = LinkDark,
        )
    }
}

/**
 * Floating bottom navigation pill bar with 4 tabs and active "Duties" tab highlight.
 */
@Composable
fun FloatingBottomNavBar(
    selectedTab: DashboardNavTab,
    onTabSelected: (DashboardNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 358.dp)
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(FloatingNavBackground),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardNavTab.entries.forEach { tab ->
                    val isActive = tab == selectedTab

                    if (isActive) {
                        // Active Tab: Black rounded square container
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .clickable { onTabSelected(tab) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Image(
                                    painter = painterResource(tab.iconRes),
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(Color.White),
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    style = InvexType.NavLabel,
                                    color = Color.White,
                                )
                            }
                        }
                    } else {
                        // Inactive Tab
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clickable { onTabSelected(tab) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Image(
                                    painter = painterResource(tab.iconRes),
                                    contentDescription = tab.label,
                                    modifier = Modifier.size(24.dp),
                                    colorFilter = ColorFilter.tint(TableHeaderText),
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = tab.label,
                                    style = InvexType.NavLabel,
                                    color = TableHeaderText,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 5. PREVIEW
// ============================================================================

@Preview(showBackground = true, widthDp = 390, heightDp = 1336)
@Composable
fun DashboardScreenPreview() {
    InvexTheme {
        DashboardScreen(
            state = DashboardUiState(
                dutySummary = "You have 2 exam’s duty today",
                assignedRooms = listOf(
                    RoomAssignment("1", "B - 317"),
                    RoomAssignment("2", "B - 317"),
                ),
                activeDuty = TodayDuty(),
                broadcasts = listOf(
                    FeedItem("b1", FeedType.SOUND),
                    FeedItem("b2", FeedType.ALERT),
                    FeedItem("b3", FeedType.FLAG),
                ),
                circulars = listOf(
                    FeedItem("c1", FeedType.SOUND),
                    FeedItem("c2", FeedType.ALERT),
                    FeedItem("c3", FeedType.FLAG),
                ),
            ),
            onTabSelected = {},
            onRoomClick = {},
            onViewRoomsClick = {},
            onEmergencyClick = {},
            onDismissMedicalDialog = {},
            onConfirmMedicalDispatch = {},
            onDismissDispatchedBanner = {},
            onFeedItemClick = {},
        )
    }
}
