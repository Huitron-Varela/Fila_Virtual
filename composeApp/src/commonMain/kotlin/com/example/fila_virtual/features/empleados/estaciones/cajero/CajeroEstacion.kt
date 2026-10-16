package com.example.fila_virtual.features.empleados.estaciones.cajero

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.ActionBlue
import com.example.fila_virtual.core.theme.BorderGray
import com.example.fila_virtual.core.theme.LightGray
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.core.theme.TrafficGreen
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import androidx.compose.ui.graphics.vector.ImageVector
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_cashier_collected
import fila_virtual.composeapp.generated.resources.emp_cashier_incidents
import fila_virtual.composeapp.generated.resources.emp_cashier_orders
import fila_virtual.composeapp.generated.resources.emp_cashier_ticket
import fila_virtual.composeapp.generated.resources.emp_cashier_total
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

    AppPullToRefreshBox(
        isRefreshing = viewModel.cargando,
        onRefresh = { viewModel.escuchar(establecimientoId, forceRefresh = true) },
        modifier = modifier.fillMaxSize()
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ResumenCajero(
                modifier = Modifier.weight(1f),
                etiqueta = stringResource(Res.string.emp_cashier_orders),
                valor = viewModel.pedidos.size.toString(),
                icono = Icons.Default.ReceiptLong,
                color = ActionBlue
            )
            ResumenCajero(
                modifier = Modifier.weight(1f),
                etiqueta = stringResource(Res.string.emp_cashier_collected),
                valor = dinero(viewModel.cobrado),
                icono = Icons.Default.AccountBalanceWallet,
                color = TrafficGreen
            )
            ResumenCajero(
                modifier = Modifier.weight(1f),
                etiqueta = stringResource(Res.string.emp_cashier_incidents),
                valor = viewModel.incidencias.toString(),
                icono = Icons.Default.ErrorOutline,
                color = TrafficRed
            )
        }
        if (viewModel.error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Surface(color = Color(0xFFFFF1F0), shape = RoundedCornerShape(12.dp)) {
                Text(
                    viewModel.error,
                    color = TrafficRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                viewModel.cargando && viewModel.pedidos.isEmpty() -> {
                    AppListSkeleton(Modifier.fillMaxSize().padding(8.dp), itemCount = 4)
                }
                viewModel.pedidos.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MediumGray, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(10.dp))
                            Text(stringResource(Res.string.emp_empty_cajero), textAlign = TextAlign.Center, color = MediumGray)
                        }
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
}

@Composable
private fun ResumenCajero(
    modifier: Modifier,
    etiqueta: String,
    valor: String,
    icono: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier.size(30.dp).background(color.copy(alpha = 0.12f), RoundedCornerShape(9.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                valor,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                etiqueta,
                color = MediumGray,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TransaccionCard(pedido: Pedido) {
    val colorEstado = colorEstadoCajero(pedido.estado)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(46.dp).background(colorEstado.copy(alpha = 0.14f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        pedido.turno.toString(),
                        fontWeight = FontWeight.Bold,
                        color = colorEstado,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.emp_turn_label, pedido.turno),
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        stringResource(Res.string.emp_cashier_ticket, pedido.id.takeLast(4).uppercase()),
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Surface(color = colorEstado.copy(alpha = 0.12f), shape = RoundedCornerShape(50)) {
                    Text(
                        textoEstadoCajero(pedido.estado),
                        color = colorEstado,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().background(LightGray, RoundedCornerShape(13.dp)).padding(11.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Fastfood, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    pedido.descripcion.ifBlank {
                        pedido.productos.joinToString(", ") { "${it.cantidad}× ${it.nombre}" }
                    },
                    color = DarkGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.emp_cashier_total),
                    color = MediumGray,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                Text(dinero(pedido.total), fontWeight = FontWeight.Bold, color = DarkGray, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

private fun colorEstadoCajero(estado: EstadoPedido): Color {
    return when (estado) {
        EstadoPedido.PENDIENTE -> ActionBlue
        EstadoPedido.RECIBIDO -> ActionBlue
        EstadoPedido.EN_PREPARACION -> PrimaryOrange
        EstadoPedido.LISTO -> TrafficGreen
        EstadoPedido.ENTREGADO -> MediumGray
        EstadoPedido.NO_RECLAMADO, EstadoPedido.CANCELADO -> TrafficRed
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
