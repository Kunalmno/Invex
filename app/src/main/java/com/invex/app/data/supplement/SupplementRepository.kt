package com.invex.app.data.supplement

import com.invex.app.supplement.RosterStudent
import com.invex.app.supplement.SheetStatus
import com.invex.app.supplement.SheetSummary
import com.invex.app.supplement.StudentSheet
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
private data class SummaryRow(
    @SerialName("in_stock") val inStock: Int = 0,
    val issued: Int = 0,
    val pending: Int = 0,
    val resolved: Int = 0,
)

@Serializable
private data class IssuedRow(
    @SerialName("seat_assignment_id") val seatId: String,
    @SerialName("course_code") val courseCode: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("seat_row") val seatRow: Int,
    @SerialName("seat_col") val seatCol: Int,
    @SerialName("sheets_issued") val sheetsIssued: Int,
    val status: String,
    @SerialName("updated_at") val updatedAt: String = "",
)

@Serializable
private data class RosterRow(
    @SerialName("seat_assignment_id") val seatId: String,
    @SerialName("course_code") val courseCode: String,
    @SerialName("student_name") val studentName: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("seat_row") val seatRow: Int,
    @SerialName("seat_col") val seatCol: Int,
)

data class SupplementData(
    val summary: SheetSummary,
    val roster: List<RosterStudent>,
    val issued: List<StudentSheet>,
)

sealed interface SupplementResult {
    data class Success(val data: SupplementData) : SupplementResult
    data class Failure(val message: String) : SupplementResult
}

/** Supplement sheets of one exam: the counts, who can be issued a sheet, who already has one, and issuing. */
class SupplementRepository(private val client: SupabaseClient) {

    suspend fun load(sessionId: String): SupplementResult = try {
        val summary = client.from("supplement_summary")
            .select(Columns.list("in_stock", "issued", "pending", "resolved")) {
                filter { eq("session_id", sessionId) }
            }
            .decodeList<SummaryRow>()
            .firstOrNull() ?: SummaryRow()

        val issued = client.from("supplement_issued")
            .select { filter { eq("session_id", sessionId) } }
            .decodeList<IssuedRow>()
            .sortedByDescending { it.updatedAt }

        val roster = client.from("attendance_roster")
            .select(Columns.list("seat_assignment_id", "course_code", "student_name", "registration_number", "seat_row", "seat_col")) {
                filter { eq("session_id", sessionId) }
            }
            .decodeList<RosterRow>()
            .sortedWith(compareBy({ it.seatRow }, { it.seatCol }))

        SupplementResult.Success(
            SupplementData(
                // Awaiting Return = sheets still with the student; Returned = resolved.
                summary = SheetSummary(
                    issued = summary.issued,
                    awaitingReturn = summary.pending,
                    returned = summary.resolved,
                    inStock = summary.inStock,
                ),
                roster = roster.map {
                    RosterStudent(it.seatId, it.studentName, it.courseCode, seatLabel(it.seatRow, it.seatCol), it.registrationNumber)
                },
                issued = issued.map {
                    StudentSheet(
                        id = it.seatId,
                        name = it.studentName,
                        course = it.courseCode,
                        seat = seatLabel(it.seatRow, it.seatCol),
                        rollNumber = it.registrationNumber,
                        sheetCount = it.sheetsIssued,
                        status = if (it.status == "resolved") SheetStatus.RESOLVED else SheetStatus.PENDING,
                    )
                },
            ),
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        SupplementResult.Failure("Could not load supplement sheets. Check your internet connection.")
    }

    /** Issues one more sheet to the student in that seat. Returns null when it worked, or a message to show. */
    suspend fun issue(seatId: String): String? = try {
        client.postgrest.rpc("issue_supplement_sheet", buildJsonObject { put("p_seat", seatId) })
        null
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        if (e.message.orEmpty().contains("No supplement sheets", ignoreCase = true)) {
            "No supplement sheets are left in stock."
        } else {
            "Could not issue the sheet. Check your internet connection and try again."
        }
    }

    private fun seatLabel(row: Int, col: Int) = "Row $row, Seat $col"
}
