package com.example.fila_virtual.features.empleados

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.core.theme.BorderGray
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.SoftOrangeBg
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.repository.siguienteEstadoCocina
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_action_listo
import fila_virtual.composeapp.generated.resources.emp_action_preparar
import fila_virtual.composeapp.generated.resources.emp_count_listo
import fila_virtual.composeapp.generated.resources.emp_count_preparacion
import fila_virtual.composeapp.generated.resources.emp_count_recibido
import fila_virtual.composeapp.generated.resources.emp_empty_cocina
import fila_virtual.composeapp.generated.resources.emp_kitchen_filter_empty
import fila_virtual.composeapp.generated.resources.emp_kitchen_blocked
import fila_virtual.composeapp.generated.resources.emp_pending_title
import fila_virtual.composeapp.generated.resources.emp_turn_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun CocinaEstacion(
    establecimientoId: String,
    empleadoUid: String,
    sesionId: String,
    turnoGuardado: Boolean,
    modifier: Modifier = Modifier,
    viewModel: CocinaViewModel = viewModel()
) {
    var filtro by remember { mutableStateOf(FiltroCocina.RECIBIDO) }
    val etiquetaFiltro = when (filtro) {
        FiltroCocina.RECIBIDO -> stringResource(Res.string.emp_count_recibido)
        FiltroCocina.PREPARACION -> stringResource(Res.string.emp_count_preparacion)
        FiltroCocina.LISTO -> stringResource(Res.string.emp_count_listo)
    }

    LaunchedEffect(establecimientoId) {
        viewModel.escuchar(establecimientoId)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ConteoCocina(
                Modifier.weight(1f),
                stringResource(Res.string.emp_count_recibido),
                viewModel.recibidos,
                filtro == FiltroCocina.RECIBIDO
            ) { filtro = FiltroCocina.RECIBIDO }
            ConteoCocina(
                Modifier.weight(1f),
                stringResource(Res.string.emp_count_preparacion),
                viewModel.enPreparacion,
                filtro == FiltroCocina.PREPARACION
            ) { filtro = FiltroCocina.PREPARACION }
            ConteoCocina(
                Modifier.weight(1f),
                stringResource(Res.string.emp_count_listo),
                viewModel.listos,
                filtro == FiltroCocina.LISTO
            ) { filtro = FiltroCocina.LISTO }
        }

        if (viewModel.porElaborar.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(Res.string.emp_pending_title),
                color = DarkGray,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(Modifier.height(6.dp))
            Text(
                viewModel.porElaborar.joinToString("   ") { (nombre, cantidad) -> "${cantidad}× $nombre" },
                color = MediumGray,
                style = MaterialTheme.typography.bodySmall
            )
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

        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                viewModel.cargando && viewModel.pedidos.isEmpty() -> {
                    CircularProgressIndicator(
                        color = PrimaryOrange,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                viewModel.pedidos.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            stringResource(Res.string.emp_empty_cocina),
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MediumGray
                        )
                    }
                }
                else -> {
                    val visibles = pedidosDelFiltro(viewModel.pedidos, filtro)
                    if (visibles.isEmpty()) {
                        Text(
                            stringResource(Res.string.emp_kitchen_filter_empty, etiquetaFiltro),
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MediumGray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(visibles, key = { it.id }) { pedido ->
                                PedidoCocinaCard(
                                    pedido = pedido,
                                    habilitado = turnoGuardado && sesionId.isNotBlank() && filtro != FiltroCocina.LISTO,
                                    enCambio = pedido.id in viewModel.pedidosEnCambio,
                                    onAvanzar = { viewModel.avanzar(pedido, empleadoUid, sesionId) }
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
private fun PedidoCocinaCard(
    pedido: Pedido,
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
                    color = PrimaryOrange,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .background(SoftOrangeBg, RoundedCornerShape(12.dp))
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

@Composable
private fun ConteoCocina(
    modifier: Modifier,
    etiqueta: String,
    cantidad: Int,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (seleccionado) SoftOrangeBg else LightSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (seleccionado) 2.dp else 1.dp, if (seleccionado) PrimaryOrange else BorderGray)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("$cantidad", fontWeight = FontWeight.Bold, color = PrimaryOrange, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(etiqueta, color = MediumGray, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}
