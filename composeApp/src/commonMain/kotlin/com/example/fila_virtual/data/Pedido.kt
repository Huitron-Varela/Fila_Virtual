package com.example.fila_virtual.data

import kotlinx.serialization.Serializable

@Serializable
enum class EstadoPedido {
    PENDIENTE,       // Creado pero no visto por el restaurante
    RECIBIDO,        // El restaurante lo aceptó
    EN_PREPARACION,  // El restaurante está cocinando
    LISTO,           // ¡La comida está lista! (Aquí se desbloquea el QR)
    ENTREGADO,       // El cliente lo recogió -> Pasa al Historial
    NO_RECLAMADO,    // Listo y no recogido. No cuenta como entrega ni reembolso
    CANCELADO        // Se canceló -> Pasa al Historial
}

private const val ALFABETO_CODIGO = "0123456789"

fun generarCodigoRecogida(): String =
    buildString(4) { repeat(4) { append(ALFABETO_CODIGO.random()) } }

fun codigoVisible(pedido: Pedido): String {
    val guardado = pedido.codigoRecogida.trim()
    if (guardado.length >= 4) return guardado.uppercase()
    return codigoDesdeId(pedido.id)
}

fun codigoDesdeId(id: String): String {
    if (id.isBlank()) return ""
    var hash = 2166136261u
    id.uppercase().forEach { caracter ->
        hash = hash xor caracter.code.toUInt()
        hash *= 16777619u
    }
    val base = ALFABETO_CODIGO.length.toUInt()
    return buildString(4) {
        var valor = hash
        repeat(4) {
            append(ALFABETO_CODIGO[(valor % base).toInt()])
            valor /= base
        }
    }
}

fun coincideCodigoRecogida(pedido: Pedido, codigo: String): Boolean {
    val limpio = codigo.trim()
    if (limpio.isEmpty()) return false
    val visible = codigoVisible(pedido)
    return limpio.equals(visible, ignoreCase = true) ||
        limpio.equals(pedido.codigoRecogida.trim(), ignoreCase = true) ||
        limpio.equals(pedido.id, ignoreCase = true) ||
        limpio.equals("ALTOQUE-${pedido.id}", ignoreCase = true)
}

@Serializable
data class Pedido(
    val id: String = "",
    val userId: String = "",
    val establecimientoId: String = "",
    val establecimientoNombre: String = "",
    val descripcion: String = "",
    val total: Double = 0.0,
    val estado: EstadoPedido = EstadoPedido.RECIBIDO,
    val turno: Int = 0,
    val createdAt: Long = 0L,
    val actualizadoAt: Long = 0L,
    val empleadoUid: String = "",
    val sesionId: String = "",
    val codigoRecogida: String = "",
    val productos: List<ProductoCarrito> = emptyList()
)