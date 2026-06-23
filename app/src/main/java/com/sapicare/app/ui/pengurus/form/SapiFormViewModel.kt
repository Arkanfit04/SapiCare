package com.sapicare.app.ui.pengurus.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Sapi
import com.sapicare.app.data.model.UserSession
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SapiFormUiState(
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val nama: String = "",
    val jenisSapi: String = "",
    val jenisKelamin: String = "",
    val tanggalLahir: String = "",
    val namaPemilik: String = "",
    val wilayah: String = "",
    val keterangan: String = "",
    val status: String = "Sehat",
    val beratBadan: Double = 0.0,
    val jenisPerawatan: List<String> = emptyList(),
    val fotoUrl: String = "",
    val ownerId: String = "",
    val existingSapiId: String = ""
)

@HiltViewModel
class SapiFormViewModel @Inject constructor(
    private val repository: SapiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SapiFormUiState())
    val uiState: StateFlow<SapiFormUiState> = _uiState.asStateFlow()

    fun initNew(session: UserSession?) {
        _uiState.value = SapiFormUiState(
            ownerId = session?.uid ?: "",
            namaPemilik = session?.username ?: ""
        )
    }

    fun loadSapi(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val sapi = repository.getSapiById(id)
            if (sapi != null) {
                _uiState.value = SapiFormUiState(
                    isLoading = false,
                    nama = sapi.nama,
                    jenisSapi = sapi.jenisSapi,
                    jenisKelamin = sapi.jenisKelamin,
                    tanggalLahir = sapi.tanggalLahir,
                    namaPemilik = sapi.namaPemilik,
                    wilayah = sapi.wilayah,
                    keterangan = sapi.keterangan,
                    status = sapi.status,
                    beratBadan = sapi.beratBadan,
                    jenisPerawatan = sapi.jenisPerawatan,
                    fotoUrl = sapi.fotoUrl,
                    ownerId = sapi.ownerId,
                    existingSapiId = sapi.id
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Data tidak ditemukan")
            }
        }
    }

    fun onNamaChange(v: String) { _uiState.value = _uiState.value.copy(nama = v) }
    fun onJenisSapiChange(v: String) { _uiState.value = _uiState.value.copy(jenisSapi = v) }
    fun onJenisKelaminChange(v: String) { _uiState.value = _uiState.value.copy(jenisKelamin = v) }
    fun onTanggalLahirChange(v: String) { _uiState.value = _uiState.value.copy(tanggalLahir = v) }
    fun onNamaPemilikChange(v: String) { _uiState.value = _uiState.value.copy(namaPemilik = v) }
    fun onWilayahChange(v: String) { _uiState.value = _uiState.value.copy(wilayah = v) }
    fun onKeteranganChange(v: String) { _uiState.value = _uiState.value.copy(keterangan = v) }
    fun onStatusChange(v: String) { _uiState.value = _uiState.value.copy(status = v) }
    fun onBeratBadanChange(v: Double) { _uiState.value = _uiState.value.copy(beratBadan = v) }
    fun togglePerawatan(p: String) {
        val current = _uiState.value.jenisPerawatan.toMutableList()
        if (p in current) current.remove(p) else current.add(p)
        _uiState.value = _uiState.value.copy(jenisPerawatan = current)
    }

    fun saveSapi() {
        val s = _uiState.value
        if (s.nama.isBlank()) { _uiState.value = s.copy(error = "Nama sapi tidak boleh kosong"); return }
        if (s.jenisSapi.isBlank()) { _uiState.value = s.copy(error = "Jenis sapi harus dipilih"); return }
        if (s.jenisKelamin.isBlank()) { _uiState.value = s.copy(error = "Jenis kelamin harus dipilih"); return }
        if (s.tanggalLahir.isBlank()) { _uiState.value = s.copy(error = "Tanggal lahir harus dipilih"); return }
        if (s.namaPemilik.isBlank()) { _uiState.value = s.copy(error = "Nama pemilik tidak boleh kosong"); return }
        if (s.wilayah.isBlank()) { _uiState.value = s.copy(error = "Wilayah harus dipilih"); return }


        viewModelScope.launch {
            _uiState.value = s.copy(isLoading = true, error = null)
            try {
                val sapi = Sapi(
                    id = s.existingSapiId,
                    nama = s.nama,
                    jenisSapi = s.jenisSapi,
                    jenisKelamin = s.jenisKelamin,
                    tanggalLahir = s.tanggalLahir,
                    namaPemilik = s.namaPemilik,
                    wilayah = s.wilayah,
                    keterangan = s.keterangan,
                    status = s.status,
                    beratBadan = s.beratBadan,
                    jenisPerawatan = s.jenisPerawatan,
                    fotoUrl = s.fotoUrl,
                    ownerId = s.ownerId,
                    createdBy = s.ownerId,
                    updatedAt = System.currentTimeMillis()
                )
                if (s.existingSapiId.isEmpty()) repository.insertSapi(sapi)
                else repository.updateSapi(sapi)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Gagal menyimpan: ${e.message}")
            }
        }
    }
}
