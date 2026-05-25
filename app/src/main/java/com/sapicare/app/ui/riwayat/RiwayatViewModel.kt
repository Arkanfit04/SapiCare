package com.sapicare.app.ui.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.RiwayatKunjungan
import com.sapicare.app.data.repository.AuthRepository
import com.sapicare.app.data.repository.RiwayatRepository
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class RiwayatFormState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val tanggal: String = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).format(Date()),
    val kondisiSapi: String = "",
    val diagnosis: String = "",
    val tindakan: String = "",
    val obat: String = "",
    val catatan: String = "",
    val statusSapi: String = "Dalam Perawatan"  // status setelah kunjungan
)

@HiltViewModel
class RiwayatViewModel @Inject constructor(
    private val riwayatRepository: RiwayatRepository,
    private val sapiRepository: SapiRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val sessionFlow = authRepository.sessionFlow

    private val _sapiId = MutableStateFlow("")

    val riwayatList: StateFlow<List<RiwayatKunjungan>> = _sapiId
        .flatMapLatest { id ->
            if (id.isEmpty()) flowOf(emptyList())
            else riwayatRepository.getRiwayatBySapiId(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow(RiwayatFormState())
    val formState: StateFlow<RiwayatFormState> = _formState.asStateFlow()

    fun setSapiId(id: String) { _sapiId.value = id }

    fun onTanggalChange(v: String) { _formState.value = _formState.value.copy(tanggal = v) }
    fun onKondisiChange(v: String) { _formState.value = _formState.value.copy(kondisiSapi = v) }
    fun onDiagnosisChange(v: String) { _formState.value = _formState.value.copy(diagnosis = v) }
    fun onTindakanChange(v: String) { _formState.value = _formState.value.copy(tindakan = v) }
    fun onObatChange(v: String) { _formState.value = _formState.value.copy(obat = v) }
    fun onCatatanChange(v: String) { _formState.value = _formState.value.copy(catatan = v) }
    fun onStatusSapiChange(v: String) { _formState.value = _formState.value.copy(statusSapi = v) }

    fun resetForm() { _formState.value = RiwayatFormState() }

    fun saveRiwayat(sapiId: String) {
        val s = _formState.value
        if (s.kondisiSapi.isBlank()) { _formState.value = s.copy(error = "Kondisi sapi tidak boleh kosong"); return }
        if (s.diagnosis.isBlank()) { _formState.value = s.copy(error = "Diagnosis harus dipilih"); return }
        if (s.tindakan.isBlank()) { _formState.value = s.copy(error = "Tindakan harus dipilih"); return }

        viewModelScope.launch {
            _formState.value = s.copy(isLoading = true)
            val session = sessionFlow.firstOrNull()

            val riwayat = RiwayatKunjungan(
                sapiId = sapiId,
                tanggal = s.tanggal,
                kondisiSapi = s.kondisiSapi,
                diagnosis = s.diagnosis,
                tindakan = s.tindakan,
                obat = s.obat,
                catatan = s.catatan,
                namaPetugas = session?.username ?: "Petugas"
            )
            riwayatRepository.addRiwayat(riwayat)

            // Update status sapi berdasarkan pilihan
            val sapi = sapiRepository.getSapiById(sapiId)
            if (sapi != null) {
                sapiRepository.updateSapi(sapi.copy(status = s.statusSapi))
            }

            _formState.value = RiwayatFormState(isSaved = true)
        }
    }

    fun deleteRiwayat(id: String) {
        viewModelScope.launch { riwayatRepository.deleteRiwayat(id) }
    }

    fun clearError() { _formState.value = _formState.value.copy(error = null) }
}
