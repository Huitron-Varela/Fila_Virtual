package com.example.fila_virtual.features.empleados

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_cashier_collected
import fila_virtual.composeapp.generated.resources.emp_cashier_incidents
import fila_virtual.composeapp.generated.resources.emp_cashier_orders
import fila_virtual.composeapp.generated.resources.emp_cashier_readonly
import fila_virtual.composeapp.generated.resources.emp_count_listo
import fila_virtual.composeapp.generated.resources.emp_count_preparacion
import fila_virtual.composeapp.generated.resources.emp_count_recibido
import fila_virtual.composeapp.generated.resources.emp_empty_cajero
import fila_virtual.composeapp.generated.resources.emp_status_cancelled
import fila_virtual.composeapp.generated.resources.emp_status_delivered
import fila_virtual.composeapp.generated.resources.emp_status_pending
import fila_virtual.composeapp.generated.resources.emp_status_unclaimed
import fila_virtual.composeapp.generated.resources.emp_turn_label
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@Composable
fun CajeroEstacion(
    establecimientoId: String,
    modifier: Modifier = Modifier,
    viewModel: CajeroViewModel = viewModel()
) {
    LaunchedEffect(establecimientoId) {
        viewModel.escuchar(establecimientoId)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(Res.string.emp_cashier_readonly),
            color = MediumGray,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResumenCajero(Modifier.weight(1f), stringResource(Res.string.emp_cashier_orders), "${viewModel.pedidos.size}")
            ResumenCajero(Modifier.weight(1f), stringResource(Res.string.emp_cashier_collected), dinero(viewModel.cobrado))
            ResumenCajero(Modifier.weight(1f), stringResource(Res.string.emp_cashier_incidents), "${viewModel.incidencias}")
        }
        if (viewModel.error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(viewModel.error, color = TrafficRed, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                viewModel.cargando && viewModel.pedidos.isEmpty() -> {
                    CircularProgressIndicator(color = PrimaryOrange, modifier = Modifier.align(Alignment.Center))
                }
                viewModel.pedidos.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            stringResource(Res.string.emp_empty_cajero),
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MediumGray
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(viewModel.pedidos, key = { it.id }) { pedido ->
                            TransaccionCard(pedido)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenCajero(modifier: Modifier, etiqueta: String, valor: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, fontWeight = FontWeight.Bold, color = PrimaryOrange, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(etiqueta, color = MediumGray, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun TransaccionCard(pedido: Pedido) {
    val incidencia = esIncidencia(pedido.estado)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(18.dp)
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
                    textoEstadoCajero(pedido.estado),
                    color = if (incidencia) TrafficRed else PrimaryOrange,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                pedido.descripcion.ifBlank {
                    pedido.productos.joinToString(", ") { "${it.cantidad}× ${it.nombre}" }
                },
                color = MediumGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(dinero(pedido.total), fontWeight = FontWeight.Bold, color = DarkGray)
        }
    }
}

@Composable
private fun textoEstadoCajero(estado: EstadoPedido): String {
    return when (estado) {
        EstadoPedido.PENDIENTE -> stringResource(Res.string.emp_status_pending)
        EstadoPedido.RECIBIDO -> stringResource(Res.string.emp_count_recibido)
        EstadoPedido.EN_PREPARACION -> stringResource(Res.string.emp_count_preparacion)
        EstadoPedido.LISTO -> stringResource(Res.string.emp_count_listo)
        EstadoPedido.ENTREGADO -> stringResource(Res.string.emp_status_delivered)
        EstadoPedido.NO_RECLAMADO -> stringResource(Res.string.emp_status_unclaimed)
        EstadoPedido.CANCELADO -> stringResource(Res.string.emp_status_cancelled)
    }
}

private fun dinero(total: Double): String {
    val centavos = round(total * 100).toInt()
    val pesos = centavos / 100
    val cents = abs(centavos % 100).toString().padStart(2, '0')
    return "$$pesos.$cents"
}
