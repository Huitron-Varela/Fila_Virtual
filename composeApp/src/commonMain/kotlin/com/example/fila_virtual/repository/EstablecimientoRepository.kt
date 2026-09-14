package com.example.fila_virtual.repository

import com.example.fila_virtual.data.Establecimiento
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EstablecimientoRepository {
    private val db = Firebase.firestore
    private val establecimientosRef = db.collection("establecimientos")

    suspend fun guardarEstablecimiento(establecimiento: Establecimiento): Result<Unit> {
        return try {
            if (establecimiento.id.isEmpty()) {
                // Para nuevos locales, usamos add() que genera el ID automáticamente y evita el error de segmentos
                val docRef = establecimientosRef.add(establecimiento)
                docRef.update("id" to docRef.id)
            } else {
                establecimientosRef.document(establecimiento.id).set(establecimiento)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun actualizarEstado(id: String, activo: Boolean): Result<Unit> {
        if (id.isEmpty()) return Result.failure(Exception("ID no válido"))
        return try {
            establecimientosRef.document(id).update("activo" to activo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarEstablecimiento(id: String): Result<Unit> {
        if (id.isEmpty()) return Result.failure(Exception("ID no válido"))
        return try {
            establecimientosRef.document(id).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getEstablecimientos(): Flow<List<Establecimiento>> {
        return establecimientosRef.snapshots.map { snapshot ->
            snapshot.documents.map { it.data<Establecimiento>() }
        }
    }

    fun getEstablecimientosByOwner(ownerUid: String): Flow<List<Establecimiento>> {
        return establecimientosRef.where { "ownerUid" equalTo ownerUid }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<Establecimiento>() }
            }
    }
}