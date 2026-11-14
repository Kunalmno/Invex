package com.invex.app.data.restroom

import com.invex.app.supplement.RosterStudent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.OffsetDateTime

@Serializable
private data class PassRow(
    @SerialName("pass_id") val passId: String,
    @SerialName("seat_assignment_id") val seatId: String,
    @SerialName("serial_number") val serialNumber: String,
    val status: String, // "running", "returned" or "not_returned"
    @SerialName("issued_at") val issuedAt: String,
    @SerialName("returned_at") val returnedAt: String? = null,
    @SerialName("student_name") val studentName: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("course_code") val courseCode: String,
    @SerialName("seat_row") val seatRow: Int,
    @SerialName("seat_col") val seatCol: Int,
)

@Serializable
private data class StudentRow(
    @SerialName("seat_assignment_id") val seatId: String,
    @SerialName("course_code") val courseCode: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("seat_row") val seatRow: Int,
    @SerialName("seat_col") val seatCol: Int,
)

/** One restroom pass with real times. The screen works out the countdown from [issuedAt]. */
data class PassRecord(
    val id: String,
    /** The student's seat assignment id (same id the student list uses). */
    val seatId: String,
    val serial: String,
    val status: String,
    val issuedAt: Instant,
    val returnedAt: Instant?,
    val studentName: String,
    val regNo: String,
    val course: String,
    val seat: String,
)

data class RestroomData(val passes: List<PassRecord>, val students: List<RosterStudent>)

sealed interface RestroomResult {
    data class Success(val data: RestroomData) : RestroomResult
    data class Failure(val message: String) : RestroomResult
}

/** Restroom passes of one exam: reading them, issuing, marking returned, and reporting to the runner. */
class RestroomRepository(private val client: SupabaseClient) {

    suspend fun load(sessionId: String): RestroomResult = try {
        val passes = client.from("restroom_pass_overview")
            .select { filter { eq("session_id", sessionId) } }
            .decodeList<PassRow>()
            .map {
                PassRecord(
                    id = it.passId,
                    seatId = it.seatId,
                    serial = it.serialNumber,
                    status = it.status,
                    issuedAt = OffsetDateTime.parse(it.issuedAt).toInstant(),
                    returnedAt = it.returnedAt?.let { t -> OffsetDateTime.parse(t).toInstant() },
                    studentName = it.studentName,
                    regNo = it.registrationNumber,
                    course = it.courseCode,
                    seat = "R${it.seatRow} S${it.seatCol}",
                )
            }
            .sortedBy { it.issuedAt }
        val students = client.from("attendance_roster")
            .select(Columns.list("seat_assignment_id", "course_code", "student_name", "registration_number", "seat_row", "seat_col")) {
                filter { eq("session_id", sessionId) }
            }
            .decodeList<StudentRow>()
            .sortedWith(compareBy({ it.seatRow }, { it.seatCol }))
            .map { RosterStudent(it.seatId, it.studentName, it.courseCode, "Row ${it.seatRow}, Seat ${it.seatCol}", it.registrationNumber) }
        RestroomResult.Success(RestroomData(passes, students))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        RestroomResult.Failure("Could not load restroom passes. Check your internet connection.")
    }

    /** Returns null when it worked, or a message to show. */
    suspend fun issue(seatId: String): String? = try {
        client.postgrest.rpc("create_restroom_pass", buildJsonObject { put("p_seat", seatId) })
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        if (e.message.orEmpty().contains("already has a pass", ignoreCase = true)) {
            "This student already has a pass running."
        } else {
            "Could not issue the pass. Check your internet connection and try again."
        }
    }

    suspend fun markReturned(passId: String): String? = try {
        client.postgrest.rpc("return_restroom_pass", buildJsonObject { put("p_pass", passId) })
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        "Could not mark the pass as returned. Check your internet connection and try again."
    }

    /** "Report to Runner": raises a runner request for this exam's room. */
    suspend fun reportToRunner(sessionId: String): String? = try {
        val userId = client.auth.currentUserOrNull()?.id ?: return "You are logged out. Please log in again."
        client.from("assistance_requests").insert(
            buildJsonObject {
                put("session_id", sessionId)
                put("requested_by", userId)
                put("kind", "runner")
            },
        )
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        "Could not reach the runner. Check your internet connection and try again."
    }
}
