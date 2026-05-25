package com.sapicare.app.ui.pengurus.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Sapi
import com.sapicare.app.data.repository.SapiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SapiViewModel @Inject constructor(
    private val sapiRepository: SapiRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val sapiList: StateFlow<List<Sapi>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) sapiRepository.getAllSapi()
            else sapiRepository.searchSapi(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
}
