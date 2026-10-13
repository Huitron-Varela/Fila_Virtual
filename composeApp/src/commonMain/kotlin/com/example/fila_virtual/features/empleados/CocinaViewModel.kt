package com.example.fila_virtual.features.empleados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.repository.CocinaRepository
import com.example.fila_virtual.repository.EmpleadoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CocinaViewModel(
    private val repository: CocinaRepository = CocinaRepository(),
    private val metricas: EmpleadoRepository = EmpleadoRepository()
) : ViewModel() {

    var pedidos by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set
    var pedidosEnCambio by mutableStateOf(setOf<String>())
        private set

    private var establecimientoActual = ""
    private var escucha: Job? = null

    val recibidos: Int
        get() = pedidos.count { it.estado == EstadoPedido.PENDIENTE || it.estado == EstadoPedido.RECIBIDO }

    val enPreparacion: Int
        get() = pedidos.count { it.estado == EstadoPedido.EN_PREPARACION }

    val listos: Int
        get() = pedidos.count { it.estado == EstadoPedido.LISTO }

    val porElaborar: List<Pair<String, Int>>
        get() = pedidos
            .filter { it.estado == EstadoPedido.PENDIENTE || it.estado == EstadoPedido.RECIBIDO || it.estado == EstadoPedido.EN_PREPARACION }
            .flatMap { it.productos }
            .groupBy { it.nombre }
            .map { (nombre, productos) -> nombre to productos.sumOf { it.cantidad } }
            .sortedByDescending { it.second }

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
            repository.escucharActivos(establecimientoId)
                .catch { fallo ->
                    error = fallo.message ?: "No se pudo cargar la cola"
                    cargando = false
                }
                .collect { lista ->
                    pedidos = lista
                    cargando = false
                }
        }
    }

    fun avanzar(pedido: Pedido, empleadoUid: String, sesionId: String) {
        if (pedido.id.isBlank() || empleadoUid.isBlank() || sesionId.isBlank() || pedido.id in pedidosEnCambio) return
        viewModelScope.launch {
            pedidosEnCambio = pedidosEnCambio + pedido.id
            error = ""
            val resultado = repository.avanzarEstado(pedido.id, empleadoUid, sesionId)
            if (resultado.isFailure) {
                error = resultado.exceptionOrNull()?.message ?: "No se pudo actualizar el pedido"
            } else if (resultado.getOrNull() == EstadoPedido.LISTO) {
                runCatching { metricas.sumarPreparado(empleadoUid) }
            }
            pedidosEnCambio = pedidosEnCambio - pedido.id
        }
    }
}
