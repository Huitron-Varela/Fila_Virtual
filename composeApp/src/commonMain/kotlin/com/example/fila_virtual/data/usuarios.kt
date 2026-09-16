package com.example.fila_virtual.data

import kotlinx.serialization.Serializable

/**
 * Estructura de datos para guardar una tarjeta vinculada del usuario.
 * Se almacena como objeto en la lista metodosPago de Firestore.
 */
@Serializable
data class TarjetaGuardada(
    val ultimos4: String = "",
    val marca: String = "",
    val nombreTitular: String = "",
    val expiracion: String = "",
    val tokenId: String = ""
)

/**
 * Roles globales de la plataforma.
 * CLIENTE: Usuario que realiza pedidos.
 * ADMIN: Administrador general con acceso a todo el sistema.
 */
object Roles {
    const val CLIENTE = "cliente"
    const val ADMIN = "admin"
}

@Serializable
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val fotoUrl: String = "",
    val rolGlobal: String = Roles.CLIENTE,
    val metodosPago: List<TarjetaGuardada> = emptyList(),
    val verificado: Boolean = false,
    val activo: Boolean = true,

    // 🔥 CAMPO NECESARIO PARA LA BILLETERA
    val mercadoPagoVinculado: Boolean = false,

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)