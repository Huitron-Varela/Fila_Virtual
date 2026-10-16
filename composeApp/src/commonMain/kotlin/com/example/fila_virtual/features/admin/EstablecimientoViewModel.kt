package com.example.fila_virtual.features.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.repository.EstablecimientoRepository
import com.example.fila_virtual.repository.uploadImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EstablecimientoViewModel : ViewModel() {
    private val repository = EstablecimientoRepository()

    private val _uiState = MutableStateFlow<FormState>(FormState.Idle)
    val uiState: StateFlow<FormState> = _uiState

    private val _ownerUid = MutableStateFlow<String?>(null)
    private val _refreshVersion = MutableStateFlow(0)
    private val _isLoadingEstablecimientos = MutableStateFlow(true)
    val isLoadingEstablecimientos: StateFlow<Boolean> = _isLoadingEstablecimientos.asStateFlow()

    // Flow de establecimientos filtrados por dueño
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val establecimientos: StateFlow<List<Establecimiento>> = combine(
        _ownerUid,
        _refreshVersion
    ) { uid, _ -> uid }
        .flatMapLatest { uid ->
            if (uid != null) {
                repository.getEstablecimientosByOwner(uid)
            } else {
                repository.getEstablecimientos()
            }
        }
        .onStart { _isLoadingEstablecimientos.value = true }
        .onEach { _isLoadingEstablecimientos.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setOwnerUid(uid: String) {
        _ownerUid.value = uid
    }

    fun recargarEstablecimientos() {
        _isLoadingEstablecimientos.value = true
        _refreshVersion.value += 1
    }

    fun guardarEstablecimiento(
        establecimiento: Establecimiento,
        imageBytes: ByteArray? = null,
        imageMimeType: String = "image/jpeg",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = FormState.Loading
            val imageResult = imageBytes?.let {
                uploadImage(
                    path = "establecimientos/${establecimiento.id.ifEmpty { establecimiento.ownerUid + System.currentTimeMillis() }}",
                    bytes = it,
                    mimeType = imageMimeType
                )
            }
            if (imageResult?.isFailure == true) {
                _uiState.value = FormState.Error("No se pudo subir la imagen")
                return@launch
            }
            val establecimientoConImagen = imageResult?.getOrNull()?.let { establecimiento.copy(logoUrl = it) }
                ?: establecimiento
            val result = repository.guardarEstablecimiento(establecimientoConImagen)
            if (result.isSuccess) {
                _uiState.value = FormState.Success
                onSuccess()
            } else {
                _uiState.value = FormState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
            }
        }
    }

    fun actualizarEstado(id: String, activo: Boolean) {
        viewModelScope.launch {
            repository.actualizarEstado(id, activo)
        }
    }

    fun eliminarEstablecimiento(id: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = FormState.Loading
            val result = repository.eliminarEstablecimiento(id)
            if (result.isSuccess) {
                _uiState.value = FormState.Success
                onSuccess()
            } else {
                _uiState.value = FormState.Error(result.exceptionOrNull()?.message ?: "Error desconocido al eliminar")
            }
        }
    }

    fun resetState() {
        _uiState.value = FormState.Idle
    }
}

sealed class FormState {
    object Idle : FormState()
    object Loading : FormState()
    object Success : FormState()
    data class Error(val message: String) : FormState()
}
