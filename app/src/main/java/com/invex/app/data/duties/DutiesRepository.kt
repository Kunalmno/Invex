package com.invex.app.data.duties

import com.invex.app.duties.DutyItem
import com.invex.app.ui.theme.InvexAccent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** One row of the `duty_overview` view (only the columns the Duties screen needs). */
@Serializable
private data class DutyRow(
    @SerialName("session_id") val sessionId: String,
    val building: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    val phase: String, // "upcoming" or "completed", decided by the database
)

/** One duty with real times, for the Dashboard (it works out "today", the live duty and the countdown itself). */
data class DutyRecord(
    val sessionId: String,
    val building: String,
    /** Shown on the room chip: "B - 317", or "B - 317 - A" when the room has a division. */
    val roomLabel: String,
    val courseCodes: List<String>,
    val startsAt: Instant,
    val endsAt: Instant,
)

sealed interface DutyRecordsResult {
    data class Success(val records: List<DutyRecord>) : DutyRecordsResult
    data class Failure(val message: String) : DutyRecordsResult
}

@Serializable
private data class DutyRecordRow(
    @SerialName("session_id") val sessionId: String,
    val building: String,
    val block: String,
    @SerialName("room_number") val roomNumber: String,
    val division: String? = null, // only for rooms split into divisions
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
    @SerialName("course_code") val courseCode: String? = null, // student view only: the student's own course
)

@Serializable
private data class SessionCourseRow(
    @SerialName("session_id") val sessionId: String,
    val code: String,
)

sealed interface DutiesResult {
    data class Success(val upcoming: List<DutyItem>, val completed: List<DutyItem>) : DutiesResult
    data class Failure(val message: String) : DutiesResult
}

/**
 * Reads the signed-in user's duties (teacher) or exams (student). Same screens, different view:
 *   teacher: `duty_overview` filtered by faculty_id, student: `student_exam_overview` filtered by student_id.
 * The database only returns rows that belong to the user (row level security); the filter is a second safety net.
 */
class DutiesRepository(private val client: SupabaseClient) {

    private val isStudent get() = InvexAccent.isStudent
    private val overview get() = if (isStudent) "student_exam_overview" else "duty_overview"
    private val ownerColumn get() = if (isStudent) "student_id" else "faculty_id"
    private val noun get() = if (isStudent) "exams" else "duties"

    suspend fun load(): DutiesResult {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return DutiesResult.Failure("You are logged out. Please log in again.")
        return try {
            val rows = client.from(overview)
                .select(Columns.list("session_id", "building", "starts_at", "ends_at", "phase")) {
                    filter { eq(ownerColumn, userId) }
                }
                .decodeList<DutyRow>()
            val zone = ZoneId.systemDefault()
            fun DutyRow.toItem(): DutyItem {
                val start = OffsetDateTime.parse(startsAt).atZoneSameInstant(zone)
                val end = OffsetDateTime.parse(endsAt).atZoneSameInstant(zone)
                return DutyItem(
                    id = sessionId,
                    building = building,
                    date = start.format(DATE_FORMAT),
                    timeRange = "${start.format(TIME_FORMAT)} - ${end.format(TIME_FORMAT)}",
                    isCompleted = phase == "completed",
                )
            }
            val (done, coming) = rows.partition { it.phase == "completed" }
            DutiesResult.Success(
                upcoming = coming.sortedBy { it.startsAt }.map { it.toItem() },
                completed = done.sortedByDescending { it.startsAt }.map { it.toItem() },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DutiesResult.Failure("Could not load your $noun. Check your internet connection and try again.")
        }
    }

    /** All duties of the signed-in teacher, oldest first, with their room label and course codes. */
    suspend fun loadRecords(): DutyRecordsResult {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return DutyRecordsResult.Failure("You are logged out. Please log in again.")
        return try {
            val columns = mutableListOf("session_id", "building", "block", "room_number", "division", "starts_at", "ends_at")
            if (isStudent) columns += "course_code"
            val rows = client.from(overview)
                .select(Columns.list(*columns.toTypedArray())) {
                    filter { eq(ownerColumn, userId) }
                }
                .decodeList<DutyRecordRow>()
            // Course codes are a nice extra: if this second call fails, the duties still show.
            val codes: Map<String, List<String>> = if (rows.isEmpty()) emptyMap() else if (isStudent) {
                // A student only sees the course they sit (it comes with the row).
                rows.associate { it.sessionId to listOfNotNull(it.courseCode) }
            } else try {
                client.from("session_course_overview")
                    .select(Columns.list("session_id", "code")) {
                        filter { isIn("session_id", rows.map { it.sessionId }) }
                    }
                    .decodeList<SessionCourseRow>()
                    .groupBy({ it.sessionId }, { it.code })
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emptyMap()
            }
            DutyRecordsResult.Success(
                rows.map {
                    DutyRecord(
                        sessionId = it.sessionId,
                        building = it.building,
                        roomLabel = roomLabel(it.block, it.roomNumber, it.division),
                        courseCodes = codes[it.sessionId].orEmpty().sorted(),
                        startsAt = OffsetDateTime.parse(it.startsAt).toInstant(),
                        endsAt = OffsetDateTime.parse(it.endsAt).toInstant(),
                    )
                }.sortedBy { it.startsAt },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            DutyRecordsResult.Failure("Could not load your $noun. Check your internet connection.")
        }
    }

    private fun roomLabel(block: String, roomNumber: String, division: String?): String =
        "$block - $roomNumber" + division?.trim()?.takeIf { it.isNotEmpty() }?.let { " - $it" }.orEmpty()

    private companion object {
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("hh a", Locale.ENGLISH)
    }
}
