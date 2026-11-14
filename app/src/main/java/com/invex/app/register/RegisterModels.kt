package com.invex.app.register

import com.invex.app.login.UserRole

data class RegisterUiState(
    val role: UserRole = UserRole.FACULTY,
    val userId: String = "",
    val createPassword: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
)
