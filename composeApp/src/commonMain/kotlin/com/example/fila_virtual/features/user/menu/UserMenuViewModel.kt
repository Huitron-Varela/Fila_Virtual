package com.example.fila_virtual.features.user.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.repository.ProductoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class UserMenuViewModel : ViewModel() {
    private val productoRepo = ProductoRepository()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando
    private val _error = MutableStateFlow(false)
    val error: StateFlow<Boolean> = _error
    private var menuJob: Job? = null

    // Esta función recibe el ID del local que el cliente tocó y va a Firebase por su menú
    fun cargarMenu(establecimientoId: String) {
        menuJob?.cancel()
        _error.value = false
        menuJob = viewModelScope.launch {
            productoRepo.getProductos(establecimientoId)
                .onStart { _cargando.value = true }
                .onEach { lista ->
                    _productos.value = lista.filter { it.enCatalogo && it.disponible }
                    _cargando.value = false
                }
                .catch {
                    _error.value = true
                    _cargando.value = false
                }
                .collect { }
        }
    }
}