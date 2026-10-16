package com.example.fila_virtual.features.empleados.estaciones.cocina

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.core.theme.BorderGray
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.ActionBlue
import com.example.fila_virtual.core.theme.LightGray
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.SoftOrangeBg
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.core.theme.TrafficGreen
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.repository.siguienteEstadoCocina
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_action_listo
import fila_virtual.composeapp.generated.resources.emp_action_preparar
import fila_virtual.composeapp.generated.resources.emp_count_listo
import fila_virtual.composeapp.generated.resources.emp_count_preparacion
import fila_virtual.composeapp.generated.resources.emp_count_recibido
import fila_virtual.composeapp.generated.resources.emp_kitchen_filter_empty
import fila_virtual.composeapp.generated.resources.emp_kitchen_blocked
import fila_virtual.composeapp.generated.resources.emp_kitchen_active_orders
import fila_virtual.composeapp.generated.resources.emp_kitchen_no_pending
import fila_virtual.composeapp.generated.resources.emp_pending_title
import fila_virtual.composeapp.generated.resources.emp_turn_label
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CocinaEstacion(
    establecimientoId: String,
    empleadoUid: String,
    sesionId: String,
    turnoGuardado: Boolean,
    modifier: Modifier = Modifier,
    viewModel: CocinaViewModel = viewModel()
) {
    val haptic = LocalHapticFeedback.current
    val boardState = rememberLazyListState()
    var avancePendiente by remember { mutableStateOf<Pair<String, EstadoPedido>?>(null) }
    val columnas = listOf(
        ColumnaCocina(
            FiltroCocina.RECIBIDO,
            stringResource(Res.string.emp_count_recibido),
            viewModel.recibidos,
            ActionBlue
        ),
        ColumnaCocina(
            FiltroCocina.PREPARACION,
            stringResource(Res.string.emp_count_preparacion),
            viewModel.enPreparacion,
            PrimaryOrange
        ),
        ColumnaCocina(
            FiltroCocina.LISTO,
            stringResource(Res.string.emp_count_listo),
            viewModel.listos,
            TrafficGreen
        )
    )

    LaunchedEffect(establecimientoId) {
        viewModel.escuchar(establecimientoId)
    }

    LaunchedEffect(viewModel.pedidos, avancePendiente) {
        val (pedidoId, estadoEsperado) = avancePendiente ?: return@LaunchedEffect
        if (viewModel.pedidos.firstOrNull { it.id == pedidoId }?.estado != estadoEsperado) {
            return@LaunchedEffect
        }

        val indiceDestino = when (estadoEsperado) {
            EstadoPedido.EN_PREPARACION -> 1
            EstadoPedido.LISTO -> 2
            else -> 0
        }
        boardState.animateScrollToItem(indiceDestino)
        if (estadoEsperado == EstadoPedido.LISTO) {
            delay(3_000)
            boardState.animateScrollToItem(0)
        }
        avancePendiente = null
    }

    LaunchedEffect(viewModel.error) {
        if (viewModel.error.isNotBlank()) avancePendiente = null
    }

    Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = LightSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.emp_pending_title),
                        color = DarkGray,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        stringResource(Res.string.emp_kitchen_active_orders, viewModel.pedidos.size),
                        color = PrimaryOrange,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .background(SoftOrangeBg, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                if (viewModel.porElaborar.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewModel.porElaborar.forEach { (nombre, cantidad) ->
                            Surface(color = LightGray, shape = RoundedCornerShape(50)) {
                                Text(
                                    "$cantidad× $nombre",
                                    color = DarkGray,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        stringResource(Res.string.emp_kitchen_no_pending),
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (!turnoGuardado) {
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(Res.string.emp_kitchen_blocked),
                color = MediumGray,
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (viewModel.error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(viewModel.error, color = TrafficRed, style = MaterialTheme.typography.bodySmall)
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                viewModel.cargando && viewModel.pedidos.isEmpty() -> {
                    CircularProgressIndicator(
                        color = PrimaryOrange,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val anchoColumna = if (maxWidth < 600.dp) {
                            maxWidth * 0.88f
                        } else {
                            (maxWidth / 3).coerceIn(300.dp, 400.dp)
                        }
                        LazyRow(
                            state = boardState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(end = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(columnas, key = { it.filtro.name }) { columna ->
                                ColumnaKanban(
                                    columna = columna,
                                    pedidos = pedidosDelFiltro(viewModel.pedidos, columna.filtro),
                                    ancho = anchoColumna,
                                    turnoGuardado = turnoGuardado,
                                    sesionId = sesionId,
                                    pedidosEnCambio = viewModel.pedidosEnCambio,
                                    onAvanzar = { pedido ->
                                        val siguiente = siguienteEstadoCocina(pedido.estado)
                                            ?: return@ColumnaKanban
                                        avancePendiente = pedido.id to siguiente
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.avanzar(pedido, empleadoUid, sesionId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class FiltroCocina { RECIBIDO, PREPARACION, LISTO }

private data class ColumnaCocina(
    val filtro: FiltroCocina,
    val titulo: String,
    val cantidad: Int,
    val color: Color
)

private fun pedidosDelFiltro(pedidos: List<Pedido>, filtro: FiltroCocina): List<Pedido> {
    return pedidos.filter { pedido ->
        when (filtro) {
            FiltroCocina.RECIBIDO -> pedido.estado == EstadoPedido.PENDIENTE || pedido.estado == EstadoPedido.RECIBIDO
            FiltroCocina.PREPARACION -> pedido.estado == EstadoPedido.EN_PREPARACION
            FiltroCocina.LISTO -> pedido.estado == EstadoPedido.LISTO
        }
    }
}

@Composable
private fun ColumnaKanban(
    columna: ColumnaCocina,
    pedidos: List<Pedido>,
    ancho: androidx.compose.ui.unit.Dp,
    turnoGuardado: Boolean,
    sesionId: String,
    pedidosEnCambio: Set<String>,
    onAvanzar: (Pedido) -> Unit
) {
    Surface(
        modifier = Modifier.width(ancho).fillMaxHeight(),
        color = LightGray,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(Modifier.fillMaxSize().padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(10.dp)
                        .background(columna.color, RoundedCornerShape(50))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    columna.titulo,
                    color = DarkGray,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    columna.cantidad.toString(),
                    color = columna.color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .background(columna.color.copy(alpha = 0.14f), RoundedCornerShape(50))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            if (pedidos.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(Res.string.emp_kitchen_filter_empty, columna.titulo.lowercase()),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center,
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pedidos, key = { it.id }) { pedido ->
                        PedidoCocinaCard(
                            pedido = pedido,
                            estadoColor = columna.color,
                            habilitado = turnoGuardado && sesionId.isNotBlank() && columna.filtro != FiltroCocina.LISTO,
                            enCambio = pedido.id in pedidosEnCambio,
                            onAvanzar = { onAvanzar(pedido) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PedidoCocinaCard(
    pedido: Pedido,
    estadoColor: Color,
    habilitado: Boolean,
    enCambio: Boolean,
    onAvanzar: () -> Unit
) {
    val accion = when (siguienteEstadoCocina(pedido.estado)) {
        EstadoPedido.EN_PREPARACION -> stringResource(Res.string.emp_action_preparar)
        EstadoPedido.LISTO -> stringResource(Res.string.emp_action_listo)
        else -> null
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.emp_turn_label, pedido.turno),
                    fontWeight = FontWeight.Bold,
                    color = DarkGray,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    textoEstado(pedido.estado),
                    color = estadoColor,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .background(estadoColor.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                pedido.descripcion.ifBlank {
                    pedido.productos.joinToString(", ") { "${it.cantidad}× ${it.nombre}" }
                },
                color = MediumGray,
                style = MaterialTheme.typography.bodyMedium
            )
            if (accion != null) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAvanzar,
                    enabled = habilitado && !enCambio,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    if (enCambio) {
                        CircularProgressIndicator(color = LightSurface, modifier = Modifier.height(18.dp))
                    } else {
                        Icon(
                            imageVector = if (pedido.estado == EstadoPedido.LISTO) Icons.Default.Check else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(accion, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun textoEstado(estado: EstadoPedido): String {
    return when (estado) {
        EstadoPedido.PENDIENTE, EstadoPedido.RECIBIDO -> stringResource(Res.string.emp_count_recibido)
        EstadoPedido.EN_PREPARACION -> stringResource(Res.string.emp_count_preparacion)
        EstadoPedido.LISTO -> stringResource(Res.string.emp_count_listo)
        else -> estado.name
    }
}

