package com.invex.app.data.medical

import com.invex.app.medicalleave.LeaveStudent
import com.invex.app.medicalleave.PickedDocument
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

/** One row of the `student_profile` view (own profile + academic details). */
@Serializable
private data class StudentProfileRow(
    @SerialName("full_name") val fullName: String,
    @SerialName("registration_number") val registrationNumber: String,
    val phone: String? = null,
    val email: String? = null,
    val program: String? = null,
    val specialization: String? = null,
    val branch: String? = null,
    val semester: Int? = null,
)

/** The request the student sends. Status is left out: the database sets it to 'pending'. */
@Serializable
private data class LeaveInsert(
    @SerialName("student_id") val studentId: String,
    val condition: String,
    val details: String,
    @SerialName("document_path") val documentPath: String? = null,
    @SerialName("document_name") val documentName: String? = null,
)

sealed interface LeaveStudentResult {
    data class Success(val student: LeaveStudent) : LeaveStudentResult
    data class Failure(val message: String) : LeaveStudentResult
}

sealed interface LeaveSubmitResult {
    data object Success : LeaveSubmitResult
    data class Failure(val message: String) : LeaveSubmitResult
}

/**
 * Medical Leave: the student reads their own details, then sends a request (pending). The admin decides;
 * the student cannot change a request after sending (row level security).
 */
class MedicalLeaveRepository(private val client: SupabaseClient) {

    suspend fun loadStudent(): LeaveStudentResult {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return LeaveStudentResult.Failure("You are logged out. Please log in again.")
        return try {
            val row = client.from("student_profile")
                .select { filter { eq("student_id", userId) } }
                .decodeList<StudentProfileRow>()
                .firstOrNull()
                ?: return LeaveStudentResult.Failure("Your student details were not found.")
            LeaveStudentResult.Success(
                LeaveStudent(
                    name = row.fullName,
                    enrollmentNumber = row.registrationNumber,
                    course = row.program.orEmpty(),
                    specialization = row.specialization.orEmpty(),
                    branch = row.branch.orEmpty(),
                    semester = row.semester,
                    mobile = row.phone.orEmpty(),
                    email = row.email.orEmpty(),
                ),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LeaveStudentResult.Failure("Could not load your details. Check your internet connection.")
        }
    }

    suspend fun submit(condition: String, details: String, document: PickedDocument?): LeaveSubmitResult {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return LeaveSubmitResult.Failure("You are logged out. Please log in again.")
        var uploadedPath: String? = null
        return try {
            if (document != null) {
                val extension = document.name.substringAfterLast('.', "").lowercase().filter { it.isLetterOrDigit() }.take(5)
                val path = "$userId/${UUID.randomUUID()}" + if (extension.isNotEmpty()) ".$extension" else ""
                client.storage.from(BUCKET).upload(path, document.bytes) {
                    upsert = false
                    contentType = ContentType.parse(document.mimeType)
                }
                uploadedPath = path
            }
            client.from("medical_leaves").insert(
                LeaveInsert(
                    studentId = userId,
                    condition = condition,
                    details = details.trim(),
                    documentPath = uploadedPath,
                    documentName = document?.name,
                ),
            )
            LeaveSubmitResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Do not leave an orphan file behind when the request itself could not be saved.
            uploadedPath?.let { runCatching { client.storage.from(BUCKET).delete(it) } }
            LeaveSubmitResult.Failure("Could not send your request. Check your internet connection and try again.")
        }
    }

    private companion object {
        const val BUCKET = "medical-documents"
    }
}
