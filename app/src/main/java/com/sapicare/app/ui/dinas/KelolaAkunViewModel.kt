package com.sapicare.app.ui.dinas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.UserProfile
import com.sapicare.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KelolaAkunUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val profiles: List<UserProfile> = emptyList(),
    val filtered: List<UserProfile> = emptyList()
)

@HiltViewModel
class KelolaAkunViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(KelolaAkunUiState())
    val uiState: StateFlow<KelolaAkunUiState> = _uiState.asStateFlow()

    private val _filterRole = MutableStateFlow<String?>(null)
    val filterRole: StateFlow<String?> = _filterRole.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = KelolaAkunUiState(isLoading = true)
            authRepository.getAllUserProfiles().fold(
                onSuccess = { profiles ->
                    _uiState.value = KelolaAkunUiState(isLoading = false, profiles = profiles,
                        filtered = applyFilter(profiles, _filterRole.value))
                },
                onFailure = { e ->
                    _uiState.value = KelolaAkunUiState(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun setFilter(role: String?) {
        _filterRole.value = role
        _uiState.value = _uiState.value.copy(
            filtered = applyFilter(_uiState.value.profiles, role)
        )
    }

    fun approve(targetUid: String, dinasUid: String) {
        viewModelScope.launch {
            authRepository.approveUser(targetUid, dinasUid)
            load() // refresh
        }
    }

    fun reject(targetUid: String) {
        viewModelScope.launch {
            authRepository.rejectUser(targetUid)
            load()
        }
    }

    private fun applyFilter(profiles: List<UserProfile>, role: String?): List<UserProfile> {
        return if (role == null) profiles else profiles.filter { it.role == role }
    }
}
