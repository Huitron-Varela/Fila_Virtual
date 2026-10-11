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
 * EMPLEADO: Personal operativo de un establecimiento (cocina, entrega o cajero).
 * ADMIN: Administrador general con acceso a todo el sistema.
 */
object Roles {
    const val CLIENTE = "cliente"
    const val EMPLEADO = "empleado"
    const val ADMIN = "admin"

    fun normalizar(valor: String?): String {
        return when (valor?.lowercase()?.trim()) {
            CLIENTE, EMPLEADO, ADMIN -> valor.lowercase().trim()
            else -> CLIENTE
        }
    }
}

@Serializable
data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val fotoUrl: String = "",
    val rolGlobal: String = Roles.CLIENTE,
    val establecimientoId: String = "",
    val establecimientoNombre: String = "",
    val rolesEmpleado: List<String> = emptyList(),
    val sesionId: String = "",
    val sesionEstacion: String = "",
    val sesionInicioAt: Long = 0L,
    val sesionActiva: Boolean = false,
    val metricasFecha: String = "",
    val metricasSesiones: List<SesionMetrica> = emptyList(),
    val metodosPago: List<TarjetaGuardada> = emptyList(),
    val verificado: Boolean = false,
    val activo: Boolean = true,

    // 🔥 CAMPO NECESARIO PARA LA BILLETERA
    val mercadoPagoVinculado: Boolean = false,

    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)