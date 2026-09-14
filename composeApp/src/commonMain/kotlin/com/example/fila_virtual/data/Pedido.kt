package com.example.fila_virtual.data

import kotlinx.serialization.Serializable

@Serializable
enum class EstadoPedido {
    PENDIENTE,       // Creado pero no visto por el restaurante
    RECIBIDO,        // El restaurante lo aceptó
    EN_PREPARACION,  // El restaurante está cocinando
    LISTO,           // ¡La comida está lista! (Aquí se desbloquea el QR)
    ENTREGADO,       // El cliente lo recogió -> Pasa al Historial
    CANCELADO        // Se canceló -> Pasa al Historial
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
    // 🔥 Lista real de productos para calificar
    val productos: List<ProductoCarrito> = emptyList()
)