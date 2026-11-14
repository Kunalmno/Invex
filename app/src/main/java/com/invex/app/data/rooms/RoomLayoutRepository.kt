package com.invex.app.data.rooms

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** One occupied seat. Name, registration number, course and status are null when the viewer may not see them. */
@Serializable
data class LayoutSeat(
    val row: Int,
    val col: Int,
    @SerialName("is_mine") val isMine: Boolean = false,
    @SerialName("student_name") val studentName: String? = null,
    @SerialName("registration_number") val registrationNumber: String? = null,
    @SerialName("course_code") val courseCode: String? = null,
    val status: String? = null, // "present", "absent" or null (not marked)
)

/** What the database function `get_room_layout` returns for one exam. */
@Serializable
data class RoomLayout(
    val block: String,
    @SerialName("room_number") val roomNumber: String,
    val division: String? = null,
    @SerialName("seat_rows") val seatRows: Int,
    @SerialName("seat_cols") val seatCols: Int,
    val courses: List<String> = emptyList(),
    val seats: List<LayoutSeat> = emptyList(),
)

sealed interface RoomLayoutResult {
    data class Success(val layout: RoomLayout) : RoomLayoutResult
    data class Failure(val message: String) : RoomLayoutResult
}

/**
 * The room's seat grid for one exam. The database decides what the viewer may see: a teacher on duty
 * sees every student, a student only sees their own details (the same function serves both apps).
 */
class RoomLayoutRepository(private val client: SupabaseClient) {

    suspend fun load(sessionId: String): RoomLayoutResult = try {
        val layout = client.postgrest
            .rpc("get_room_layout", buildJsonObject { put("p_session", sessionId) })
            .decodeAs<RoomLayout>()
        RoomLayoutResult.Success(layout)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val text = e.message.orEmpty()
        RoomLayoutResult.Failure(
            when {
                text.contains("not on duty", ignoreCase = true) -> "You are not on duty for this exam."
                text.contains("no seat", ignoreCase = true) -> "You have no seat in this exam."
                else -> "Could not load the room. Check your internet connection."
            },
        )
    }
}
