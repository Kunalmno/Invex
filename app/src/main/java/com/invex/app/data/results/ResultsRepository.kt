package com.invex.app.data.results

import com.invex.app.results.SemesterResult
import com.invex.app.results.SubjectResult
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One row of the `result_overview` view. */
@Serializable
private data class ResultRow(
    val semester: Int,
    @SerialName("course_code") val courseCode: String,
    @SerialName("course_title") val courseTitle: String,
    val grade: Double,
)

sealed interface ResultsResult {
    /** Semesters in order (1, 2, 3 ...); only semesters that have grades. */
    data class Success(val semesters: List<SemesterResult>) : ResultsResult
    data class Failure(val message: String) : ResultsResult
}

/** Reads the signed-in student's grades. The admin enters them; the student can only read their own. */
class ResultsRepository(private val client: SupabaseClient) {

    suspend fun load(): ResultsResult {
        val userId = client.auth.currentUserOrNull()?.id
            ?: return ResultsResult.Failure("You are logged out. Please log in again.")
        return try {
            val rows = client.from("result_overview")
                .select(Columns.list("semester", "course_code", "course_title", "grade")) {
                    filter { eq("student_id", userId) }
                }
                .decodeList<ResultRow>()
            ResultsResult.Success(
                rows.groupBy { it.semester }
                    .toSortedMap()
                    .map { (semester, list) ->
                        SemesterResult(
                            semester = semester,
                            subjects = list.sortedBy { it.courseCode }
                                .map { SubjectResult(it.courseTitle, it.courseCode, it.grade) },
                        )
                    },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ResultsResult.Failure("Could not load your results. Check your internet connection.")
        }
    }
}
