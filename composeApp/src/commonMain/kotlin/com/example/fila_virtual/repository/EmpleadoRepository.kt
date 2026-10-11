package com.example.fila_virtual.repository

import com.example.fila_virtual.data.AsignacionEmpleado
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.MetricaDiaria
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.ResumenTurno
import com.example.fila_virtual.data.SesionMetrica
import com.example.fila_virtual.data.Empleado
import com.example.fila_virtual.data.EstacionOperativa
import com.example.fila_virtual.data.InvitacionEmpleado
import com.example.fila_virtual.data.Roles
import com.example.fila_virtual.data.SesionTurno
import com.example.fila_virtual.data.Usuario
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
                "sesionPreparados" to 0,
                "sesionEntregados" to 0,
                "updatedAt" to ahora
            )
            Result.success(sesion)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun tieneInvitacionPendiente(correo: String): Boolean {
        val email = correo.trim()
        if (email.isBlank()) return false
        return try {
            val ahora = Clock.System.now().toEpochMilliseconds()
            val snapshot = db.collection("invitaciones_empleado")
                .where { "correo" equalTo email }
                .get()
            snapshot.documents.any { documento ->
                val invitacion = runCatching { documento.data<InvitacionEmpleado>() }.getOrNull()
                    ?: return@any false
                invitacion.status == "pending" &&
                    invitacion.correo.equals(email, ignoreCase = true) &&
                    (invitacion.expiresAt == 0L || invitacion.expiresAt > ahora)
            }
        } catch (_: Exception) {
            false
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

    suspend fun sumarPreparado(uid: String) {
        sumarContador(uid, "sesionPreparados")
    }

    suspend fun sumarEntregado(uid: String) {
        sumarContador(uid, "sesionEntregados")
    }

    suspend fun cerrarSesion(sesion: SesionTurno): Result<ResumenTurno> {
        if (sesion.id.isBlank() || sesion.establecimientoId.isBlank()) {
            return Result.failure(Exception("La sesión no tiene identificador"))
        }
        return try {
            val ahora = Clock.System.now().toEpochMilliseconds()
            val usuarioRef = db.collection("usuarios").document(sesion.empleadoUid)
            val usuario = usuarioRef.get()
            val contados = contarPorSesion(sesion)
            val preparados = maxOf(leerEntero(usuario, "sesionPreparados"), contados.first)
            val entregados = maxOf(leerEntero(usuario, "sesionEntregados"), contados.second)
            val guardado = guardarMetrica(usuarioRef, usuario, sesion, ahora, preparados, entregados)
            Result.success(
                ResumenTurno(
                    estacion = sesion.estacion,
                    inicioAt = sesion.inicioAt,
                    finAt = ahora,
                    preparados = preparados,
                    entregados = entregados,
                    registroGuardado = guardado
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun sumarContador(uid: String, campo: String) {
        if (uid.isBlank()) return
        val referencia = db.collection("usuarios").document(uid)
        val actual = leerEntero(referencia.get(), campo)
        referencia.update(campo to actual + 1)
    }

    private suspend fun contarPorSesion(sesion: SesionTurno): Pair<Int, Int> {
        if (sesion.establecimientoId.isBlank()) return 0 to 0
        return try {
            val snapshot = db.collection("pedidos")
                .where { "establecimientoId" equalTo sesion.establecimientoId }
                .get()
            val mios = snapshot.documents.mapNotNull { documento ->
                runCatching { documento.data<Pedido>().copy(id = documento.id) }.getOrNull()
            }.filter { it.sesionId == sesion.id }
            val preparados = if (sesion.estacion == EstacionOperativa.COCINA.clave()) {
                mios.count { it.estado == EstadoPedido.LISTO }
            } else {
                0
            }
            val entregados = if (sesion.estacion == EstacionOperativa.ENTREGA.clave()) {
                mios.count { it.estado == EstadoPedido.ENTREGADO }
            } else {
                0
            }
            preparados to entregados
        } catch (_: Exception) {
            0 to 0
        }
    }

    private suspend fun guardarMetrica(
        usuarioRef: dev.gitlive.firebase.firestore.DocumentReference,
        usuario: dev.gitlive.firebase.firestore.DocumentSnapshot,
        sesion: SesionTurno,
        finAt: Long,
        preparados: Int,
        entregados: Int
    ): Boolean {
        val fecha = Instant.fromEpochMilliseconds(finAt)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
            .toString()
        val cuenta = runCatching { usuario.data<Usuario>() }.getOrNull()
        val previas = if (cuenta?.metricasFecha == fecha) cuenta.metricasSesiones else emptyList()
        val yaCerrada = previas.any { it.id == sesion.id && it.finAt > 0L }
        val cerrada = SesionMetrica(
            id = sesion.id,
            estacion = sesion.estacion,
            inicioAt = sesion.inicioAt,
            finAt = finAt,
            preparados = preparados,
            entregados = entregados
        )
        val actualizadas = if (yaCerrada) previas else previas.filter { it.id != sesion.id } + cerrada
        val guardadoEnCuenta = try {
            usuarioRef.update(
                "sesionActiva" to false,
                "updatedAt" to finAt,
                "metricasFecha" to fecha,
                "metricasSesiones" to actualizadas
            )
            true
        } catch (_: Exception) {
            false
        }
        runCatching {
            db.collection("empleadoMetricasDiarias")
                .document("${sesion.empleadoUid}_$fecha")
                .set(
                    MetricaDiaria(
                        empleadoUid = sesion.empleadoUid,
                        establecimientoId = sesion.establecimientoId,
                        fecha = fecha,
                        sesiones = actualizadas
                    )
                )
        }
        return guardadoEnCuenta
    }
}

private fun leerEntero(documento: dev.gitlive.firebase.firestore.DocumentSnapshot, campo: String): Int {
    if (!documento.contains(campo)) return 0
    return runCatching { documento.get<Long>(campo).toInt() }.getOrNull()
        ?: runCatching { documento.get<Int>(campo) }.getOrNull()
        ?: 0
}