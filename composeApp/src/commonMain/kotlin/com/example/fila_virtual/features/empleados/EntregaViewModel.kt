package com.example.fila_virtual.features.empleados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.coincideCodigoRecogida
import com.example.fila_virtual.repository.EmpleadoRepository
import com.example.fila_virtual.repository.EntregaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class EntregaViewModel(
    private val repository: EntregaRepository = EntregaRepository(),
    private val metricas: EmpleadoRepository = EmpleadoRepository()
) : ViewModel() {

    var pedidos by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set
    var pedidoEnCambio by mutableStateOf<String?>(null)
        private set
    var focoId by mutableStateOf<String?>(null)
        private set
    var cierreListo by mutableStateOf(0)
        private set
    var confirmacion by mutableStateOf("")
        private set

    private var establecimientoActual = ""
    private var escucha: Job? = null

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
            repository.escucharListos(establecimientoId)
                .catch { fallo ->
                    error = mensajeEntrega(fallo)
                    cargando = false
                }
                .collect { lista ->
                    pedidos = lista
                    cargando = false
                }
        }
    }

    fun limpiarAviso() {
        error = ""
    }

    fun entregarPorCodigo(
        codigo: String,
        establecimientoId: String,
        empleadoUid: String,
        sesionId: String
    ) {
        if (sesionId.isBlank() || pedidoEnCambio != null) return
        confirmacion = ""
        error = ""
        val coinciden = pedidos.filter { coincideCodigoRecogida(it, codigo) }
        when {
            coinciden.isEmpty() -> error = "Ese código no corresponde a un pedido listo."
            coinciden.size > 1 -> error = "Hay más de un pedido con ese código."
            else -> cerrar(
                coinciden.first(),
                EstadoPedido.ENTREGADO,
                establecimientoId,
                empleadoUid,
                sesionId
            )
        }
    }

    fun cerrar(
        pedido: Pedido,
        destino: EstadoPedido,
        establecimientoId: String,
        empleadoUid: String,
        sesionId: String
    ) {
        if (pedido.id.isBlank() || sesionId.isBlank() || pedidoEnCambio != null) return
        viewModelScope.launch {
            pedidoEnCambio = pedido.id
            error = ""
            confirmacion = ""
            val resultado = repository.cerrarEntrega(
                pedido.id,
                destino,
                establecimientoId,
                empleadoUid,
                sesionId
            )
            if (resultado.isFailure) {
                error = mensajeEntrega(resultado.exceptionOrNull())
            } else {
                if (destino == EstadoPedido.ENTREGADO) {
                    runCatching { metricas.sumarEntregado(empleadoUid) }
                } else if (destino == EstadoPedido.NO_RECLAMADO) {
                    runCatching { metricas.sumarAbandonado(empleadoUid) }
                }
                val indice = pedidos.indexOfFirst { it.id == pedido.id }
                val siguiente = pedidos.getOrNull(indice + 1)?.id
                    ?: pedidos.firstOrNull { it.id != pedido.id }?.id
                focoId = siguiente
                cierreListo += 1
                confirmacion = if (destino == EstadoPedido.ENTREGADO) {
                    "Turno ${pedido.turno} entregado"
                } else {
                    "Turno ${pedido.turno} marcado como abandonado"
                }
            }
            pedidoEnCambio = null
        }
    }
}

private fun mensajeEntrega(error: Throwable?): String {
    val texto = error?.message.orEmpty()
    return if (texto.contains("PERMISSION_DENIED", ignoreCase = true)) {
        "Tu cuenta no tiene permiso para actualizar este pedido."
    } else {
        texto.ifBlank { "No se pudo registrar la entrega" }
    }
}
