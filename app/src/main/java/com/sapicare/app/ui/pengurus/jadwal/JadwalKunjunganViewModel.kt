package com.sapicare.app.ui.pengurus.jadwal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.JadwalKunjungan
import com.sapicare.app.data.model.StatusJadwal
import com.sapicare.app.data.model.StatusKeluhan
import com.sapicare.app.data.repository.JadwalRepository
import com.sapicare.app.data.repository.KeluhanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JadwalKunjunganViewModel @Inject constructor(
    private val jadwalRepository: JadwalRepository,
    private val keluhanRepository: KeluhanRepository
) : ViewModel() {

    private val _filterStatus = MutableStateFlow<StatusJadwal?>(null)
    val filterStatus: StateFlow<StatusJadwal?> = _filterStatus.asStateFlow()

    val jadwalList: StateFlow<List<JadwalKunjungan>> = combine(
        jadwalRepository.getAllJadwal(), _filterStatus
    ) { list, filter ->
        if (filter == null) list else list.filter { it.status == filter }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(s: StatusJadwal?) { _filterStatus.value = s }

    fun batalkanJadwal(jadwal: JadwalKunjungan) {
        viewModelScope.launch {

            jadwalRepository.updateStatusJadwal(
                jadwal.id,
                StatusJadwal.DIBATALKAN
            )

            if (jadwal.keluhanId.isNotBlank()) {
                keluhanRepository.updateStatusKeluhan(
                    jadwal.keluhanId,
                    StatusKeluhan.MENUNGGU
                )
            }
        }
    }
}
