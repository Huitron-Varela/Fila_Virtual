package com.example.fila_virtual.features.user.ordenes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


class OrdenesViewModel : ViewModel() {

    /*
     * ==========================================================
     * FIREBASE
     * ==========================================================
     */
    private val db = Firebase.firestore
    private val pedidosRef = db.collection("pedidos")

    /*
     * ==========================================================
     * PEDIDOS ACTIVOS
     * ==========================================================
     */
    private val _pedidosActivos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidosActivos: StateFlow<List<Pedido>> = _pedidosActivos

    /*
     * ==========================================================
     * HISTORIAL
     * ==========================================================
     */
    private val _pedidosHistorial = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidosHistorial: StateFlow<List<Pedido>> = _pedidosHistorial

    /*
     * ==========================================================
     * LOADING
     * ==========================================================
     */
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    /*
     * ==========================================================
     * PEDIDOS CALIFICADOS (Memoria de la sesión)
     * ==========================================================
     */
    private val _pedidosCalificados = MutableStateFlow<Set<String>>(emptySet())
    val pedidosCalificados: StateFlow<Set<String>> = _pedidosCalificados

    fun marcarPedidoComoCalificado(pedidoId: String) {
        _pedidosCalificados.value = _pedidosCalificados.value + pedidoId
    }

    /*
     * ==========================================================
     * INICIO
     * ==========================================================
     */
    init {
        escucharPedidosDelUsuario()
    }

    /*
     * ==========================================================
     * ESCUCHAR PEDIDOS DEL USUARIO
     * ==========================================================
     */
    private fun escucharPedidosDelUsuario() {
        val userId = Firebase.auth.currentUser?.uid

        if (userId == null) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            pedidosRef
                .where { "userId" equalTo userId }
                .snapshots
                .map { snapshot ->
                    snapshot.documents.map { document ->
                        document.data<Pedido>()
                    }
                }
                .catch { exception ->
                    println("ORDENES_VM: Error escuchando pedidos: " + exception.message)
                    _isLoading.value = false
                }
                .collect { todosLosPedidos ->

                    val activos = todosLosPedidos.filter { pedido ->
                        pedido.estado == EstadoPedido.PENDIENTE ||
                                pedido.estado == EstadoPedido.RECIBIDO ||
                                pedido.estado == EstadoPedido.EN_PREPARACION ||
                                pedido.estado == EstadoPedido.LISTO ||
                                pedido.estado == EstadoPedido.ENTREGADO
                    }.sortedByDescending { it.createdAt }

                    val historial = todosLosPedidos.filter { pedido ->
                        pedido.estado == EstadoPedido.ENTREGADO ||
                                pedido.estado == EstadoPedido.CANCELADO
                    }.sortedByDescending { it.createdAt }

                    _pedidosActivos.value = activos
                    _pedidosHistorial.value = historial
                    _isLoading.value = false
                }
        }
    }
}