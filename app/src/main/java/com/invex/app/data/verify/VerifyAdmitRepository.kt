
package com.invex.app.data.verify

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

@Serializable
data class VerifiedStudent(
    @SerialName("student_name") val studentName: String,
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("seat_row") val seatRow: Int,
    @SerialName("seat_col") val seatCol: Int,
    @SerialName("already_verified") val alreadyVerified: Boolean = false,
)

sealed interface VerifyResult {
    data class Verified(val student: VerifiedStudent) : VerifyResult
    data class Rejected(val message: String) : VerifyResult
}

class VerifyAdmitRepository(private val client: SupabaseClient) {

    suspend fun verify(qrText: String): VerifyResult {
        val code = runCatching {
            UUID.fromString(qrText.trim())
        }.getOrNull()
            ?: return VerifyResult.Rejected("This is not an InvEx admit card.")

        return try {
            val student = client.postgrest
                .rpc(
                    "verify_admit_card",
                    buildJsonObject {
                        put("p_code", code.toString())
                    }
                )
                .decodeAs<VerifiedStudent>()

            VerifyResult.Verified(student)

        } catch (e: CancellationException) {
            throw e

        } catch (e: Exception) {
            val errorMessage = e.message.orEmpty()

            VerifyResult.Rejected(
                when {
                    errorMessage.contains(
                        "Invalid admit card",
                        ignoreCase = true
                    ) -> "Admit card not found. It may be wrong or cancelled."

                    errorMessage.contains(
                        "not assigned",
                        ignoreCase = true
                    ) -> "This student is not in an exam you are on duty for."

                    errorMessage.contains(
                        "Only faculty",
                        ignoreCase = true
                    ) -> "Only faculty can verify admit cards."

                    else -> "Could not verify this admit card. Try again."
                }
            )
        }
    }
}
