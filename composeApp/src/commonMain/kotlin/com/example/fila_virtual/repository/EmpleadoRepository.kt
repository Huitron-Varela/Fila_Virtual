package com.example.fila_virtual.repository

import com.example.fila_virtual.data.AsignacionEmpleado
import com.example.fila_virtual.data.Empleado
import com.example.fila_virtual.data.EstacionOperativa
import com.example.fila_virtual.data.InvitacionEmpleado
import com.example.fila_virtual.data.Roles
import com.example.fila_virtual.data.SesionTurno
import com.example.fila_virtual.data.Usuario
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class EmpleadoRepository {
    private val db = Firebase.firestore

    suspend fun registrarEmpleado(establecimientoId: String, empleado: Empleado): Result<Unit> {
        if (establecimientoId.isBlank()) return Result.failure(Exception("ID de establecimiento no válido"))
        
        return try {
            val docRef = db.collection("establecimientos")
                .document(establecimientoId)
                .collection("empleados")
                .document(empleado.uid)

            val timestamp = Clock.System.now().toEpochMilliseconds()
            val empleadoFinal = empleado.copy(
                joinedAt = if (empleado.joinedAt == 0L) timestamp else empleado.joinedAt,
                updatedAt = timestamp
            )

            docRef.set(empleadoFinal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarEmpleado(establecimientoId: String, uid: String): Result<Unit> {
        return try {
            db.collection("establecimientos")
                .document(establecimientoId)
                .collection("empleados")
                .document(uid)
                .delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun obtenerEmpleados(establecimientoId: String): Result<List<Empleado>> {
        if (establecimientoId.isBlank()) return Result.success(emptyList())
        
        return try {
            val snapshot = db.collection("establecimientos")
                .document(establecimientoId)
                .collection("empleados")
                .get()

            val empleados = snapshot.documents.mapNotNull { it.data<Empleado>() }
            Result.success(empleados)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getEmpleadosFlow(establecimientoId: String): Flow<List<Empleado>> {
        if (establecimientoId.isBlank()) return kotlinx.coroutines.flow.flowOf(emptyList())
        
        return db.collection("establecimientos")
            .document(establecimientoId)
            .collection("empleados")
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<Empleado>() }
            }
    }

    suspend fun buscarAsignacion(uid: String): Result<AsignacionEmpleado?> {
        if (uid.isBlank()) return Result.success(null)
        return try {
            val documento = db.collection("usuarios").document(uid).get()
            if (!documento.exists) return Result.success(null)
            val usuario = documento.data<Usuario>()
            if (usuario.establecimientoId.isBlank()) return Result.success(null)
            Result.success(
                AsignacionEmpleado(
                    establecimientoId = usuario.establecimientoId,
                    establecimientoNombre = usuario.establecimientoNombre,
                    roles = usuario.rolesEmpleado
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun iniciarSesion(
        uid: String,
        asignacion: AsignacionEmpleado,
        estacion: EstacionOperativa
    ): Result<SesionTurno> {
        return try {
            val ahora = Clock.System.now().toEpochMilliseconds()
            val sesion = SesionTurno(
                id = "$uid-$ahora",
                empleadoUid = uid,
                establecimientoId = asignacion.establecimientoId,
                estacion = estacion.clave(),
                inicioAt = ahora,
                activa = true
            )
            db.collection("usuarios").document(uid).update(
                "sesionId" to sesion.id,
                "sesionEstacion" to sesion.estacion,
                "sesionInicioAt" to ahora,
                "sesionActiva" to true,
                "updatedAt" to ahora
            )
            Result.success(sesion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun aceptarInvitacion(token: String): Result<String> {
        val user = Firebase.auth.currentUser
            ?: return Result.failure(Exception("Debes iniciar sesión para aceptar la invitación."))
        val codigo = token.trim()
        if (codigo.isBlank()) return Result.failure(Exception("Escribe el código de invitación."))

        return try {
            val document = db.collection("invitaciones_empleado").document(codigo).get()
            if (!document.exists) {
                return Result.failure(Exception("La invitación no existe o ya no está disponible."))
            }
            val invitation = document.data<InvitacionEmpleado>()
            val now = Clock.System.now().toEpochMilliseconds()
            val correoSesion = user.email.orEmpty()
            val yaAceptadaPorMi = invitation.status == "accepted" && invitation.acceptedBy == user.uid
            when {
                invitation.status != "pending" && !yaAceptadaPorMi ->
                    return Result.failure(Exception("Esta invitación ya fue utilizada."))
                invitation.status == "pending" && invitation.expiresAt < now ->
                    return Result.failure(Exception("Esta invitación ha expirado."))
                !invitation.correo.equals(correoSesion, ignoreCase = true) ->
                    return Result.failure(Exception("Inicia sesión con el correo que recibió la invitación."))
                invitation.ownerUid == user.uid ->
                    return Result.failure(Exception("El dueño no puede aceptar su propia invitación."))
            }

            val roles = invitation.roles.ifEmpty {
                invitation.rol.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }
            val employee = Empleado(
                uid = user.uid,
                rol = roles.joinToString(","),
                roles = roles,
                activo = true,
                invitacionToken = codigo,
                joinedAt = now,
                updatedAt = now
            )
            runCatching { registrarEmpleado(invitation.establecimientoId, employee).getOrThrow() }
            if (invitation.status == "pending") {
                runCatching {
                    db.collection("invitaciones_empleado").document(codigo).update(
                        "status" to "accepted",
                        "acceptedBy" to user.uid,
                        "acceptedAt" to now
                    )
                }
            }
            db.collection("usuarios").document(user.uid).update(
                "rolGlobal" to Roles.EMPLEADO,
                "establecimientoId" to invitation.establecimientoId,
                "establecimientoNombre" to invitation.establecimientoNombre,
                "rolesEmpleado" to roles,
                "updatedAt" to now
            )
            Result.success(invitation.establecimientoNombre)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cerrarSesion(sesion: SesionTurno): Result<Unit> {
        if (sesion.id.isBlank() || sesion.establecimientoId.isBlank()) {
            return Result.failure(Exception("La sesión no tiene identificador"))
        }
        return try {
            val ahora = Clock.System.now().toEpochMilliseconds()
            db.collection("usuarios").document(sesion.empleadoUid).update(
                "sesionActiva" to false,
                "updatedAt" to ahora
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}