package com.example.fila_virtual.features.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.core.ErrorMessages
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.ProductoCarrito
import com.example.fila_virtual.data.TarjetaGuardada
import com.example.fila_virtual.data.Usuario
import com.example.fila_virtual.repository.UserRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.math.round

class UserViewModel(
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val MERCADO_PAGO_PUBLIC_KEY = "TEST-f1ae3349-69ba-4fed-b8b4-72166ffb423d"

    var usuario by mutableStateOf<Usuario?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf("")
        private set
    var numeroTarjeta by mutableStateOf("")
        private set
    var nombreTitular by mutableStateOf("")
        private set
    var fechaExpiracion by mutableStateOf("")
        private set
    var cvv by mutableStateOf("")
        private set
    var walletMessage by mutableStateOf<String?>(null)
        private set
    var walletMessageIsError by mutableStateOf(false)
        private set
    var pendingWalletAction by mutableStateOf<String?>(null)
        private set

    private val _carrito = MutableStateFlow<List<ProductoCarrito>>(emptyList())
    val carrito: StateFlow<List<ProductoCarrito>> = _carrito

    init {
        loadUserData()
    }

    fun updatePendingWalletAction(action: String) { pendingWalletAction = action }
    fun clearPendingWalletAction() { pendingWalletAction = null }

    fun clearWalletMessage() {
        walletMessage = null
        walletMessageIsError = false
    }

    fun loadUserData() {
        val uid = repository.getCurrentUserUid()
        if (uid == null) return

        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val data = repository.getUserData(uid)
                if (data != null) {
                    usuario = data
                } else {
                    errorMessage = ErrorMessages.USER_NOT_FOUND
                }
            } catch (e: Exception) {
                println("USER_VM: Error cargando usuario: ${e.message}")
                errorMessage = ErrorMessages.DATABASE_ERROR
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfile(nombre: String, telefono: String, fotoUrl: String?, onResult: (Boolean) -> Unit) {
        val uid = repository.getCurrentUserUid()
        if (uid == null) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val now = Timestamp.now().seconds * 1000
                val updates = mutableMapOf<String, Any?>("nombre" to nombre, "telefono" to telefono, "updatedAt" to now)
                if (fotoUrl != null) updates["fotoUrl"] = fotoUrl

                val success = repository.updateUserData(uid, updates)
                if (success) loadUserData()
                onResult(success)
            } catch (e: Exception) {
                println("USER_VM: Error actualizando perfil: ${e.message}")
                onResult(false)
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.signOut()
            onSuccess()
        }
    }

    fun onNumeroTarjetaChange(nuevoNumero: String) {
        numeroTarjeta = nuevoNumero.filter { it.isDigit() }.take(19)
        limpiarErrorWalletAlEditar()
    }

    fun onNombreTitularChange(nuevoNombre: String) {
        if (nuevoNombre.any { it.isDigit() }) {
            walletMessage = "El nombre del titular no puede contener números."
            walletMessageIsError = true
        } else {
            limpiarErrorWalletAlEditar()
        }
        nombreTitular = nuevoNombre.filter { it.isLetter() || it.isWhitespace() || it == '\'' || it == '-' }.take(50)
    }

    fun onFechaExpiracionChange(nuevaFecha: String) {
        fechaExpiracion = nuevaFecha.filter { it.isDigit() }.take(4)
        limpiarErrorWalletAlEditar()
    }

    fun onCvvChange(nuevoCvv: String) {
        cvv = nuevoCvv.filter { it.isDigit() }.take(4)
        limpiarErrorWalletAlEditar()
    }

    private fun limpiarErrorWalletAlEditar() {
        if (walletMessageIsError) {
            walletMessage = null
            walletMessageIsError = false
        }
    }

    private fun mostrarErrorWallet(mensaje: String) {
        walletMessage = mensaje
        walletMessageIsError = true
        isLoading = false
    }

    private fun validarNombreTitular(nombre: String): String? {
        val limpio = nombre.trim()
        if (limpio.isBlank()) return "Escribe el nombre del titular de la tarjeta."
        if (limpio.count { it.isLetter() } < 2) return "El nombre del titular debe contener al menos dos letras."
        if (limpio.any { it.isDigit() }) return "El nombre del titular no puede contener números."
        val caracteresValidos = limpio.all { it.isLetter() || it.isWhitespace() || it == '\'' || it == '-' }
        if (!caracteresValidos) return "El nombre solo puede contener letras, espacios, apóstrofes y guiones."
        return null
    }

    private fun validarNumeroTarjeta(numero: String): String? {
        if (numero.isBlank()) return "Ingresa el número de la tarjeta."
        if (!numero.all { it.isDigit() }) return "El número de tarjeta solo puede contener números."
        if (numero.length !in 13..19) return "El número está incompleto. Debe contener entre 13 y 19 dígitos."
        if (numero.all { it == numero.first() }) return "Número rechazado: todos los dígitos son iguales. Ese patrón no corresponde a una tarjeta válida."
        if (!cumpleAlgoritmoLuhn(numero)) return "Número rechazado: no supera la validación Luhn. Revisa que hayas escrito correctamente todos los dígitos."
        return null
    }

    private fun cumpleAlgoritmoLuhn(numero: String): Boolean {
        var suma = 0
        var duplicar = false
        for (i in numero.length - 1 downTo 0) {
            var digito = numero[i].digitToInt()
            if (duplicar) {
                digito *= 2
                if (digito > 9) digito -= 9
            }
            suma += digito
            duplicar = !duplicar
        }
        return suma % 10 == 0
    }

    private fun validarFechaExpiracion(fecha: String): String? {
        if (fecha.length != 4) return "Ingresa la fecha de vencimiento completa en formato MM/AA."
        if (!fecha.all { it.isDigit() }) return "La fecha de vencimiento solo puede contener números."
        val mes = fecha.substring(0, 2).toIntOrNull() ?: return "El mes de vencimiento no es válido."
        if (mes !in 1..12) return "El mes de vencimiento debe estar entre 01 y 12."
        return null
    }

    private fun validarCvv(codigo: String): String? {
        if (codigo.length !in 3..4) return "El código de seguridad debe tener 3 o 4 dígitos."
        if (!codigo.all { it.isDigit() }) return "El código de seguridad solo puede contener números."
        return null
    }

    fun eliminarTarjeta(ultimos4: String) {
        val userId = Firebase.auth.currentUser?.uid ?: return
        val metodosActuales = usuario?.metodosPago ?: return
        val nuevosMetodos = metodosActuales.filter { it.ultimos4 != ultimos4 }

        viewModelScope.launch {
            isLoading = true
            try {
                val metodosComoMapa = nuevosMetodos.map { tarjeta ->
                    mapOf(
                        "ultimos4" to tarjeta.ultimos4,
                        "marca" to tarjeta.marca,
                        "nombreTitular" to tarjeta.nombreTitular,
                        "expiracion" to tarjeta.expiracion,
                        "tokenId" to tarjeta.tokenId
                    )
                }
                Firebase.firestore.collection("usuarios").document(userId)
                    .update("metodosPago" to metodosComoMapa, "updatedAt" to Timestamp.now().seconds * 1000)
                usuario = usuario?.copy(metodosPago = nuevosMetodos)
            } catch (e: Exception) {
                println("WALLET: Error eliminando tarjeta: ${e.message}")
                errorMessage = "No se pudo eliminar la tarjeta."
            } finally {
                isLoading = false
            }
        }
    }

    fun procesarPagoSeguro(onSuccess: () -> Unit = {}) {
        clearWalletMessage()

        validarNumeroTarjeta(numeroTarjeta)?.let { mostrarErrorWallet(it); return }
        validarNombreTitular(nombreTitular)?.let { mostrarErrorWallet(it); return }
        validarFechaExpiracion(fechaExpiracion)?.let { mostrarErrorWallet(it); return }
        validarCvv(cvv)?.let { mostrarErrorWallet(it); return }

        val userId = Firebase.auth.currentUser?.uid
        if (userId == null) {
            mostrarErrorWallet("Tu sesión expiró. Inicia sesión nuevamente.")
            return
        }

        isLoading = true

        viewModelScope.launch {
            val client = HttpClient { expectSuccess = false }
            try {
                val mes = fechaExpiracion.substring(0, 2).toInt()
                val anio = ("20" + fechaExpiracion.substring(2, 4)).toInt()
                val body = buildJsonObject {
                    put("card_number", numeroTarjeta)
                    put("expiration_month", mes)
                    put("expiration_year", anio)
                    put("security_code", cvv)
                    put("cardholder", buildJsonObject { put("name", nombreTitular.trim()) })
                }.toString()

                val response: HttpResponse = client.post("https://api.mercadopago.com/v1/card_tokens?public_key=$MERCADO_PAGO_PUBLIC_KEY") {
                    header("Content-Type", "application/json")
                    setBody(body)
                }

                val responseText = response.bodyAsText()
                println("MERCADO_PAGO_TOKEN: HTTP ${response.status.value}")

                if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                    val jsonResponse = Json.parseToJsonElement(responseText).jsonObject
                    val tokenId = jsonResponse["id"]?.jsonPrimitive?.content ?: ""

                    if (tokenId.isBlank()) {
                        mostrarErrorWallet("Mercado Pago respondió, pero no fue posible validar la tarjeta.")
                        return@launch
                    }

                    val ultimos4 = numeroTarjeta.takeLast(4)
                    val expiracionFormateada = "${fechaExpiracion.substring(0, 2)}/${fechaExpiracion.substring(2, 4)}"
                    val marcaReal = detectarMarcaTarjeta(numeroTarjeta)
                    val currentMethods = usuario?.metodosPago?.toMutableList() ?: mutableListOf()

                    val yaExiste = currentMethods.any { tarjeta ->
                        tarjeta.ultimos4 == ultimos4 && tarjeta.marca == marcaReal && tarjeta.expiracion == expiracionFormateada
                    }

                    if (yaExiste) {
                        mostrarErrorWallet("Esta tarjeta ya está vinculada a tu Wallet.")
                        return@launch
                    }

                    val nuevaTarjeta = TarjetaGuardada(ultimos4, marcaReal, nombreTitular.trim(), expiracionFormateada, tokenId)
                    currentMethods.add(nuevaTarjeta)

                    val metodosComoMapa = currentMethods.map { tarjeta ->
                        mapOf(
                            "ultimos4" to tarjeta.ultimos4,
                            "marca" to tarjeta.marca,
                            "nombreTitular" to tarjeta.nombreTitular,
                            "expiracion" to tarjeta.expiracion,
                            "tokenId" to tarjeta.tokenId
                        )
                    }

                    Firebase.firestore.collection("usuarios").document(userId)
                        .update("metodosPago" to metodosComoMapa, "card_token" to tokenId, "updatedAt" to Timestamp.now().seconds * 1000)

                    usuario = usuario?.copy(metodosPago = currentMethods)
                    numeroTarjeta = ""; nombreTitular = ""; fechaExpiracion = ""; cvv = ""
                    walletMessage = "¡Tarjeta validada y vinculada correctamente!"
                    walletMessageIsError = false
                    errorMessage = ""
                    onSuccess()
                } else {
                    println("MERCADO_PAGO_TOKEN_ERROR: $responseText")
                    if (response.status.value == 400) {
                        mostrarErrorWallet("Mercado Pago rechazó los datos. Revisa el número, la fecha de vencimiento y el CVV.")
                    } else {
                        mostrarErrorWallet("No fue posible validar la tarjeta con Mercado Pago. Inténtalo nuevamente.")
                    }
                }
            } catch (e: Exception) {
                println("MERCADO_PAGO_TOKEN_EXCEPTION: ${e.message}")
                mostrarErrorWallet("No pudimos conectarnos con Mercado Pago. Revisa tu conexión e inténtalo nuevamente.")
            } finally {
                client.close()
                isLoading = false
            }
        }
    }

    private fun detectarMarcaTarjeta(numero: String): String {
        return when {
            numero.startsWith("4") -> "VISA"
            numero.startsWith("34") || numero.startsWith("37") -> "AMEX"
            numero.length >= 2 && numero.take(2).toIntOrNull() in 51..55 -> "MASTERCARD"
            else -> "TARJETA"
        }
    }

    fun agregarAlCarrito(idProducto: String, nombre: String, precio: Double) {
        val listaActual = _carrito.value.toMutableList()
        val itemExistente = listaActual.find { it.idProducto == idProducto }

        if (itemExistente != null) {
            val index = listaActual.indexOf(itemExistente)
            listaActual[index] = itemExistente.copy(cantidad = itemExistente.cantidad + 1)
        } else {
            listaActual.add(ProductoCarrito(idProducto, nombre, precio, 1))
        }
        _carrito.value = listaActual
    }

    fun incrementarCantidad(idProducto: String) {
        val listaActual = _carrito.value.toMutableList()
        val index = listaActual.indexOfFirst { it.idProducto == idProducto }

        if (index != -1) {
            val item = listaActual[index]
            listaActual[index] = item.copy(cantidad = item.cantidad + 1)
            _carrito.value = listaActual
        }
    }

    fun decrementarCantidad(idProducto: String) {
        val listaActual = _carrito.value.toMutableList()
        val index = listaActual.indexOfFirst { it.idProducto == idProducto }

        if (index != -1) {
            val item = listaActual[index]
            if (item.cantidad > 1) {
                listaActual[index] = item.copy(cantidad = item.cantidad - 1)
            } else {
                listaActual.removeAt(index)
            }
            _carrito.value = listaActual
        }
    }

    fun vaciarCarrito() { _carrito.value = emptyList() }
    fun calcularTotalCarrito(): Double { return _carrito.value.sumOf { it.precio * it.cantidad } }
    fun calcularCantidadTotalItems(): Int { return _carrito.value.sumOf { it.cantidad } }

    fun procesarCompraDelCarrito(
        establecimientoId: String,
        establecimientoNombre: String,
        tarjetaSeleccionada: TarjetaGuardada?,
        onSuccess: () -> Unit
    ) {
        if (_carrito.value.isEmpty()) {
            errorMessage = ErrorMessages.CART_EMPTY
            return
        }

        isLoading = true
        errorMessage = ""

        viewModelScope.launch {
            try {
                val userId = Firebase.auth.currentUser?.uid
                if (userId == null) {
                    errorMessage = ErrorMessages.SESSION_EXPIRED
                    isLoading = false
                    return@launch
                }

                val subtotal = calcularTotalCarrito()
                val tarifaServicio = 5.0
                val total = round((subtotal + tarifaServicio) * 100) / 100.0

                val descripcion = _carrito.value.joinToString(", ") { "${it.cantidad}x ${it.nombre}" }
                println("DEMO_PAGO: Procesando pago...")
                delay(800)
                println("DEMO_PAGO: Pago aprobado (simulación).")

                val turno = (1..99).random()
                val now = Timestamp.now().seconds * 1000
                val pedidosRef = Firebase.firestore.collection("pedidos")
                val nuevoPedidoRef = pedidosRef.document

                val nuevoPedido = Pedido(
                    id = nuevoPedidoRef.id,
                    userId = userId,
                    establecimientoId = establecimientoId,
                    establecimientoNombre = establecimientoNombre,
                    descripcion = descripcion,
                    total = total,
                    estado = EstadoPedido.RECIBIDO,
                    turno = turno,
                    createdAt = now,
                    productos = _carrito.value // 🔥 MAGIA: GUARDAMOS LOS PRODUCTOS REALES CON SU ID
                )

                nuevoPedidoRef.set(nuevoPedido)
                vaciarCarrito()

                isLoading = false
                errorMessage = ""
                onSuccess()

                delay(4000)
                nuevoPedidoRef.update("estado" to EstadoPedido.EN_PREPARACION.name)

                delay(6000)
                nuevoPedidoRef.update("estado" to EstadoPedido.LISTO.name)

                delay(8000)
                nuevoPedidoRef.update("estado" to EstadoPedido.ENTREGADO.name)
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "No se pudo generar el pedido."
                println("DEMO_PEDIDO_ERROR: ${e.message}")
            }
        }
    }
}