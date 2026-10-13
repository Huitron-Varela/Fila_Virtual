package com.example.fila_virtual.features.empleados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.repository.CajeroRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CajeroViewModel(
    private val repository: CajeroRepository = CajeroRepository()
) : ViewModel() {

    var pedidos by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set

    private var establecimientoActual = ""
    private var escucha: Job? = null

    val incidencias: Int
        get() = pedidos.count { esIncidencia(it.estado) }

    val cobrado: Double
        get() = pedidos
            .filter { it.estado != EstadoPedido.CANCELADO }
            .sumOf { it.total }

    fun escuchar(establecimientoId: String) {
        if (establecimientoId == establecimientoActual && escucha?.isActive == true) return
        establecimientoActual = establecimientoId
        escucha?.cancel()
        if (establecimientoId.isBlank()) {
            pedidos = emptyList()
            cargando = false
            return
        }
        escucha = viewModelScope.launch {
            cargando = true
            error = ""
            repository.escuchar(establecimientoId)
                .catch { fallo ->
                    error = fallo.message ?: "No se pudieron consultar las transacciones"
                    cargando = false
                }
                .collect { lista ->
                    pedidos = lista
                    cargando = false
                }
        }
    }
}

fun esIncidencia(estado: EstadoPedido): Boolean {
    return estado == EstadoPedido.NO_RECLAMADO || estado == EstadoPedido.CANCELADO
}
