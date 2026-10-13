package com.example.fila_virtual.repository

import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.data.Producto
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

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
                snapshot.documents.map { documento ->
                    documento.data<Producto>().copy(id = documento.id)
                }
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

    suspend fun actualizarDatosMenu(
        productoId: String,
        nombre: String,
        descripcion: String,
        precio: Double,
        categoria: String,
        imagenUrl: String = ""
    ): Result<Unit> {
        if (productoId.isBlank()) return Result.failure(Exception("Producto no válido"))
        return try {
            val ahora = Clock.System.now().toEpochMilliseconds()
            if (imagenUrl.isBlank()) {
                productosRef.document(productoId).update(
                    "nombre" to nombre,
                    "descripcion" to descripcion,
                    "precio" to precio,
                    "categoria" to categoria,
                    "updatedAt" to ahora
                )
            } else {
                productosRef.document(productoId).update(
                    "nombre" to nombre,
                    "descripcion" to descripcion,
                    "precio" to precio,
                    "categoria" to categoria,
                    "imagenUrl" to imagenUrl,
                    "updatedAt" to ahora
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (!e.message.orEmpty().contains("PERMISSION_DENIED", ignoreCase = true)) {
                return Result.failure(e)
            }
            try {
                if (imagenUrl.isBlank()) {
                    productosRef.document(productoId).update(
                        "nombre" to nombre,
                        "descripcion" to descripcion,
                        "precio" to precio,
                        "categoria" to categoria
                    )
                } else {
                    productosRef.document(productoId).update(
                        "nombre" to nombre,
                        "descripcion" to descripcion,
                        "precio" to precio,
                        "categoria" to categoria,
                        "imagenUrl" to imagenUrl
                    )
                }
                Result.success(Unit)
            } catch (segundo: Exception) {
                Result.failure(segundo)
            }
        }
    }

    suspend fun darDeBaja(productoId: String): Result<Unit> {
        if (productoId.isBlank()) return Result.failure(Exception("Producto no válido"))
        return try {
            productosRef.document(productoId).update(
                "enCatalogo" to false,
                "disponible" to false,
                "updatedAt" to Clock.System.now().toEpochMilliseconds()
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun crearPlatillo(
        establecimientoId: String,
        ownerUid: String,
        nombre: String,
        descripcion: String,
        precio: Double,
        categoria: String,
        imagenUrl: String = ""
    ): Result<Unit> {
        if (establecimientoId.isBlank()) return Result.failure(Exception("Establecimiento no válido"))
        return try {
            val ahora = Clock.System.now().toEpochMilliseconds()
            val docRef = productosRef.document
            docRef.set(
                Producto(
                    id = docRef.id,
                    establecimientoId = establecimientoId,
                    ownerUid = ownerUid,
                    nombre = nombre,
                    descripcion = descripcion,
                    precio = precio,
                    categoria = categoria,
                    imagenUrl = imagenUrl,
                    disponible = true,
                    enCatalogo = true,
                    createdAt = ahora,
                    updatedAt = ahora
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leerOwnerUid(establecimientoId: String): String {
        if (establecimientoId.isBlank()) return ""
        return try {
            val documento = db.collection("establecimientos").document(establecimientoId).get()
            if (!documento.exists) "" else documento.data<Establecimiento>().ownerUid
        } catch (_: Exception) {
            ""
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