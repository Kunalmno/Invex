package com.invex.app.login

/**
 * Who is logging in. The selector under the logo switches this. The label above the first field follows it.
 * Only "Faculty" is drawn; "Student" is assumed from the dropdown arrow and the Student page in Figma.
 */
enum class UserRole(val label: String, val idLabel: String, val dbValue: String) {
    FACULTY("Faculty", "Faculty ID", "faculty"),
    STUDENT("Student", "Student ID", "student"),
}

data class LoginUiState(
    val role: UserRole = UserRole.FACULTY,
    val userId: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    /** Shown above the Login button. Not in the Figma design yet. */
    val error: String? = null,
)
