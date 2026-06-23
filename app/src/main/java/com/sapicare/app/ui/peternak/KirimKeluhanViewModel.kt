package com.sapicare.app.ui.peternak

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Keluhan
import com.sapicare.app.data.model.NotificationItem
import com.sapicare.app.data.model.Sapi
import com.sapicare.app.data.model.UserSession
import com.sapicare.app.data.repository.AuthRepository
import com.sapicare.app.data.repository.KeluhanRepository
import com.sapicare.app.data.repository.NotificationRepository
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class KirimKeluhanFormState(
    val isLoading: Boolean = false,
    val isSent: Boolean = false,
    val error: String? = null,
    val sapiId: String = "",
    val namaSapi: String = "",
    val deskripsiKeluhan: String = "",
    val gejala: List<String> = emptyList(),
    val tanggalKeluhan: String = SimpleDateFormat("dd/MM/yyyy", Locale("id","ID")).format(Date()),
    val usulanTanggalKunjungan: String = ""
)

@HiltViewModel
class KirimKeluhanViewModel @Inject constructor(
    private val sapiRepository: SapiRepository,
    private val keluhanRepository: KeluhanRepository,
    private val authRepository: AuthRepository,
    private val notificationRepository: NotificationRepository

) : ViewModel() {

    private val _ownerId = MutableStateFlow("")
    private val _formState = MutableStateFlow(KirimKeluhanFormState())
    val formState: StateFlow<KirimKeluhanFormState> = _formState.asStateFlow()

    val sapiSaya: StateFlow<List<Sapi>> = _ownerId
        .flatMapLatest { id ->
            if (id.isEmpty()) flowOf(emptyList())
            else sapiRepository.getSapiByOwner(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun init(session: UserSession?) { _ownerId.value = session?.uid ?: "" }
    fun onSapiSelected(id: String, nama: String) { _formState.value = _formState.value.copy(sapiId = id, namaSapi = nama) }
    fun onDeskripsiChange(v: String) { _formState.value = _formState.value.copy(deskripsiKeluhan = v) }
    fun onTanggalChange(v: String) { _formState.value = _formState.value.copy(tanggalKeluhan = v) }
    fun onUsulanTanggalChange(v: String) { _formState.value = _formState.value.copy(usulanTanggalKunjungan = v) }
    fun toggleGejala(g: String) {
        val current = _formState.value.gejala.toMutableList()
        if (g in current) current.remove(g) else current.add(g)
        _formState.value = _formState.value.copy(gejala = current)
    }

    fun kirimKeluhan(session: UserSession?) {
        val s = _formState.value
        if (s.sapiId.isBlank()) { _formState.value = s.copy(error = "Pilih sapi terlebih dahulu"); return }
        if (s.deskripsiKeluhan.isBlank()) { _formState.value = s.copy(error = "Deskripsi keluhan tidak boleh kosong"); return }

        viewModelScope.launch {
            _formState.value = s.copy(isLoading = true, error = null)
            val sapi = sapiRepository.getSapiById(s.sapiId)
            val keluhan = Keluhan(
                sapiId = s.sapiId,
                namaSapi = s.namaSapi,
                peternak = session?.username ?: "",
                peternakUid = session?.uid ?: "",
                wilayah = sapi?.wilayah ?: "",
                deskripsiKeluhan = s.deskripsiKeluhan,
                gejala = s.gejala,
                tanggalKeluhan = s.tanggalKeluhan,
                usulanTanggalKunjungan = s.usulanTanggalKunjungan
            )
            keluhanRepository.addKeluhan(keluhan)

            val pengurusList = authRepository.getApprovedPengurus()

            Log.d(
                "KELUHAN",
                "jumlah pengurus = ${pengurusList.size}"
            )

            Log.d("KELUHAN", "Mulai kirim")

            pengurusList.forEach { pengurus ->

                Log.d(
                    "KELUHAN",
                    "Notif ke ${pengurus.uid}"
                )

                notificationRepository.addNotificationWithPush(
                    NotificationItem(
                        targetUid = pengurus.uid,
                        title = "Keluhan Baru",
                        message = "${session?.username} mengirim keluhan untuk ${s.namaSapi}",
                        type = "KELUHAN"
                    )
                )
            }
            _formState.value = KirimKeluhanFormState(isSent = true)
        }
    }

    fun resetForm() { _formState.value = KirimKeluhanFormState() }
}
