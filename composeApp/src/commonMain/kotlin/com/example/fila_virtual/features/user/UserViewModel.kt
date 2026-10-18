package com.example.fila_virtual.features.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.core.ErrorMessages
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.generarCodigoRecogida
import com.example.fila_virtual.data.ProductoCarrito
import com.example.fila_virtual.data.TarjetaGuardada
import com.example.fila_virtual.data.Usuario
import com.example.fila_virtual.repository.uploadImage
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
import kotlinx.coroutines.Job
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
        internal set // Cambiado a internal set para modificarlo desde ClienteMainScreen
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

    // 🔥 NUEVO ESTADO GLOBAL PARA CONTROLAR LA ESPERA DE MERCADO PAGO
    var isWaitingForPayment by mutableStateOf(false)

    private val _carrito = MutableStateFlow<List<ProductoCarrito>>(emptyList())
    val carrito: StateFlow<List<ProductoCarrito>> = _carrito
    var carritoEstablecimientoId by mutableStateOf("")
        private set
    var carritoEstablecimientoNombre by mutableStateOf("")
        private set
    private var cargaUsuario: Job? = null

    init {
        loadUserData()
    }

    fun updatePendingWalletAction(action: String) { pendingWalletAction = action }
    fun clearPendingWalletAction() { pendingWalletAction = null }

    fun clearWalletMessage() {
        walletMessage = null
        walletMessageIsError = false
        errorMessage = "" // Limpiamos errores de MP también
    }

    fun resetWalletCardForm() {
        numeroTarjeta = ""
        nombreTitular = ""
        fechaExpiracion = ""
        cvv = ""
        clearWalletMessage()
    }

    fun toggleMercadoPagoVinculado(vincular: Boolean) {
        val uid = usuario?.uid ?: return
        viewModelScope.launch {
            try {
                Firebase.firestore.collection("usuarios").document(uid).update("mercadoPagoVinculado" to vincular)
                usuario = usuario?.copy(mercadoPagoVinculado = vincular)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun loadUserData() {
        val uid = repository.getCurrentUserUid()
        cargaUsuario?.cancel()
        if (uid == null) {
            usuario = null
            isLoading = false
            return
        }
        if (usuario?.uid != uid) {
            usuario = null
        }

        cargaUsuario = viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val data = repository.getUserData(uid)
                if (repository.getCurrentUserUid() != uid) return@launch
                if (data != null) {
                    usuario = data
                } else {
                    errorMessage = ErrorMessages.USER_NOT_FOUND
                }
            } catch (e: Exception) {
                errorMessage = ErrorMessages.DATABASE_ERROR
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfile(
        nombre: String,
        telefono: String,
        fotoUrl: String?,
        fotoBytes: ByteArray? = null,
        fotoMimeType: String = "image/jpeg",
        onResult: (Boolean) -> Unit
    ) {
        val uid = repository.getCurrentUserUid()
        if (uid == null) {
            onResult(false)
            return
        }

        viewModelScope.launch {
            try {
                val fotoActualizada = if (fotoBytes != null) {
                    val uploadResult = uploadImage(
                        path = "usuarios/$uid/perfil",
                        bytes = fotoBytes,
                        mimeType = fotoMimeType
                    )
                    uploadResult.getOrNull() ?: run {
                        onResult(false)
                        return@launch
                    }
                } else {
                    fotoUrl
                }

                val now = Timestamp.now().seconds * 1000
                val updates = mutableMapOf<String, Any?>("nombre" to nombre, "telefono" to telefono, "updatedAt" to now)
                if (fotoActualizada != null) updates["fotoUrl"] = fotoActualizada

                val success = repository.updateUserData(uid, updates)
                if (success) loadUserData()
                onResult(success)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun signOut(onSuccess: () -> Unit) {
        cargaUsuario?.cancel()
        usuario = null
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
        return null
    }

    private fun validarNumeroTarjeta(numero: String): String? {
        if (numero.isBlank()) return "Ingresa el número de la tarjeta."
        if (numero.length !in 13..19) return "El número está incompleto."
        return null
    }

    private fun validarFechaExpiracion(fecha: String): String? {
        if (fecha.length != 4) return "Ingresa formato MM/AA."
        return null
    }

    private fun validarCvv(codigo: String): String? {
        if (codigo.length !in 3..4) return "Debe tener 3 o 4 dígitos."
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
                errorMessage = "No se pudo eliminar la tarjeta."
            } finally {
                isLoading = false
            }
        }
    }

    fun actualizarDatosTarjeta(
        tarjetaOriginal: TarjetaGuardada,
        nombreTitular: String,
        expiracion: String,
        onComplete: (Boolean) -> Unit
    ) {
        val userId = Firebase.auth.currentUser?.uid
        val metodosActuales = usuario?.metodosPago
        if (userId == null || metodosActuales == null) {
            onComplete(false)
            return
        }

        val index = metodosActuales.indexOfFirst { tarjeta ->
            if (tarjetaOriginal.tokenId.isNotBlank()) {
                tarjeta.tokenId == tarjetaOriginal.tokenId
            } else {
                tarjeta == tarjetaOriginal
            }
        }
        if (index < 0) {
            onComplete(false)
            return
        }

        val nuevosMetodos = metodosActuales.toMutableList().also { metodos ->
            metodos[index] = metodos[index].copy(
                nombreTitular = nombreTitular.trim(),
                expiracion = expiracion
            )
        }

        viewModelScope.launch {
            isLoading = true
            var success = false
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
                success = true
            } catch (e: Exception) {
                errorMessage = "No se pudieron actualizar los datos de la tarjeta."
            } finally {
                isLoading = false
                onComplete(success)
            }
        }
    }

    fun procesarPagoSeguro(
        tarjetaAReemplazar: TarjetaGuardada? = null,
        onSuccess: (TarjetaGuardada) -> Unit = {}
    ) {
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

                if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                    val jsonResponse = Json.parseToJsonElement(responseText).jsonObject
                    val tokenId = jsonResponse["id"]?.jsonPrimitive?.content ?: ""

                    if (tokenId.isBlank()) {
                        mostrarErrorWallet("No fue posible validar la tarjeta.")
                        return@launch
                    }

                    val ultimos4 = numeroTarjeta.takeLast(4)
                    val expiracionFormateada = "${fechaExpiracion.substring(0, 2)}/${fechaExpiracion.substring(2, 4)}"
                    val marcaReal = detectarMarcaTarjeta(numeroTarjeta)
                    val currentMethods = usuario?.metodosPago?.toMutableList() ?: mutableListOf()
                    val nuevaTarjeta = TarjetaGuardada(ultimos4, marcaReal, nombreTitular.trim(), expiracionFormateada, tokenId)
                    if (tarjetaAReemplazar == null) {
                        currentMethods.add(nuevaTarjeta)
                    } else {
                        val indiceReemplazo = currentMethods.indexOfFirst { tarjeta ->
                            if (tarjetaAReemplazar.tokenId.isNotBlank()) {
                                tarjeta.tokenId == tarjetaAReemplazar.tokenId
                            } else {
                                tarjeta == tarjetaAReemplazar
                            }
                        }
                        if (indiceReemplazo < 0) {
                            mostrarErrorWallet("La tarjeta que intentas actualizar ya no está guardada.")
                            return@launch
                        }
                        currentMethods[indiceReemplazo] = nuevaTarjeta
                    }

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
                    walletMessage = if (tarjetaAReemplazar == null) {
                        "¡Tarjeta validada y vinculada correctamente!"
                    } else {
                        "¡Tarjeta actualizada correctamente!"
                    }
                    walletMessageIsError = false
                    errorMessage = ""
                    onSuccess(nuevaTarjeta)
                } else {
                    mostrarErrorWallet("Mercado Pago rechazó los datos.")
                }
            } catch (e: Exception) {
                mostrarErrorWallet("No pudimos conectarnos. Revisa tu conexión.")
            } finally {
                client.close()
                isLoading = false
            }
        }
    }

    private fun detectarMarcaTarjeta(numero: String): String {
        return when {
            numero.startsWith("4") -> "VISA"
            numero.startsWith("34") -> "AMEX"
            numero.startsWith("37") -> "AMEX"
            numero.length >= 2 && numero.take(2).toIntOrNull() in 51..55 -> "MASTERCARD"
            else -> "TARJETA"
        }
    }

    fun agregarAlCarrito(
        idProducto: String,
        nombre: String,
        precio: Double,
        establecimientoId: String = "",
        establecimientoNombre: String = "",
        imagenUrl: String = "",
        descripcion: String = ""
    ) {
        if (
            establecimientoId.isNotBlank() &&
            carritoEstablecimientoId.isNotBlank() &&
            carritoEstablecimientoId != establecimientoId &&
            _carrito.value.isNotEmpty()
        ) {
            _carrito.value = emptyList()
        }
        if (establecimientoId.isNotBlank()) {
            carritoEstablecimientoId = establecimientoId
            if (establecimientoNombre.isNotBlank()) {
                carritoEstablecimientoNombre = establecimientoNombre
            }
        }
        val listaActual = _carrito.value.toMutableList()
        val itemExistente = listaActual.find { it.idProducto == idProducto }

        if (itemExistente != null) {
            val index = listaActual.indexOf(itemExistente)
            listaActual[index] = itemExistente.copy(
                cantidad = itemExistente.cantidad + 1,
                imagenUrl = itemExistente.imagenUrl.ifBlank { imagenUrl },
                descripcion = itemExistente.descripcion.ifBlank { descripcion }
            )
        } else {
            listaActual.add(
                ProductoCarrito(
                    idProducto = idProducto,
                    nombre = nombre,
                    precio = precio,
                    cantidad = 1,
                    imagenUrl = imagenUrl,
                    descripcion = descripcion
                )
            )
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
        if (_carrito.value.isEmpty()) {
            carritoEstablecimientoId = ""
            carritoEstablecimientoNombre = ""
        }
    }

    fun vaciarCarrito() {
        _carrito.value = emptyList()
        carritoEstablecimientoId = ""
        carritoEstablecimientoNombre = ""
    }
    fun calcularTotalCarrito(): Double { return _carrito.value.sumOf { it.precio * it.cantidad } }
    fun calcularCantidadTotalItems(): Int { return _carrito.value.sumOf { it.cantidad } }

    // 🔥 GENERACIÓN DE LINK CON INYECCIÓN DE ERROR A LA UI 🔥
    suspend fun generarLinkMercadoPago(): String? {
        val tokenTest = "APP_USR-260696831028649-091421-ad1ec1fe9f07ba759892dad5a2adf5d2-3690860310"

        return try {
            val carritoList = _carrito.value
            if (carritoList.isEmpty()) {
                errorMessage = "El carrito está vacío"
                return null
            }

            val client = HttpClient { expectSuccess = false }

            val itemsJson = carritoList.joinToString(",") { producto ->
                """
                {
                    "title": "${producto.nombre}",
                    "quantity": ${producto.cantidad},
                    "unit_price": ${producto.precio},
                    "currency_id": "MXN"
                }
                """.trimIndent()
            }

            val jsonBody = """
            {
                "items": [$itemsJson],
                "back_urls": {
                    "success": "altoquefood://success",
                    "failure": "altoquefood://failure",
                    "pending": "altoquefood://pending"
                },
                "auto_return": "approved"
            }
            """.trimIndent()

            val response: HttpResponse = client.post("https://api.mercadopago.com/checkout/preferences") {
                header("Authorization", "Bearer $tokenTest")
                header("Content-Type", "application/json")
                setBody(jsonBody)
            }

            val responseText = response.bodyAsText()
            client.close()

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                val jsonResponse = Json.parseToJsonElement(responseText).jsonObject
                val payUrl = jsonResponse["sandbox_init_point"]?.jsonPrimitive?.content
                    ?: jsonResponse["init_point"]?.jsonPrimitive?.content

                errorMessage = "" // Limpiamos cualquier error previo
                return payUrl
            } else {
                // Si MP lo rechaza, mandamos el mensaje exacto a la pantalla
                errorMessage = "Error MP (${response.status.value}): $responseText"
                return null
            }
        } catch (e: Exception) {
            // Si Ktor falla (sin internet, etc), lo mandamos a la pantalla
            errorMessage = "Excepción Ktor: ${e.message}"
            return null
        }
    }

    fun confirmarPedidoDelCarrito(onSuccess: () -> Unit) {
        if (carritoEstablecimientoId.isBlank()) {
            errorMessage = "Agrega los productos desde el menú de La gusgreria."
            return
        }
        procesarCompraDelCarrito(
            establecimientoId = carritoEstablecimientoId,
            establecimientoNombre = carritoEstablecimientoNombre.ifBlank { "Establecimiento" },
            onSuccess = onSuccess
        )
    }

    fun procesarCompraDelCarrito(
        establecimientoId: String,
        establecimientoNombre: String,
        onSuccess: () -> Unit
    ) {
        if (_carrito.value.isEmpty()) return

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
                val total = round((subtotal + 5.0) * 100) / 100.0
                val descripcion = _carrito.value.joinToString(", ") { "${it.cantidad}x ${it.nombre}" }

                val turno = (1..99).random()
                val now = Timestamp.now().seconds * 1000
                val pedidosRef = Firebase.firestore.collection("pedidos")
                val nuevoPedidoRef = pedidosRef.document

                val nuevoPedido = Pedido(
                    id = nuevoPedidoRef.id, userId = userId, establecimientoId = establecimientoId,
                    establecimientoNombre = establecimientoNombre, descripcion = descripcion, total = total,
                    estado = EstadoPedido.RECIBIDO, turno = turno, createdAt = now,
                    codigoRecogida = generarCodigoRecogida(), productos = _carrito.value
                )

                nuevoPedidoRef.set(nuevoPedido)
                vaciarCarrito()
                isLoading = false
                onSuccess()
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "No se pudo generar el pedido."
            }
        }
    }
}