package com.invex.app.accountinfo

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AccountInfoViewModel : ViewModel() {

    // TODO: load the real profile from the backend. The design shows empty fields, so we start empty.
    private var saved = AccountInfoUiState()

    private val _state = MutableStateFlow(saved)
    val state: StateFlow<AccountInfoUiState> = _state.asStateFlow()

    fun onNameChange(value: String) = _state.update { it.copy(name = value) }
    fun onRegistrationChange(value: String) = _state.update { it.copy(registrationNumber = value) }
    fun onPhoneChange(value: String) = _state.update { it.copy(phone = value) }
    fun onEmailChange(value: String) = _state.update { it.copy(email = value) }

    /** SAVE: keep what was typed as the new saved copy. TODO: send it to the backend. */
    fun save() {
        saved = _state.value
    }

    /** CANCEL: throw away unsaved edits. */
    fun cancel() {
        _state.value = saved
    }
}
