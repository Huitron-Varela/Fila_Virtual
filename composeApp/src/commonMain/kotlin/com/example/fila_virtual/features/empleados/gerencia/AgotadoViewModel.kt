package com.example.fila_virtual.features.empleados.gerencia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.repository.ProductoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AgotadoViewModel(
    private val repository: ProductoRepository = ProductoRepository()
) : ViewModel() {

    var productos by mutableStateOf<List<Producto>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set
    var productoEnCambio by mutableStateOf<String?>(null)
        private set

    private var establecimientoActual = ""
    private var escucha: Job? = null

    fun escuchar(establecimientoId: String) {
        if (establecimientoId == establecimientoActual && escucha?.isActive == true) return
        establecimientoActual = establecimientoId
        escucha?.cancel()
        if (establecimientoId.isBlank()) {
            productos = emptyList()
            cargando = false
            return
        }
        escucha = viewModelScope.launch {
            cargando = true
            error = ""
            repository.getProductos(establecimientoId)
                .catch { fallo ->
                    error = mensajeAgotado(fallo)
                    cargando = false
                }
                .collect { lista ->
                    productos = lista
                        .filter { it.enCatalogo }
                        .sortedBy { it.nombre.lowercase() }
                    cargando = false
                }
        }
    }

    fun cambiarDisponibilidad(producto: Producto) {
        if (producto.id.isBlank() || productoEnCambio != null) return
        viewModelScope.launch {
            productoEnCambio = producto.id
            error = ""
            val resultado = repository.actualizarDisponibilidad(producto.id, !producto.disponible)
            if (resultado.isFailure) {
                error = mensajeAgotado(resultado.exceptionOrNull())
            }
            productoEnCambio = null
        }
    }
}

private fun mensajeAgotado(error: Throwable?): String {
    val texto = error?.message.orEmpty()
    return if (texto.contains("PERMISSION_DENIED", ignoreCase = true)) {
        "Tu cuenta no tiene permiso para cambiar la disponibilidad de este producto."
    } else {
        texto.ifBlank { "No se pudo actualizar el producto" }
    }
}
