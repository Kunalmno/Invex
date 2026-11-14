package com.invex.app.data.exam

import com.invex.app.examdetails.ExamCourse
import com.invex.app.examdetails.RoomInfo
import com.invex.app.ui.theme.InvexAccent
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
private data class CourseRow(
    @SerialName("course_id") val courseId: String,
    val code: String,
    val title: String,
    @SerialName("student_count") val studentCount: Int = 0,
)

@Serializable
private data class RoomRow(
    val block: String,
    @SerialName("room_number") val roomNumber: String,
    val division: String? = null,
)

@Serializable
private data class StudentExamRow(
    val building: String,
    val block: String,
    @SerialName("room_number") val roomNumber: String,
    val division: String? = null,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
)

sealed interface ExamDetailsResult {
    /** [building], [date] and [time] are only filled for students (their Exam Details shows these instead of courses). */
    data class Success(
        val courses: List<ExamCourse>,
        val room: RoomInfo,
        val building: String? = null,
        val date: String? = null,
        val time: String? = null,
    ) : ExamDetailsResult
    data class Failure(val message: String) : ExamDetailsResult
}

/** Courses (with student counts) and the room of one exam the teacher is on duty for. */
class ExamDetailsRepository(private val client: SupabaseClient) {

    suspend fun load(sessionId: String): ExamDetailsResult {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return ExamDetailsResult.Failure("You are logged out. Please log in again.")
        if (InvexAccent.isStudent) return loadForStudent(sessionId, userId)
        return try {
            // Only returns a row when this teacher has a duty in that exam (row level security + the filter).
            val room = client.from("duty_overview")
                .select(Columns.list("block", "room_number", "division")) {
                    filter {
                        eq("session_id", sessionId)
                        eq("faculty_id", userId)
                    }
                    limit(1)
                }
                .decodeList<RoomRow>()
                .firstOrNull()
                ?: return ExamDetailsResult.Failure("You are not assigned to this exam.")

            val courses = client.from("session_course_overview")
                .select(Columns.list("course_id", "code", "title", "student_count")) {
                    filter { eq("session_id", sessionId) }
                }
                .decodeList<CourseRow>()
                .sortedBy { it.code }
                .map { ExamCourse(id = it.courseId, label = it.code, name = it.title, students = it.studentCount) }

            ExamDetailsResult.Success(
                courses = courses,
                room = RoomInfo(block = room.block, room = room.roomNumber, division = room.division?.takeIf { it.isNotBlank() }),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ExamDetailsResult.Failure("Could not load the exam details. Check your internet connection.")
        }
    }

    /** Student: building, date, time and room of an exam the student has a seat in. */
    private suspend fun loadForStudent(sessionId: String, userId: String): ExamDetailsResult = try {
        val row = client.from("student_exam_overview")
            .select(Columns.list("building", "block", "room_number", "division", "starts_at", "ends_at")) {
                filter {
                    eq("session_id", sessionId)
                    eq("student_id", userId)
                }
                limit(1)
            }
            .decodeList<StudentExamRow>()
            .firstOrNull()
            ?: return ExamDetailsResult.Failure("You have no seat in this exam.")
        val zone = ZoneId.systemDefault()
        val start = OffsetDateTime.parse(row.startsAt).atZoneSameInstant(zone)
        val end = OffsetDateTime.parse(row.endsAt).atZoneSameInstant(zone)
        ExamDetailsResult.Success(
            courses = emptyList(),
            room = RoomInfo(block = row.block, room = row.roomNumber, division = row.division?.takeIf { it.isNotBlank() }),
            building = row.building,
            date = start.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)),
            time = start.format(TIME_FORMAT) + " - " + end.format(TIME_FORMAT),
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        ExamDetailsResult.Failure("Could not load the exam details. Check your internet connection.")
    }

    private companion object {
        /** "10 AM", "1 PM" (design: "10 AM - 1 PM"). */
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("h a", Locale.ENGLISH)
    }
}
