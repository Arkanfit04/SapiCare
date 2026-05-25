package com.sapicare.app.ui.pengurus.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Sapi
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailSapiUiState(
    val sapi: Sapi? = null,
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailSapiViewModel @Inject constructor(
    private val repository: SapiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailSapiUiState())
    val uiState: StateFlow<DetailSapiUiState> = _uiState.asStateFlow()

    fun loadSapi(id: String) {
        viewModelScope.launch {
            _uiState.value = DetailSapiUiState(isLoading = true)
            val sapi = repository.getSapiById(id)
            _uiState.value = if (sapi != null) {
                DetailSapiUiState(sapi = sapi, isLoading = false)
            } else {
                DetailSapiUiState(isLoading = false, error = "Data sapi tidak ditemukan")
            }
        }
    }

    fun deleteSapi() {
        val sapi = _uiState.value.sapi ?: return
        viewModelScope.launch {
            repository.deleteSapi(sapi.id)
            _uiState.value = _uiState.value.copy(isDeleted = true)
        }
    }
}
