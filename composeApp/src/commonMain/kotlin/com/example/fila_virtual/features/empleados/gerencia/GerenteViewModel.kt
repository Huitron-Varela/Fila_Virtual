package com.example.fila_virtual.features.empleados.gerencia

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.repository.CocinaRepository
import com.example.fila_virtual.repository.ProductoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class GerenteViewModel(
    private val repository: ProductoRepository = ProductoRepository()
) : ViewModel() {

    var ownerUid by mutableStateOf("")
        private set
    var cargandoOwner by mutableStateOf(true)
        private set
    var errorOwner by mutableStateOf("")
        private set
    var pedidos by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var errorCola by mutableStateOf("")
        private set

    private val cocina = CocinaRepository()
    private var establecimientoActual = ""
    private var escuchaCola: Job? = null
    private var cargaOwner: Job? = null

    fun escuchar(establecimientoId: String) {
        if (establecimientoId == establecimientoActual && escuchaCola?.isActive == true) return
        establecimientoActual = establecimientoId
        escuchaCola?.cancel()
        cargaOwner?.cancel()
        ownerUid = ""
        cargandoOwner = true
        errorOwner = ""
        if (establecimientoId.isBlank()) {
            pedidos = emptyList()
            cargandoOwner = false
            return
        }
        cargaOwner = viewModelScope.launch {
            cargandoOwner = true
            ownerUid = repository.leerOwnerUid(establecimientoId)
            cargandoOwner = false
            if (ownerUid.isBlank()) errorOwner = "No se pudo verificar el dueño del local."
        }
        escuchaCola = viewModelScope.launch {
            cocina.escucharActivos(establecimientoId)
                .catch { fallo ->
                    errorCola = fallo.message ?: "No se pudo cargar la cola de pedidos."
                    pedidos = emptyList()
                }
                .collect { lista ->
                    errorCola = ""
                    pedidos = lista
                }
        }
    }
}
