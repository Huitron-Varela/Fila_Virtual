package com.example.fila_virtual.data

import kotlinx.serialization.Serializable

@Serializable
data class Producto(
    val id: String = "",
    val establecimientoId: String = "",
    val ownerUid: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val imagenUrl: String = "",
    val disponible: Boolean = true,
    val enCatalogo: Boolean = true,
    val popularidad: Double = 0.0, // Puedes dejar esta por si la usan para métricas de clics después

    // 👇 NUEVOS CAMPOS PARA EL RANKING ORGÁNICO
    val ratingPromedio: Double = 0.0,
    val totalVotos: Int = 0,

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

object CategoriasProducto {
    const val COMIDA = "Comida"
    const val BEBIDAS = "Bebidas"
    const val SNACKS = "Snacks"
    const val POSTRES = "Postres"
}