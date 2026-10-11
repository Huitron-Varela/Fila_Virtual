package com.example.fila_virtual.repository

import com.example.fila_virtual.data.Roles
import com.example.fila_virtual.data.TarjetaGuardada
import com.example.fila_virtual.data.Usuario
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.firestore.firestore

class UserRepository {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    fun getCurrentUserUid(): String? = auth.currentUser?.uid
    
    fun getFirebaseUser(): FirebaseUser? = auth.currentUser

    suspend fun getUserData(uid: String): Usuario? {
        return try {
            val document = db.collection("usuarios").document(uid).get()
            if (document.exists) {
                val rolGuardado = if (document.contains("rolGlobal")) {
                    runCatching { document.get<String>("rolGlobal") }.getOrNull()
                } else {
                    null
                }
                val rolGlobal = Roles.normalizar(rolGuardado)
                try {
                    val user = document.data<Usuario>()
                    user.copy(uid = uid, rolGlobal = rolGlobal)
                } catch (serializationException: Exception) {
                    println("🔥 Serialization Error, attempting safe fallback: ${serializationException.message}")
                    val nombre = if (document.contains("nombre")) document.get<String>("nombre") else ""
                    val email = if (document.contains("email")) document.get<String>("email") else ""
                    val telefono = if (document.contains("telefono")) document.get<String>("telefono") else ""
                    val fotoUrl = if (document.contains("fotoUrl")) document.get<String>("fotoUrl") else ""
                    val verificado = if (document.contains("verificado")) document.get<Boolean>("verificado") else false
                    val activo = if (document.contains("activo")) document.get<Boolean>("activo") else true
                    val createdAt = if (document.contains("createdAt")) document.get<Long>("createdAt") else 0L
                    val updatedAt = if (document.contains("updatedAt")) document.get<Long>("updatedAt") else 0L
                    
                    val metodosPagoList = mutableListOf<TarjetaGuardada>()
                    if (document.contains("metodosPago")) {
                        try {
                            val rawList = document.get<List<Any>>("metodosPago")
                            for (item in rawList) {
                                if (item is Map<*, *>) {
                                    val ultimos4 = item["ultimos4"] as? String ?: ""
                                    val marca = item["marca"] as? String ?: "VISA"
                                    val nombreTitular = item["nombreTitular"] as? String ?: ""
                                    val expiracion = item["expiracion"] as? String ?: ""
                                    val tokenId = item["tokenId"] as? String ?: ""
                                    metodosPagoList.add(TarjetaGuardada(ultimos4, marca, nombreTitular, expiracion, tokenId))
                                } else if (item is String) {
                                    val ultimos4 = item.takeLast(4)
                                    metodosPagoList.add(
                                        TarjetaGuardada(
                                            ultimos4 = if (ultimos4.all { it.isDigit() }) ultimos4 else "----",
                                            marca = "VISA",
                                            nombreTitular = nombre,
                                            expiracion = "--/--",
                                            tokenId = ""
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            println("🔥 Error parsing metodosPago in fallback: ${e.message}")
                        }
                    }
                    
                    Usuario(
                        uid = uid,
                        nombre = nombre,
                        email = email,
                        telefono = telefono,
                        fotoUrl = fotoUrl,
                        rolGlobal = rolGlobal,
                        metodosPago = metodosPagoList,
                        verificado = verificado,
                        activo = activo,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                }
            } else {
                null
            }
        } catch (e: Exception) {
            println("🔥 Repo Error: ${e.message}")
            null
        }
    }

    suspend fun updateUserData(uid: String, updates: Map<String, Any?>): Boolean {
        return try {
            db.collection("usuarios").document(uid).update(updates)
            true
        } catch (e: Exception) {
            println("🔥 Update Error: ${e.message}")
            false
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }
}
