package com.example.fila_virtual.features.admin


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.features.admin.FormState
import com.example.fila_virtual.repository.ProductoRepository
import com.example.fila_virtual.repository.uploadImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductoViewModel : ViewModel() {
    private val repository = ProductoRepository()

    private val _uiState = MutableStateFlow<FormState>(FormState.Idle)
    val uiState: StateFlow<FormState> = _uiState

    private val _establecimientoId = MutableStateFlow<String?>(null)
    private val _ownerUid = MutableStateFlow<String?>(null)

    val productos: StateFlow<List<Producto>> = _establecimientoId
        .flatMapLatest { id ->
            val ownerId = _ownerUid.value
            if (id != null && id.isNotEmpty()) {
                repository.getProductos(id)
            } else if (ownerId != null) {
                repository.getProductosByOwner(ownerId)
            } else {
                MutableStateFlow(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setEstablecimientoId(id: String) {
        _establecimientoId.value = id
    }
    
    fun setOwnerUid(uid: String) {
        _ownerUid.value = uid
    }

    fun guardarProducto(
        id: String = "",
        establecimientoId: String,
        ownerUid: String,
        nombre: String,
        descripcion: String,
        precio: Double,
        categoria: String,
        imagenUrl: String = "",
        imageBytes: ByteArray? = null,
        imageMimeType: String = "image/jpeg",
        onSuccess: () -> Unit
    ) {
        if (nombre.isEmpty() || precio <= 0) {
            _uiState.value = FormState.Error("El nombre y el precio son obligatorios")
            return
        }

        viewModelScope.launch {
            _uiState.value = FormState.Loading
            val imageResult = imageBytes?.let {
                uploadImage(
                    path = "productos/${id.ifEmpty { ownerUid + System.currentTimeMillis() }}",
                    bytes = it,
                    mimeType = imageMimeType
                )
            }
            if (imageResult?.isFailure == true) {
                _uiState.value = FormState.Error("No se pudo subir la imagen")
                return@launch
            }

            val nuevoProducto = Producto(
                id = id,
                establecimientoId = establecimientoId,
                ownerUid = ownerUid,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                categoria = categoria,
                imagenUrl = imageResult?.getOrNull() ?: imagenUrl,
                disponible = true
            )

            val result = repository.guardarProducto(nuevoProducto)

            if (result.isSuccess) {
                _uiState.value = FormState.Success
                onSuccess()
            } else {
                _uiState.value = FormState.Error(result.exceptionOrNull()?.message ?: "Error al guardar el platillo")
            }
        }
    }

    fun actualizarDisponibilidad(productoId: String, disponible: Boolean) {
        viewModelScope.launch {
            repository.actualizarDisponibilidad(productoId, disponible)
        }
    }

    fun eliminarProducto(productoId: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = FormState.Loading
            val result = repository.eliminarProducto(productoId)
            if (result.isSuccess) {
                _uiState.value = FormState.Success
                onSuccess()
            } else {
                _uiState.value = FormState.Error(result.exceptionOrNull()?.message ?: "Error al eliminar el platillo")
            }
        }
    }

    fun resetState() {
        _uiState.value = FormState.Idle
    }
}