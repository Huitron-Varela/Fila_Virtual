package com.example.fila_virtual.data

import kotlinx.serialization.Serializable

@Serializable
data class ProductoCarrito(
    val idProducto: String,
    val nombre: String,
    val precio: Double,
    val cantidad: Int,
    val imagenUrl: String = "",
    val descripcion: String = ""
)