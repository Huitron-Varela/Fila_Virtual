package com.example.fila_virtual.repository

import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class CocinaRepository {
    private val db = Firebase.firestore
    private val pedidosRef = db.collection("pedidos")

    fun escucharActivos(establecimientoId: String): Flow<List<Pedido>> {
        return pedidosRef
            .where { "establecimientoId" equalTo establecimientoId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { documento ->
                    runCatching { documento.data<Pedido>().copy(id = documento.id) }.getOrNull()
                }.filter { pedido ->
                    pedido.estado == EstadoPedido.PENDIENTE ||
                        pedido.estado == EstadoPedido.RECIBIDO ||
                        pedido.estado == EstadoPedido.EN_PREPARACION ||
                        pedido.estado == EstadoPedido.LISTO
                }.sortedWith(compareBy({ it.createdAt }, { it.turno }))
            }
    }

    suspend fun avanzarEstado(
        pedidoId: String,
        empleadoUid: String,
        sesionId: String
    ): Result<EstadoPedido> {
        if (pedidoId.isBlank()) return Result.failure(Exception("El pedido no tiene identificador"))
        return try {
            val referencia = pedidosRef.document(pedidoId)
            val ahora = Clock.System.now().toEpochMilliseconds()
            val nuevoEstado = db.runTransaction {
                val actual = runCatching {
                    EstadoPedido.valueOf(get(referencia).get<String>("estado"))
                }.getOrNull()
                val siguiente = siguienteEstadoCocina(actual)
                    ?: throw IllegalStateException("El pedido ya cambió de estado")
                update(
                    referencia,
                    "estado" to siguiente.name,
                    "empleadoUid" to empleadoUid,
                    "sesionId" to sesionId,
                    "actualizadoAt" to ahora
                )
                siguiente
            }
            Result.success(nuevoEstado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun siguienteEstadoCocina(actual: EstadoPedido?): EstadoPedido? {
    return when (actual) {
        EstadoPedido.PENDIENTE, EstadoPedido.RECIBIDO -> EstadoPedido.EN_PREPARACION
        EstadoPedido.EN_PREPARACION -> EstadoPedido.LISTO
        else -> null
    }
}
