package com.sapicare.app.ui.pengurus.jadwal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.RiwayatKunjungan
import com.sapicare.app.data.model.StatusJadwal
import com.sapicare.app.data.model.StatusKeluhan
import com.sapicare.app.data.repository.AuthRepository
import com.sapicare.app.data.repository.JadwalRepository
import com.sapicare.app.data.repository.KeluhanRepository
import com.sapicare.app.data.repository.RiwayatRepository
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TindakLanjutFormState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val namaSapi: String = "",
    val tanggalKunjungan: String = "",
    val kondisiSapi: String = "",
    val diagnosis: String = "",
    val tindakan: String = "",
    val obat: String = "",
    val catatan: String = "",
    val statusSapi: String = "Sehat",
    val keluhanId: String = ""
)
@HiltViewModel
class TindakLanjutViewModel @Inject constructor(
    private val jadwalRepository: JadwalRepository,
    private val riwayatRepository: RiwayatRepository,
    private val sapiRepository: SapiRepository,
    private val keluhanRepository: KeluhanRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _jadwalId = MutableStateFlow("")
    private val _sapiId = MutableStateFlow("")
    private val _formState = MutableStateFlow(TindakLanjutFormState())
    val formState: StateFlow<TindakLanjutFormState> = _formState.asStateFlow()

    fun init(jadwalId: String, sapiId: String) {
        _jadwalId.value = jadwalId
        _sapiId.value = sapiId
        viewModelScope.launch {
            jadwalRepository.getAllJadwal().collect { list ->
                val jadwal = list.find { it.id == jadwalId } ?: return@collect
                val sapi = sapiRepository.getSapiById(sapiId)
                _formState.value = _formState.value.copy(
                    namaSapi = jadwal.namaSapi,
                    tanggalKunjungan = jadwal.tanggalKunjungan,
                    keluhanId = jadwal.keluhanId,
                    kondisiSapi = sapi?.status ?: ""
                )
            }
        }
    }

    fun onKondisiChange(v: String) { _formState.value = _formState.value.copy(kondisiSapi = v) }
    fun onDiagnosisChange(v: String) { _formState.value = _formState.value.copy(diagnosis = v) }
    fun onTindakanChange(v: String) { _formState.value = _formState.value.copy(tindakan = v) }
    fun onObatChange(v: String) { _formState.value = _formState.value.copy(obat = v) }
    fun onCatatanChange(v: String) { _formState.value = _formState.value.copy(catatan = v) }
    fun onStatusSapiChange(v: String) { _formState.value = _formState.value.copy(statusSapi = v) }

    fun simpan() {
        val s = _formState.value
        if (s.kondisiSapi.isBlank()) { _formState.value = s.copy(error = "Kondisi sapi wajib diisi"); return }
        if (s.diagnosis.isBlank()) { _formState.value = s.copy(error = "Diagnosis harus dipilih"); return }
        if (s.tindakan.isBlank()) { _formState.value = s.copy(error = "Tindakan harus dipilih"); return }

        viewModelScope.launch {
            _formState.value = s.copy(isLoading = true)
            val session = authRepository.sessionFlow.firstOrNull()
            val riwayat = RiwayatKunjungan(
                sapiId = _sapiId.value,
                jadwalId = _jadwalId.value,
                keluhanId = s.keluhanId,
                tanggal = s.tanggalKunjungan,
                kondisiSapi = s.kondisiSapi,
                diagnosis = s.diagnosis,
                tindakan = s.tindakan,
                obat = s.obat,
                catatan = s.catatan,
                namaPetugas = session?.username ?: "Petugas"
            )
            riwayatRepository.addRiwayat(riwayat)
            // Update status sapi
            sapiRepository.getSapiById(_sapiId.value)?.let {
                sapiRepository.updateSapi(it.copy(status = s.statusSapi))
            }
            // Selesaikan jadwal
            jadwalRepository.updateStatusJadwal(_jadwalId.value, StatusJadwal.SELESAI)
            // Update keluhan jadi selesai
            if (s.keluhanId.isNotEmpty()) {
                keluhanRepository.updateStatusKeluhan(s.keluhanId, StatusKeluhan.SELESAI)
            }
            _formState.value = s.copy(isLoading = false, isSaved = true)
        }
    }
}
