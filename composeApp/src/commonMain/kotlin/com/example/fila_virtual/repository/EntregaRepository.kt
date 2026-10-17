package com.example.fila_virtual.repository

import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class EntregaRepository {
    private val db = Firebase.firestore
    private val pedidosRef = db.collection("pedidos")

    fun escucharPedidosEntrega(establecimientoId: String): Flow<List<Pedido>> {
        return pedidosRef
            .where { "establecimientoId" equalTo establecimientoId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { documento ->
                    runCatching { documento.data<Pedido>().copy(id = documento.id) }.getOrNull()
                }.filter {
                    it.estado == EstadoPedido.LISTO || it.estado == EstadoPedido.ENTREGADO
                }
                    .sortedWith(compareBy({ it.createdAt }, { it.turno }))
            }
    }

    suspend fun cerrarEntrega(
        pedidoId: String,
        destino: EstadoPedido,
        establecimientoId: String,
        empleadoUid: String,
        sesionId: String
    ): Result<Unit> {
        if (destino != EstadoPedido.ENTREGADO && destino != EstadoPedido.NO_RECLAMADO) {
            return Result.failure(Exception("Esa acción no corresponde a entrega"))
        }
        return try {
            val referencia = pedidosRef.document(pedidoId)
            val ahora = Clock.System.now().toEpochMilliseconds()
            db.runTransaction {
                val snapshot = get(referencia)
                val actual = runCatching { EstadoPedido.valueOf(snapshot.get<String>("estado")) }.getOrNull()
                val local = runCatching { snapshot.get<String>("establecimientoId") }.getOrNull()
                if (actual != EstadoPedido.LISTO || local != establecimientoId) {
                    throw IllegalStateException("El pedido ya no está listo para entregar")
                }
                update(
                    referencia,
                    "estado" to destino.name,
                    "empleadoUid" to empleadoUid,
                    "sesionId" to sesionId,
                    "actualizadoAt" to ahora
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
