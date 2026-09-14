package com.example.fila_virtual.repository

import com.example.fila_virtual.data.Empleado
import dev.gitlive.firebase.Firebase
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
}