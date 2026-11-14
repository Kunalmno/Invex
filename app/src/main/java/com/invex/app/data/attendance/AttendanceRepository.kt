package com.invex.app.data.attendance

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** One student seat of an exam, from the `attendance_roster` view. [status] is null until it is marked. */
@Serializable
data class RosterRow(
    @SerialName("seat_assignment_id") val seatId: String,
    @SerialName("course_id") val courseId: String,
    @SerialName("course_code") val courseCode: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("seat_row") val seatRow: Int,
    @SerialName("seat_col") val seatCol: Int,
    val status: String? = null, // "present", "absent" or null
)

sealed interface RosterResult {
    data class Success(val rows: List<RosterRow>) : RosterResult
    data class Failure(val message: String) : RosterResult
}

/** Reads the attendance of an exam the teacher is on duty for, and lets the teacher mark one student. */
class AttendanceRepository(private val client: SupabaseClient) {

    suspend fun loadRoster(sessionId: String): RosterResult = try {
        val rows = client.from("attendance_roster")
            .select { filter { eq("session_id", sessionId) } }
            .decodeList<RosterRow>()
            .sortedWith(compareBy({ it.seatRow }, { it.seatCol }))
        RosterResult.Success(rows)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        RosterResult.Failure("Could not load attendance. Check your internet connection.")
    }

    /** Manual attendance. Returns null when it worked, or a message to show. */
    suspend fun mark(seatId: String, present: Boolean): String? = try {
        client.postgrest.rpc(
            "set_attendance",
            buildJsonObject {
                put("p_seat", seatId)
                put("p_status", if (present) "present" else "absent")
            },
        )
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        "Could not save attendance. Check your internet connection and try again."
    }
}
