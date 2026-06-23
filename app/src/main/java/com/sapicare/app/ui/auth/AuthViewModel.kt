package com.sapicare.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.ApprovalStatus
import com.sapicare.app.data.model.SavedAccount
import com.sapicare.app.data.model.UserRole
import com.sapicare.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
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

    // ─── LOGIN DINAS ──────────────────────────────────────────────────────────
    fun loginDinas(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.loginDinas(email, password).fold(
                onSuccess = { _ ->
                    _uiState.value = AuthUiState()
                    onSuccess()
                },
                onFailure = { err ->
                    _uiState.value = AuthUiState(error = err.message)
                }
            )
        }
    }

    // ─── LOGIN GOOGLE ─────────────────────────────────────────────────────────
    fun loginWithGoogle(idToken: String, onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            authRepository.loginWithGoogle(idToken).fold(
                onSuccess = { (username, hasAccounts) ->
                    if (hasAccounts) {
                        val session = authRepository.sessionFlow.firstOrNull()
                        _uiState.value = AuthUiState()
                        onSuccess(session?.role ?: UserRole.PENGURUS)
                    } else {
                        _uiState.value = AuthUiState(showRolePicker = true, googleUsername = username)
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

    fun cancelRolePicker() {
        viewModelScope.launch {
            authRepository.logout()

            _uiState.value = _uiState.value.copy(
                showRolePicker = false,
                googleUsername = "",
                error = null
            )
        }
    }

    // ─── REFRESH STATUS APPROVAL ──────────────────────────────────────────────
    fun refreshApprovalStatus(onResult: (ApprovalStatus) -> Unit) {
        viewModelScope.launch {
            val session = authRepository.sessionFlow.firstOrNull() ?: return@launch
            val status = authRepository.refreshApprovalStatus(session.uid)

            val accounts = authRepository.savedAccountsFlow.firstOrNull() ?: return@launch
            val account = accounts.find { it.uid == session.uid } ?: return@launch

            authRepository.switchAccount(
                account.copy(
                    approvalStatus = status
                )
            )

            onResult(status)
        }
    }

    // ─── REAPPLY APPROVAL ──────────────────────────────────────────────
    fun reapplyApproval(uid: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.reapplyApproval(uid)
                .onSuccess { onSuccess() }
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

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun logoutAll() {
        viewModelScope.launch { authRepository.logoutAll() }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null)
    }

    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = message
        )
    }
}
