package com.example.fila_virtual.repository

import com.example.fila_virtual.data.Pedido
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CajeroRepository {
    private val pedidosRef = Firebase.firestore.collection("pedidos")

    fun escuchar(establecimientoId: String): Flow<List<Pedido>> {
        return pedidosRef
            .where { "establecimientoId" equalTo establecimientoId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { documento ->
                    runCatching { documento.data<Pedido>().copy(id = documento.id) }.getOrNull()
                }.sortedByDescending { it.createdAt }
            }
    }
}
