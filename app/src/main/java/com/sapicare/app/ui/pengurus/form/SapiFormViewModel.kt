package com.sapicare.app.ui.pengurus.form

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Sapi
import com.sapicare.app.data.remote.CloudinaryHelper
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class SapiFormState(
    val isLoading: Boolean = false,
    val isUploadingFoto: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    // Field lama
    val nama: String = "",
    val tanggalLahir: String = "",
    val keterangan: String = "",
    val jenisPerawatan: List<String> = emptyList(),
    val newPerawatan: String = "",
    val fotoUrl: String = "",
    val fotoUriLocal: Uri? = null,
    val enableReminder: Boolean = false,
    // Field baru
    val jenisSapi: String = "",
    val jenisKelamin: String = "",
    val namaPemilik: String = "",
    val wilayah: String = "",
    // Computed
    val umur: String = ""
)

@HiltViewModel
class SapiFormViewModel @Inject constructor(
    private val repository: SapiRepository,
    private val cloudinaryHelper: CloudinaryHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(SapiFormState())
    val state: StateFlow<SapiFormState> = _state.asStateFlow()

    private var editingSapiId: String? = null

    fun loadSapi(id: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val sapi = repository.getSapiById(id)
            if (sapi != null) {
                editingSapiId = sapi.id
                _state.value = SapiFormState(
                    nama = sapi.nama,
                    jenisSapi = sapi.jenisSapi,
                    jenisKelamin = sapi.jenisKelamin,
                    tanggalLahir = sapi.tanggalLahir,
                    namaPemilik = sapi.namaPemilik,
                    wilayah = sapi.wilayah,
                    keterangan = sapi.keterangan,
                    jenisPerawatan = sapi.jenisPerawatan,
                    fotoUrl = sapi.fotoUrl,
                    umur = hitungUmur(sapi.tanggalLahir)
                )
            }
        }
    }

    fun onNamaChange(v: String) { _state.value = _state.value.copy(nama = v) }
    fun onJenisSapiChange(v: String) { _state.value = _state.value.copy(jenisSapi = v) }
    fun onJenisKelaminChange(v: String) { _state.value = _state.value.copy(jenisKelamin = v) }
    fun onNamaPemilikChange(v: String) { _state.value = _state.value.copy(namaPemilik = v) }
    fun onWilayahChange(v: String) { _state.value = _state.value.copy(wilayah = v) }
    fun onKeteranganChange(v: String) { _state.value = _state.value.copy(keterangan = v) }
    fun onNewPerawatanChange(v: String) { _state.value = _state.value.copy(newPerawatan = v) }
    fun onReminderToggle(v: Boolean) { _state.value = _state.value.copy(enableReminder = v) }
    fun onFotoSelected(uri: Uri) { _state.value = _state.value.copy(fotoUriLocal = uri) }
    fun onFotoRemoved() { _state.value = _state.value.copy(fotoUriLocal = null, fotoUrl = "") }

    fun onTanggalLahirChange(v: String) {
        _state.value = _state.value.copy(
            tanggalLahir = v,
            umur = hitungUmur(v)
        )
    }

    fun addPerawatan(item: String) {
        if (item.isNotEmpty() && !_state.value.jenisPerawatan.contains(item)) {
            _state.value = _state.value.copy(
                jenisPerawatan = _state.value.jenisPerawatan + item,
                newPerawatan = ""
            )
        }
    }

    fun addPerawatanCustom() {
        val new = _state.value.newPerawatan.trim()
        if (new.isNotEmpty() && !_state.value.jenisPerawatan.contains(new)) {
            _state.value = _state.value.copy(
                jenisPerawatan = _state.value.jenisPerawatan + new,
                newPerawatan = ""
            )
        }
    }

    fun removePerawatan(item: String) {
        _state.value = _state.value.copy(jenisPerawatan = _state.value.jenisPerawatan - item)
    }

    private fun hitungUmur(tanggalLahir: String): String {
        if (tanggalLahir.isBlank()) return ""
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
            val lahir = sdf.parse(tanggalLahir) ?: return ""
            val sekarang = Calendar.getInstance()
            val lahirCal = Calendar.getInstance().apply { time = lahir }

            var tahun = sekarang.get(Calendar.YEAR) - lahirCal.get(Calendar.YEAR)
            var bulan = sekarang.get(Calendar.MONTH) - lahirCal.get(Calendar.MONTH)

            if (bulan < 0) { tahun--; bulan += 12 }

            when {
                tahun > 0 && bulan > 0 -> "$tahun tahun $bulan bulan"
                tahun > 0 -> "$tahun tahun"
                bulan > 0 -> "$bulan bulan"
                else -> "< 1 bulan"
            }
        } catch (e: Exception) { "" }
    }

    fun saveSapi() {
        val s = _state.value
        if (s.nama.isBlank()) { _state.value = s.copy(error = "Nama sapi tidak boleh kosong"); return }
        if (s.jenisSapi.isBlank()) { _state.value = s.copy(error = "Jenis sapi harus dipilih"); return }
        if (s.jenisKelamin.isBlank()) { _state.value = s.copy(error = "Jenis kelamin harus dipilih"); return }
        if (s.tanggalLahir.isBlank()) { _state.value = s.copy(error = "Tanggal lahir tidak boleh kosong"); return }
        if (s.namaPemilik.isBlank()) { _state.value = s.copy(error = "Nama pemilik tidak boleh kosong"); return }
        if (s.wilayah.isBlank()) { _state.value = s.copy(error = "Wilayah harus dipilih"); return }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true)

            var fotoUrl = s.fotoUrl
            if (s.fotoUriLocal != null) {
                try {
                    _state.value = _state.value.copy(isUploadingFoto = true)
                    fotoUrl = cloudinaryHelper.uploadImage(s.fotoUriLocal)
                    _state.value = _state.value.copy(isUploadingFoto = false)
                } catch (e: Exception) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isUploadingFoto = false,
                        error = "Gagal upload foto: ${e.message}"
                    )
                    return@launch
                }
            }

            val id = editingSapiId
            val sapi = Sapi(
                id = id ?: "",
                nama = s.nama.trim(),
                jenisSapi = s.jenisSapi,
                jenisKelamin = s.jenisKelamin,
                tanggalLahir = s.tanggalLahir,
                namaPemilik = s.namaPemilik.trim(),
                wilayah = s.wilayah,
                keterangan = s.keterangan.trim(),
                jenisPerawatan = s.jenisPerawatan,
                fotoUrl = fotoUrl,
                updatedAt = System.currentTimeMillis()
            )

            if (id != null) repository.updateSapi(sapi)
            else repository.insertSapi(sapi)

            _state.value = _state.value.copy(isLoading = false, isSaved = true)
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
}
