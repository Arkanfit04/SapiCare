package com.sapicare.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.SavedAccount
import com.sapicare.app.data.model.UserRole
import com.sapicare.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    // Setelah Google login berhasil, tampilkan role picker
    val showRolePicker: Boolean = false,
    val googleUsername: String = ""
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val sessionFlow = authRepository.sessionFlow
    val savedAccountsFlow = authRepository.savedAccountsFlow
    val activeUidFlow = authRepository.activeUidFlow

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun loginWithGoogle(idToken: String, onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.loginWithGoogle(idToken).fold(
                onSuccess = { (username, hasAccounts) ->
                    if (hasAccounts) {
                        // Sudah punya akun — langsung ke dashboard akun aktif
                        val session = sessionFlow.let { flow ->
                            var s: com.sapicare.app.data.model.UserSession? = null
                            val job = async { flow.collect { s = it } }
                            kotlinx.coroutines.delay(200)
                            job.cancel()
                            s
                        }
                        _uiState.value = AuthUiState()
                        onSuccess(session?.role ?: UserRole.PENGURUS)
                    } else {
                        // Belum punya akun — tampilkan role picker
                        _uiState.value = AuthUiState(
                            showRolePicker = true,
                            googleUsername = username
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.value = AuthUiState(error = err.message)
                }
            )
        }
    }

    fun selectRole(role: UserRole, onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.saveRoleAccount(role).fold(
                onSuccess = { session ->
                    _uiState.value = AuthUiState()
                    onSuccess(session.role)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(isLoading = false, error = err.message)
                }
            )
        }
    }

    fun switchAccount(account: SavedAccount, onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            authRepository.switchAccount(account).fold(
                onSuccess = { session -> onSuccess(session.role) },
                onFailure = {}
            )
        }
    }

    fun removeAccount(uid: String) {
        viewModelScope.launch { authRepository.removeAccount(uid) }
    }

    fun logout() { authRepository.logout() }

    fun logoutAll() {
        viewModelScope.launch { authRepository.logoutAll() }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

}
