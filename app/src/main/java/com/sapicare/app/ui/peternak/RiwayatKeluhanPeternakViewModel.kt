package com.sapicare.app.ui.peternak

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Keluhan
import com.sapicare.app.data.model.UserSession
import com.sapicare.app.data.repository.KeluhanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RiwayatKeluhanPeternakViewModel @Inject constructor(
    private val keluhanRepository: KeluhanRepository
) : ViewModel() {

    private val _uid = MutableStateFlow("")

    val keluhanList: StateFlow<List<Keluhan>> = _uid
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(emptyList())
            else keluhanRepository.getKeluhanByPeternak(uid)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun init(session: UserSession?) { _uid.value = session?.uid ?: "" }
}
