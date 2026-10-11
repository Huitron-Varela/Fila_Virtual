package com.example.fila_virtual.data

import kotlinx.serialization.Serializable

/**
 * 🧑‍🍳 SUBCOLECCIÓN: Empleados
 * Ruta en Firestore: establecimientos/{establecimientoId}/empleados/{uid}
 * * Nota: Para mostrar los datos del empleado en la UI (nombre, foto),
 * debes cruzar este 'uid' con la colección global 'users'.
 */
@Serializable
data class Empleado(
    val uid: String = "",
    val rol: String = "empleado",
    val roles: List<String> = emptyList(),
    val activo: Boolean = true,
    val invitacionToken: String = "",
    val joinedAt: Long = 0L,
    val updatedAt: Long = 0L
)

data class EmpleadoDetalle(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val fotoUrl: String = "",
    val rol: String = "",
    val roles: List<String> = emptyList(),
    val activo: Boolean = true,
    val joinedAt: Long = 0L
)

@Serializable
data class InvitacionEmpleado(
    val token: String = "",
    val correo: String = "",
    val establecimientoId: String = "",
    val rol: String = "",
    val roles: List<String> = emptyList(),
    val ownerUid: String = "",
    val ownerNombre: String = "",
    val establecimientoNombre: String = "",
    val status: String = "pending",
    val acceptedBy: String = "",
    val acceptedAt: Long = 0L,
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L
)

object RolesEmpleado {
    val disponibles = listOf("cajero", "cocina", "entrega", "supervisor", "admin")
}

enum class EstacionOperativa {
    COCINA,
    ENTREGA,
    CAJERO;

    fun clave(): String = name.lowercase()

    companion object {
        fun desdeRoles(roles: List<String>): List<EstacionOperativa> {
            val autorizadas = roles.mapNotNull { rol ->
                when (rol.lowercase().trim()) {
                    "cocina" -> COCINA
                    "entrega" -> ENTREGA
                    "cajero" -> CAJERO
                    else -> null
                }
            }.distinct()
            return autorizadas.ifEmpty { entries }
        }
    }
}

data class AsignacionEmpleado(
    val establecimientoId: String = "",
    val establecimientoNombre: String = "",
    val roles: List<String> = emptyList()
)

@Serializable
data class SesionTurno(
    val id: String = "",
    val empleadoUid: String = "",
    val establecimientoId: String = "",
    val estacion: String = "",
    val inicioAt: Long = 0L,
    val finAt: Long = 0L,
    val activa: Boolean = true
)