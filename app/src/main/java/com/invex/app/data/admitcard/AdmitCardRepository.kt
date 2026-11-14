package com.invex.app.data.admitcard

import com.invex.app.admitcard.AdmitCard
import com.invex.app.admitcard.AdmitExam
import com.invex.app.admitcard.AdmitStudent
import com.invex.app.admitcard.VerifiedCard
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime

@Serializable
private data class SubjectRow(
    @SerialName("course_code") val courseCode: String,
    @SerialName("course_title") val courseTitle: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("ends_at") val endsAt: String,
)

@Serializable
private data class StudentJson(
    val name: String,
    @SerialName("registration_number") val registrationNumber: String,
    val program: String? = null,
    val specialization: String? = null,
    val branch: String? = null,
    val semester: Int? = null,
)

@Serializable
private data class CardJson(
    val student: StudentJson,
    val exams: List<SubjectRow> = emptyList(),
    @SerialName("qr_code") val qrCode: String? = null,
    @SerialName("signature_path") val signaturePath: String,
)

@Serializable
private data class VerifiedJson(
    @SerialName("student_name") val studentName: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("course_code") val courseCode: String,
    @SerialName("course_title") val courseTitle: String,
    val building: String,
    val block: String,
    @SerialName("room_number") val roomNumber: String,
    @SerialName("starts_at") val startsAt: String,
    @SerialName("seat_row") val seatRow: Int,
    @SerialName("seat_col") val seatCol: Int,
)

@Serializable
private data class SignatureRow(
    @SerialName("student_id") val studentId: String,
    @SerialName("signature_path") val signaturePath: String,
    @SerialName("signed_with") val signedWith: String,
)

@Serializable
private data class ReportInsert(
    @SerialName("student_id") val studentId: String,
    val message: String,
)

/** A failed call, with a message the screen can show as it is. */
class AdmitCardException(message: String) : Exception(message)

/**
 * Admit Card for the signed-in student: upcoming subjects, signing, the finished card, "Verify the Card" and "Report".
 * The QR code is the admit code of the student's next exam (the value the teacher's scanner checks).
 */
class AdmitCardRepository(private val client: SupabaseClient) {

    private fun userId() = client.auth.currentUserOrNull()?.id
        ?: throw AdmitCardException("You are logged out. Please log in again.")

    /** The upcoming exams of the student, earliest first: the subjects to read and verify. */
    suspend fun loadSubjects(): Result<List<AdmitExam>> = call("Could not load your subjects. Check your internet connection.") {
        client.from("student_exam_overview")
            .select(Columns.list("course_code", "course_title", "starts_at", "ends_at")) {
                filter {
                    eq("student_id", userId())
                    eq("phase", "upcoming")
                }
                order("starts_at", Order.ASCENDING)
            }
            .decodeList<SubjectRow>()
            .map { it.toExam() }
    }

    /** Saves the signature (replacing an older one) and returns the finished card. */
    suspend fun signAndLoadCard(signaturePng: ByteArray, signedWith: String): Result<AdmitCard> =
        call("Could not save your signature. Check your internet connection and try again.") {
            val uid = userId()
            val path = "$uid/admit-signature.png"
            client.storage.from(SIGNATURES).upload(path, signaturePng) {
                upsert = true
                contentType = ContentType.Image.PNG
            }
            client.from("admit_signatures").upsert(SignatureRow(uid, path, signedWith))
            loadCardNow()
        }

    /** The card of a student who has already signed. */
    suspend fun loadCard(): Result<AdmitCard> = call("Could not load your admit card. Check your internet connection.") { loadCardNow() }

    private suspend fun loadCardNow(): AdmitCard {
        val json = client.postgrest.rpc("get_admit_card").decodeAs<CardJson>()
        val png = runCatching { client.storage.from(SIGNATURES).downloadAuthenticated(json.signaturePath) }.getOrNull()
        return AdmitCard(
            student = AdmitStudent(
                name = json.student.name,
                enrollmentNumber = json.student.registrationNumber,
                course = json.student.program.orEmpty(),
                specialization = json.student.specialization.orEmpty(),
                branch = json.student.branch.orEmpty(),
                semester = json.student.semester,
            ),
            exams = json.exams.map { it.toExam() },
            qrCode = json.qrCode,
            signaturePng = png,
        )
    }

    /** "Verify the Card": looks the QR code up in the official records. Read-only. */
    suspend fun verifyCard(qrCode: String): Result<VerifiedCard> = call("Could not reach the server. Check your internet connection.") {
        try {
            val v = client.postgrest
                .rpc("verify_my_admit_card", buildJsonObject { put("p_code", qrCode) })
                .decodeAs<VerifiedJson>()
            VerifiedCard(
                studentName = v.studentName,
                enrollmentNumber = v.registrationNumber,
                courseCode = v.courseCode,
                courseTitle = v.courseTitle,
                building = v.building,
                room = "${v.block} - ${v.roomNumber}",
                startsAt = OffsetDateTime.parse(v.startsAt),
                seatRow = v.seatRow,
                seatCol = v.seatCol,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw AdmitCardException(
                "This admit card does not match the official records."
            )
        }
    }

    /** "Report": the student says something is wrong with the subjects. The admin handles it. */
    suspend fun report(message: String): Result<Unit> = call("Could not send your report. Check your internet connection and try again.") {
        client.from("admit_reports").insert(ReportInsert(userId(), message.trim()))
    }

    private fun SubjectRow.toExam() = AdmitExam(courseCode, courseTitle, OffsetDateTime.parse(startsAt), OffsetDateTime.parse(endsAt))

    private suspend fun <T> call(failure: String, block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: AdmitCardException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(AdmitCardException(failure))
    }

    private companion object {
        const val SIGNATURES = "signatures"
    }
}
