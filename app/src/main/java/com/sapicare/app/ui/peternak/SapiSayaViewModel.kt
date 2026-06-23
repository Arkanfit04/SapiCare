package com.sapicare.app.ui.peternak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Sapi
import com.sapicare.app.data.model.UserSession
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SapiSayaViewModel @Inject constructor(
    private val sapiRepository: SapiRepository
) : ViewModel() {

    private val _ownerId = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    private val _showSapiSaya = MutableStateFlow(true)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val showSapiSaya: StateFlow<Boolean> = _showSapiSaya.asStateFlow()

    val sapiSaya: StateFlow<List<Sapi>> = _ownerId
        .flatMapLatest { id ->
            if (id.isEmpty()) flowOf(emptyList())
            else sapiRepository.getSapiByOwner(id)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val sapiWilayah: StateFlow<List<Sapi>> =
        sapiRepository.getAllSapi()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    fun init(session: UserSession?) {

        if (session == null) {
            _ownerId.value = ""
            return
        }

        _ownerId.value = session.uid
    }

    fun onSearchChange(q: String) { _searchQuery.value = q }
    fun setShowSapiSaya(v: Boolean) { _showSapiSaya.value = v }
}
