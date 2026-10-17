package com.example.fila_virtual.features.empleados.comun

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.AsignacionEmpleado
import com.example.fila_virtual.data.EstacionOperativa
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.ResumenTurno
import com.example.fila_virtual.data.SesionTurno
import com.example.fila_virtual.repository.EmpleadoRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

class EmpleadoTurnoViewModel(
    private val repository: EmpleadoRepository = EmpleadoRepository()
) : ViewModel() {

    var asignacion by mutableStateOf<AsignacionEmpleado?>(null)
        private set
    var cargando by mutableStateOf(false)
        private set
    var guardando by mutableStateOf(false)
        private set
    var sesion by mutableStateOf<SesionTurno?>(null)
        private set
    var estadisticasSesion by mutableStateOf<ResumenTurno?>(null)
        private set
    var pedidosEntregadosRecientes by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var error by mutableStateOf("")
        private set
    private var estadisticasJob: kotlinx.coroutines.Job? = null
    private var ultimoPedidoJob: kotlinx.coroutines.Job? = null
    private var ultimoPedidoKey = ""

    val estaciones: List<EstacionOperativa>
        get() = EstacionOperativa.desdeRoles(asignacion?.roles.orEmpty())

    fun cargar(uid: String) {
        if (uid.isBlank()) return
        estadisticasJob?.cancel()
        estadisticasJob = viewModelScope.launch {
            repository.observarEstadisticasSesion(uid)
                .catch { estadisticasSesion = null }
                .collect { estadisticasSesion = it }
        }
        viewModelScope.launch {
            cargando = true
            error = ""
            val resultado = repository.buscarAsignacion(uid)
            if (resultado.isSuccess) {
                asignacion = resultado.getOrNull()
                sesion = asignacion?.let { actual ->
                    repository.buscarSesionActiva(uid, actual.establecimientoId)
                }
                cargarUltimoPedidoEntregado(uid, asignacion?.establecimientoId.orEmpty())
            } else {
                asignacion = null
                sesion = null
                cargarUltimoPedidoEntregado(uid, "")
                error = mensajeError(resultado.exceptionOrNull(), "No se pudo consultar tu establecimiento")
            }
            cargando = false
        }
    }

    private fun cargarUltimoPedidoEntregado(uid: String, establecimientoId: String) {
        val key = "$uid:$establecimientoId"
        if (key == ultimoPedidoKey) return
        ultimoPedidoKey = key
        ultimoPedidoJob?.cancel()
        pedidosEntregadosRecientes = emptyList()
        if (uid.isBlank() || establecimientoId.isBlank()) return

        ultimoPedidoJob = viewModelScope.launch {
            val pedidos = repository.obtenerPedidosEntregadosEmpleado(uid, listOf(establecimientoId))
                .getOrNull()
                ?.take(3)
                .orEmpty()
            if (ultimoPedidoKey == key) pedidosEntregadosRecientes = pedidos
        }
    }

    fun iniciar(uid: String, estacion: EstacionOperativa, onAbierto: (guardada: Boolean) -> Unit) {
        val asignacionActual = asignacion
        if (uid.isBlank() || asignacionActual == null) {
            sesion = null
            onAbierto(false)
            return
        }
        viewModelScope.launch {
            guardando = true
            error = ""
            sesion?.let { repository.cerrarSesion(it) }
            val resultado = repository.iniciarSesion(uid, asignacionActual, estacion)
            if (resultado.isSuccess) {
                sesion = resultado.getOrNull()
                onAbierto(true)
            } else {
                error = mensajeError(resultado.exceptionOrNull(), "No se pudo iniciar el turno")
            }
            guardando = false
        }
    }

    fun terminar(onCerrado: (ResumenTurno?) -> Unit) {
        val sesionActual = sesion
        if (sesionActual == null) {
            onCerrado(null)
            return
        }
        viewModelScope.launch {
            guardando = true
            error = ""
            val resultado = repository.cerrarSesion(sesionActual)
            if (resultado.isSuccess) {
                sesion = null
                onCerrado(resultado.getOrNull())
            } else {
                error = mensajeError(resultado.exceptionOrNull(), "No se pudo cerrar el turno")
            }
            guardando = false
        }
    }
}

private fun mensajeError(error: Throwable?, respaldo: String): String {
    val texto = error?.message.orEmpty()
    return if (texto.contains("PERMISSION_DENIED", ignoreCase = true)) {
        "Tu cuenta no tiene permiso para guardar este turno."
    } else {
        texto.ifBlank { respaldo }
    }
}
