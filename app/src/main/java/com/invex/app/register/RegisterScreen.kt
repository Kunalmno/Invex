package com.invex.app.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.login.UserRole
import com.invex.app.ui.theme.Inter
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.MobileBackground
import com.invex.app.ui.theme.StatusRed

/**
 * Stateful entry point: owns the ViewModel and navigation callbacks.
 */
@Composable
fun RegisterRoute(
    onRegistered: (UserRole) -> Unit,
    onSignIn: () -> Unit,
    viewModel: RegisterViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    RegisterScreen(
        state = state,
        onRoleChange = viewModel::onRoleChange,
        onUserIdChange = viewModel::onUserIdChange,
        onCreatePasswordChange = viewModel::onCreatePasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onRegister = { viewModel.onRegisterClick(onRegistered) },
        onSignIn = onSignIn,
    )
}

/**
 * Stateless screen:
 * Displays wordmark, role toggle, 3 credential fields (ID, Create Password, Confirm Password),
 * "Create your account" CTA, and centered legal terms.
 */
@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onRoleChange: (UserRole) -> Unit,
    onUserIdChange: (String) -> Unit,
    onCreatePasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MobileBackground)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        // Logo and "Sign in" switch
        RegisterHeader(onSignIn = onSignIn)

        // Role selector (Faculty / Student)
        RegisterRoleSelector(selected = state.role, onSelect = onRoleChange)

        // Error message banner if validation fails
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                fontFamily = Inter,
                fontSize = 13.sp,
                color = StatusRed,
            )
        }

        // Field 1: Faculty ID / Student ID
        RegisterField(
            label = state.role.idLabel,
            value = state.userId,
            onValueChange = onUserIdChange,
            imeAction = ImeAction.Next,
        )

        // Field 2: Create Password
        RegisterField(
            label = "Create Password",
            value = state.createPassword,
            onValueChange = onCreatePasswordChange,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            visualTransformation = PasswordVisualTransformation(),
        )

        // Field 3: Confirm Password
        RegisterField(
            label = "Confirm Password",
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            visualTransformation = PasswordVisualTransformation(),
        )

        // CTA Button
        RegisterButton(onClick = onRegister, isLoading = state.isLoading)

        // Centered Legal Disclaimer
        RegisterTermsText()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RegisterScreenPreview() {
    InvexTheme {
        RegisterScreen(
            state = RegisterUiState(),
            onRoleChange = {},
            onUserIdChange = {},
            onCreatePasswordChange = {},
            onConfirmPasswordChange = {},
            onRegister = {},
            onSignIn = {},
        )
    }
}
