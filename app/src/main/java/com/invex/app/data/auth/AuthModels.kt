package com.invex.app.data.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One row of the `profiles` table. */
@Serializable
data class UserProfile(
    val id: String,
    val role: String,                                  // "student", "faculty" or "admin"
    @SerialName("registration_number") val registrationNumber: String,
    @SerialName("full_name") val fullName: String,
    val phone: String? = null,
    val email: String? = null,
)

sealed interface LoginResult {
    data class Success(val profile: UserProfile) : LoginResult
    data class Failure(val message: String) : LoginResult
}

sealed interface RegisterResult {
    data class Success(val profile: UserProfile) : RegisterResult
    data class Failure(val message: String) : RegisterResult
}
