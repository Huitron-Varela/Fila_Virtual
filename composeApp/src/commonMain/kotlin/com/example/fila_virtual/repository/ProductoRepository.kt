package com.example.fila_virtual.repository

import com.example.fila_virtual.data.Producto
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductoRepository {
    private val db = Firebase.firestore
    private val productosRef = db.collection("productos")

    suspend fun guardarProducto(producto: Producto): Result<Unit> {
        return try {
            val docRef = if (producto.id.isEmpty()) {
                productosRef.document
            } else {
                productosRef.document(producto.id)
            }

            val finalProducto = producto.copy(id = docRef.id)

            docRef.set(finalProducto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getProductos(establecimientoId: String): Flow<List<Producto>> {
        return productosRef
            .where { "establecimientoId" equalTo establecimientoId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<Producto>() }
            }
    }

    fun getProductosByOwner(ownerUid: String): Flow<List<Producto>> {
        return productosRef
            .where { "ownerUid" equalTo ownerUid }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<Producto>() }
            }
    }

    suspend fun actualizarDisponibilidad(productoId: String, disponible: Boolean): Result<Unit> {
        return try {
            productosRef.document(productoId).update("disponible" to disponible)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun eliminarProducto(productoId: String): Result<Unit> {
        return try {
            productosRef.document(productoId).delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getProductosGlobales(): Flow<List<Producto>> {
        return productosRef
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { it.data<Producto>() }
                    .sortedByDescending { it.ratingPromedio }
            }
    }

    suspend fun calificarProducto(productoId: String, nuevasEstrellas: Int): Result<Unit> {
        return try {
            val docRef = productosRef.document(productoId)

            db.runTransaction {
                val snapshot = get(docRef)

                // Extraemos los valores como "Number" de forma súper segura con try-catch
                // Si el campo no existe, atrapamos el error silenciosamente y usamos 0
                val votosAnteriores = try {
                    snapshot.get<Number>("totalVotos").toInt()
                } catch (e: Exception) {
                    0
                }

                val promedioAnterior = try {
                    snapshot.get<Number>("ratingPromedio").toDouble()
                } catch (e: Exception) {
                    0.0
                }

                val nuevosVotos = votosAnteriores + 1
                val nuevoPromedio = ((promedioAnterior * votosAnteriores) + nuevasEstrellas) / nuevosVotos

                // Actualizamos, y si los campos no existían, Firebase los creará
                update(docRef, "totalVotos" to nuevosVotos, "ratingPromedio" to nuevoPromedio)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            println("Error al calificar: ${e.message}")
            Result.failure(e)
        }
    }
}