package com.invex.app.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.MobileBackground

/** Stateful entry point: owns the ViewModel. */
@Composable
fun LoginRoute(
    onLoggedIn: (UserRole) -> Unit,
    onSignUp: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    LoginScreen(
        state = state,
        onRoleChange = viewModel::onRoleChange,
        onUserIdChange = viewModel::onUserIdChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = { viewModel.onLoginClick(onLoggedIn) },
        onSignUp = onSignUp,
    )
}

/**
 * Stateless screen. The whole block (logo, role, two fields, button, terms) is centered on the screen
 * with 24dp between items, as in the Figma frame. It scrolls only when the keyboard or a short screen needs it.
 */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onRoleChange: (UserRole) -> Unit,
    onUserIdChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onSignUp: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        LoginHeader(onSignUp = onSignUp)
        RoleSelector(selected = state.role, onSelect = onRoleChange)
        LabeledField(
            label = state.role.idLabel,
            value = state.userId,
            onValueChange = onUserIdChange,
        )
        LabeledField(
            label = "Password",
            value = state.password,
            onValueChange = onPasswordChange,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            visualTransformation = PasswordVisualTransformation(),
        )
        state.error?.let { LoginError(it) }
        LoginButton(onClick = onLogin, loading = state.isLoading)
        TermsText()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LoginScreenPreview() {
    InvexTheme {
        LoginScreen(
            state = LoginUiState(),
            onRoleChange = {},
            onUserIdChange = {},
            onPasswordChange = {},
            onLogin = {},
            onSignUp = {},
        )
    }
}
