package com.example.fila_virtual.features.user.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserMenuViewModel : ViewModel() {
    private val productoRepo = ProductoRepository()

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    // Esta función recibe el ID del local que el cliente tocó y va a Firebase por su menú
    fun cargarMenu(establecimientoId: String) {
        viewModelScope.launch {
            productoRepo.getProductos(establecimientoId).collect { lista ->
                // Solo le mostramos al cliente los productos que estén marcados como "disponibles"
                _productos.value = lista.filter { it.enCatalogo && it.disponible }
            }
        }
    }
}