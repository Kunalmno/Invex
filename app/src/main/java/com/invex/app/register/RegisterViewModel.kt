package com.invex.app.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.auth.RegisterResult
import com.invex.app.login.UserRole
import com.invex.app.ui.theme.InvexAccent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegisterUiState())
    val state: StateFlow<RegisterUiState> = _state.asStateFlow()

    fun onRoleChange(role: UserRole) = _state.update { it.copy(role = role, errorMessage = null) }

    fun onUserIdChange(value: String) = _state.update { it.copy(userId = value, errorMessage = null) }

    fun onCreatePasswordChange(value: String) = _state.update { it.copy(createPassword = value, errorMessage = null) }

    fun onConfirmPasswordChange(value: String) = _state.update { it.copy(confirmPassword = value, errorMessage = null) }

    /** The first problem with the input, or null when everything is fine. */
    private fun problem(current: RegisterUiState): String? {
        val id = IdRules.check(current.role, current.userId)
        return when {
            id is IdCheck.Invalid -> id.message
            current.createPassword.isBlank() -> "Please enter a password"
            current.createPassword.length < 6 -> "Password must be at least 6 characters"
            current.createPassword != current.confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    /** Checks the input, creates the account in Supabase and runs [onSuccess] with the role when it worked. */
    fun onRegisterClick(onSuccess: (UserRole) -> Unit) {
        val current = _state.value
        if (current.isLoading) return
        problem(current)?.let { message ->
            _state.update { it.copy(errorMessage = message) }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.auth.register(current.role, current.userId, current.createPassword)) {
                is RegisterResult.Failure -> _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                is RegisterResult.Success -> {
                    InvexAccent.setForRole(result.profile.role) // orange for faculty, blue for students
                    _state.update { it.copy(isLoading = false, createPassword = "", confirmPassword = "") }
                    onSuccess(current.role)
                }
            }
        }
    }
}
