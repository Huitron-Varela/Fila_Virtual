package com.example.fila_virtual.features.empleados

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.repository.CocinaRepository
import com.example.fila_virtual.repository.ProductoRepository
import com.example.fila_virtual.repository.uploadImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class GerenteViewModel(
    private val repository: ProductoRepository = ProductoRepository()
) : ViewModel() {

    var productos by mutableStateOf<List<Producto>>(emptyList())
        private set
    var cargando by mutableStateOf(false)
        private set
    var guardando by mutableStateOf(false)
        private set
    var error by mutableStateOf("")
        private set
    var aviso by mutableStateOf("")
        private set
    var productoEnCambio by mutableStateOf<String?>(null)
        private set
    var pedidos by mutableStateOf<List<Pedido>>(emptyList())
        private set
    var errorCola by mutableStateOf("")
        private set

    private val cocina = CocinaRepository()
    private var establecimientoActual = ""
    private var escucha: Job? = null
    private var escuchaCola: Job? = null

    fun escuchar(establecimientoId: String) {
        if (establecimientoId == establecimientoActual && escucha?.isActive == true) return
        establecimientoActual = establecimientoId
        escucha?.cancel()
        escuchaCola?.cancel()
        if (establecimientoId.isBlank()) {
            productos = emptyList()
            pedidos = emptyList()
            cargando = false
            return
        }
        escucha = viewModelScope.launch {
            cargando = true
            error = ""
            repository.getProductos(establecimientoId)
                .catch { fallo ->
                    error = mensajeMenu(fallo)
                    cargando = false
                }
                .collect { lista ->
                    productos = lista
                        .filter { it.establecimientoId == establecimientoId && it.enCatalogo }
                        .sortedWith(compareBy({ it.categoria.lowercase() }, { it.nombre.lowercase() }))
                    cargando = false
                }
        }
        escuchaCola = viewModelScope.launch {
            cocina.escucharActivos(establecimientoId)
                .catch { fallo ->
                    errorCola = mensajeMenu(fallo)
                    pedidos = emptyList()
                }
                .collect { lista ->
                    errorCola = ""
                    pedidos = lista
                }
        }
    }

    fun cambiarDisponibilidad(producto: Producto) {
        if (producto.id.isBlank() || productoEnCambio != null || guardando) return
        if (producto.establecimientoId != establecimientoActual) {
            error = "Ese platillo no es de tu establecimiento."
            return
        }
        viewModelScope.launch {
            productoEnCambio = producto.id
            error = ""
            aviso = ""
            val resultado = repository.actualizarDisponibilidad(producto.id, !producto.disponible)
            if (resultado.isFailure) {
                error = mensajeMenu(resultado.exceptionOrNull())
            }
            productoEnCambio = null
        }
    }

    fun darDeBaja(producto: Producto, onListo: (Boolean) -> Unit = {}) {
        if (producto.id.isBlank() || guardando || productoEnCambio != null) return
        if (producto.establecimientoId != establecimientoActual || !producto.enCatalogo) {
            error = "Ese platillo no es de tu establecimiento."
            return
        }
        viewModelScope.launch {
            guardando = true
            error = ""
            aviso = ""
            val resultado = repository.darDeBaja(producto.id)
            guardando = false
            if (resultado.isSuccess) {
                aviso = "El platillo salió del menú. Los pedidos anteriores se conservan."
                onListo(true)
            } else {
                error = mensajeMenu(resultado.exceptionOrNull())
                onListo(false)
            }
        }
    }

    fun guardar(
        productoId: String,
        nombre: String,
        descripcion: String,
        precioTexto: String,
        categoria: String,
        imagenBytes: ByteArray? = null,
        imagenMime: String = "image/jpeg",
        imagenUrlActual: String = "",
        onGuardado: () -> Unit
    ) {
        if (establecimientoActual.isBlank() || guardando) return
        val nombreLimpio = nombre.trim()
        val categoriaLimpia = categoria.trim()
        val precio = parsePrecio(precioTexto)
        if (nombreLimpio.isEmpty() || categoriaLimpia.isEmpty() || precio == null) {
            error = "Escribe el nombre, una categoría y un precio mayor a cero."
            return
        }
        val existente = if (productoId.isBlank()) {
            null
        } else {
            productos.find { it.id == productoId && it.establecimientoId == establecimientoActual }
        }
        if (productoId.isNotBlank() && existente == null) {
            error = "Ese platillo no es de tu establecimiento."
            return
        }
        viewModelScope.launch {
            guardando = true
            error = ""
            aviso = ""
            val imagenNueva = if (imagenBytes == null) {
                ""
            } else {
                val destino = productoId.ifBlank {
                    "$establecimientoActual-${Clock.System.now().toEpochMilliseconds()}"
                }
                val subida = uploadImage("productos/$destino", imagenBytes, imagenMime)
                if (subida.isFailure) {
                    guardando = false
                    error = mensajeMenu(subida.exceptionOrNull()).let { texto ->
                        if (texto.contains("negó")) texto else "No se pudo subir la foto. El platillo no se guardó."
                    }
                    return@launch
                }
                subida.getOrNull().orEmpty().ifBlank { imagenUrlActual }
            }
            val resultado = if (existente == null) {
                val owner = productos.firstOrNull { it.ownerUid.isNotBlank() }?.ownerUid
                    ?: repository.leerOwnerUid(establecimientoActual)
                repository.crearPlatillo(
                    establecimientoId = establecimientoActual,
                    ownerUid = owner,
                    nombre = nombreLimpio,
                    descripcion = descripcion.trim(),
                    precio = precio,
                    categoria = categoriaLimpia,
                    imagenUrl = imagenNueva.ifBlank { imagenUrlActual }
                )
            } else {
                repository.actualizarDatosMenu(
                    productoId = existente.id,
                    nombre = nombreLimpio,
                    descripcion = descripcion.trim(),
                    precio = precio,
                    categoria = categoriaLimpia,
                    imagenUrl = imagenNueva
                )
            }
            guardando = false
            if (resultado.isSuccess) {
                aviso = "Platillo guardado."
                onGuardado()
            } else {
                error = mensajeMenu(resultado.exceptionOrNull())
            }
        }
    }
}

private fun parsePrecio(texto: String): Double? {
    val limpio = texto.trim().replace("$", "").replace(",", ".")
    val valor = limpio.toDoubleOrNull() ?: return null
    if (valor <= 0.0 || valor.isNaN() || valor.isInfinite()) return null
    return valor
}

private fun mensajeMenu(error: Throwable?): String {
    val texto = error?.message.orEmpty()
    return if (texto.contains("PERMISSION_DENIED", ignoreCase = true)) {
        "Firebase no dejó guardar el platillo. Esta cuenta sí puede marcarlo agotado, pero las reglas de productos no permiten cambiar nombre, descripción, precio ni categoría."
    } else {
        texto.ifBlank { "No se pudo guardar el platillo" }
    }
}
