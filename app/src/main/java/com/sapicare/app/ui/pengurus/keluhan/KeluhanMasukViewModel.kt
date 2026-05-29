package com.sapicare.app.ui.pengurus.keluhan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Keluhan
import com.sapicare.app.data.model.StatusKeluhan
import com.sapicare.app.data.repository.KeluhanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class KeluhanMasukViewModel @Inject constructor(
    private val keluhanRepository: KeluhanRepository
) : ViewModel() {

    private val _filterStatus = MutableStateFlow<StatusKeluhan?>(null)
    val filterStatus: StateFlow<StatusKeluhan?> = _filterStatus.asStateFlow()

    val keluhanList: StateFlow<List<Keluhan>> = combine(
        keluhanRepository.getAllKeluhan(), _filterStatus
    ) { list, filter ->
        if (filter == null) list else list.filter { it.status == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(status: StatusKeluhan?) { _filterStatus.value = status }
}
