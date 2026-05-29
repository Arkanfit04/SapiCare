package com.sapicare.app.ui.peternak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Sapi
import com.sapicare.app.data.model.UserSession
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SapiSayaViewModel @Inject constructor(
    private val sapiRepository: SapiRepository
) : ViewModel() {

    private val _ownerId = MutableStateFlow("")
    private val _wilayah = MutableStateFlow("")
    private val _searchQuery = MutableStateFlow("")
    private val _showSapiSaya = MutableStateFlow(true)

    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val showSapiSaya: StateFlow<Boolean> = _showSapiSaya.asStateFlow()

    val sapiSaya: StateFlow<List<Sapi>> = _ownerId
        .flatMapLatest { id ->
            if (id.isEmpty()) flowOf(emptyList())
            else sapiRepository.getSapiByOwner(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sapiWilayah: StateFlow<List<Sapi>> = _wilayah
        .flatMapLatest { w ->
            if (w.isEmpty()) sapiRepository.getAllSapi()
            else sapiRepository.getSapiByWilayah(w)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun init(session: UserSession?) {
        _ownerId.value = session?.uid ?: ""
        // Wilayah diambil dari sapi miliknya (lazy — pakai getAllSapi kalau belum ada)
        viewModelScope.launch {
            sapiSaya.first { it.isNotEmpty() || _ownerId.value.isNotEmpty() }.let { list ->
                val w = list.firstOrNull()?.wilayah ?: ""
                if (w.isNotEmpty()) _wilayah.value = w
            }
        }
    }

    fun onSearchChange(q: String) { _searchQuery.value = q }
    fun setShowSapiSaya(v: Boolean) { _showSapiSaya.value = v }
}
