package com.sapicare.app.ui.pengurus.keluhan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.JadwalKunjungan
import com.sapicare.app.data.model.Keluhan
import com.sapicare.app.data.model.StatusKeluhan
import com.sapicare.app.data.repository.AuthRepository
import com.sapicare.app.data.repository.JadwalRepository
import com.sapicare.app.data.repository.KeluhanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailKeluhanUiState(
    val isLoading: Boolean = true,
    val isJadwalSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetailKeluhanViewModel @Inject constructor(
    private val keluhanRepository: KeluhanRepository,
    private val jadwalRepository: JadwalRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _keluhan = MutableStateFlow<Keluhan?>(null)
    val keluhan: StateFlow<Keluhan?> = _keluhan.asStateFlow()

    private val _uiState = MutableStateFlow(DetailKeluhanUiState())
    val uiState: StateFlow<DetailKeluhanUiState> = _uiState.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            keluhanRepository.getAllKeluhan().collect { list ->
                val k = list.find { it.id == id }
                _keluhan.value = k
                _uiState.value = DetailKeluhanUiState(isLoading = false)
            }
        }
    }

    fun jadwalkan(keluhan: Keluhan, tanggal: String, waktu: String, catatan: String) {
        viewModelScope.launch {
            val session = authRepository.sessionFlow.firstOrNull()
            val jadwal = JadwalKunjungan(
                keluhanId = keluhan.id,
                sapiId = keluhan.sapiId,
                namaSapi = keluhan.namaSapi,
                peternak = keluhan.peternak,
                peternakUid = keluhan.peternakUid,
                wilayah = keluhan.wilayah,
                tanggalKunjungan = tanggal,
                waktuKunjungan = waktu,
                catatan = catatan,
                pengurus = session?.username ?: "",
                pengurusUid = session?.uid ?: ""
            )
            jadwalRepository.addJadwal(jadwal)
            keluhanRepository.updateStatusKeluhan(keluhan.id, StatusKeluhan.DIJADWALKAN, catatan)
            _uiState.value = _uiState.value.copy(isJadwalSaved = true)
        }
    }
}
