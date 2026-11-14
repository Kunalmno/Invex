package com.invex.app.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.invex.app.data.InvexBackend
import com.invex.app.data.auth.LoginResult
import com.invex.app.ui.theme.InvexAccent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    /** Roles that have screens in the app so far. */
    private val supportedRoles = setOf(UserRole.FACULTY, UserRole.STUDENT)

    fun onRoleChange(role: UserRole) = _state.update { it.copy(role = role, error = null) }

    fun onUserIdChange(value: String) = _state.update { it.copy(userId = value, error = null) }

    fun onPasswordChange(value: String) = _state.update { it.copy(password = value, error = null) }

    /** Logs in with Supabase. [onSuccess] runs only when the ID and password are right and the role matches. */
    fun onLoginClick(onSuccess: (UserRole) -> Unit) {
        val current = _state.value
        if (current.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = InvexBackend.auth.login(current.role, current.userId, current.password)) {
                is LoginResult.Failure ->
                    _state.update { it.copy(isLoading = false, error = result.message) }
                is LoginResult.Success -> {
                    if (current.role in supportedRoles) {
                        InvexAccent.setForRole(current.role.dbValue) // orange for faculty, blue for students
                        _state.update { it.copy(isLoading = false, password = "") }
                        onSuccess(current.role)
                    } else {
                        InvexBackend.auth.logout()
                        _state.update {
                            it.copy(isLoading = false, error = "${current.role.label} screens are not available in the app yet")
                        }
                    }
                }
            }
        }
    }
}
