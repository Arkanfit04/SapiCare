package com.sapicare.app.ui.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.RiwayatKunjungan
import com.sapicare.app.data.model.Sapi
import com.sapicare.app.data.repository.RiwayatRepository
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RiwayatDashboardViewModel @Inject constructor(
    private val riwayatRepository: RiwayatRepository,
    private val sapiRepository: SapiRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Daftar sapi untuk dialog pilih sapi
    val sapiList: StateFlow<List<Sapi>> = sapiRepository.getAllSapi()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Semua riwayat + data sapinya
    val riwayatWithSapi: StateFlow<List<Pair<RiwayatKunjungan, Sapi>>> = combine(
        riwayatRepository.getAllRiwayat(),
        sapiRepository.getAllSapi(),
        _searchQuery
    ) { riwayatList, sapiList, query ->
        val sapiMap = sapiList.associateBy { it.id }
        riwayatList
            .mapNotNull { riwayat ->
                val sapi = sapiMap[riwayat.sapiId] ?: return@mapNotNull null
                Pair(riwayat, sapi)
            }
            .filter { (riwayat, sapi) ->
                if (query.isBlank()) true
                else sapi.nama.contains(query, ignoreCase = true) ||
                        riwayat.diagnosis.contains(query, ignoreCase = true) ||
                        riwayat.tindakan.contains(query, ignoreCase = true)
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchChange(query: String) { _searchQuery.value = query }
}
