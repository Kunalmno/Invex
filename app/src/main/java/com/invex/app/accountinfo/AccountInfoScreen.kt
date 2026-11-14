package com.invex.app.accountinfo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.invex.app.ui.theme.InvexTheme
import com.invex.app.ui.theme.MobileBackground

/** SAVE keeps the edits and goes back; CANCEL drops them and goes back. */
@Composable
fun AccountInfoRoute(
    onBack: () -> Unit,
    viewModel: AccountInfoViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()
    AccountInfoScreen(
        state = state,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onRegistrationChange = viewModel::onRegistrationChange,
        onPhoneChange = viewModel::onPhoneChange,
        onEmailChange = viewModel::onEmailChange,
        onSave = {
            viewModel.save()
            onBack()
        },
        onCancel = {
            viewModel.cancel()
            onBack()
        },
    )
}

@Composable
fun AccountInfoScreen(
    state: AccountInfoUiState,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onRegistrationChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MobileBackground)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        AccountInfoHeader(onBack = onBack)
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            AccountField("Name", state.name, onNameChange)
            AccountField("Registration Number", state.registrationNumber, onRegistrationChange)
            AccountField("Phone Number", state.phone, onPhoneChange, keyboardType = KeyboardType.Phone)
            AccountField("Email", state.email, onEmailChange, keyboardType = KeyboardType.Email, imeAction = ImeAction.Done)
        }
        AccountActionButtons(onSave = onSave, onCancel = onCancel, modifier = Modifier.padding(top = 8.dp))
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AccountInfoPreview() {
    InvexTheme {
        AccountInfoScreen(AccountInfoUiState(), {}, {}, {}, {}, {}, {}, {})
    }
}
