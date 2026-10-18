package com.example.fila_virtual.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.orders_active
import fila_virtual.composeapp.generated.resources.orders_all_filter
import fila_virtual.composeapp.generated.resources.orders_history
import fila_virtual.composeapp.generated.resources.orders_pickup
import fila_virtual.composeapp.generated.resources.orders_rate
import fila_virtual.composeapp.generated.resources.orders_rated
import fila_virtual.composeapp.generated.resources.orders_reorder
import fila_virtual.composeapp.generated.resources.orders_turn
import fila_virtual.composeapp.generated.resources.orders_total
import org.jetbrains.compose.resources.stringResource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round

@Composable
fun OrdersSelector(
    selectedTab: Int,
    activeCount: Int,
    historyCount: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ExtraLightGray, RoundedCornerShape(22.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                stringResource(Res.string.orders_active) to activeCount,
                stringResource(Res.string.orders_history) to historyCount
            ).forEachIndexed { index, (title, count) ->
                val selected = selectedTab == index
                Surface(
                    modifier = Modifier.weight(1f).heightIn(min = 42.dp).clickable {
                        if (!selected) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onTabSelected(index)
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) LightSurface else Color.Transparent,
                    shadowElevation = if (selected) 1.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = if (selected) DarkGray else MediumGray,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (count > 0 && selected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(Modifier.size(7.dp).background(PrimaryOrange, CircleShape))
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun ActiveOrderCard(
    pedido: Pedido,
    status: String,
    statusColor: Color,
    onClick: () -> Unit
) {
    OrderCardSurface(pedido = pedido, status = status, statusColor = statusColor, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val summary = orderProductsSummary(pedido)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = summary,
                    color = DarkGray,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MediumGray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(Res.string.orders_pickup),
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            OrderTotal(pedido.total)
        }
    }
}

@Composable
fun HistoryOrderCard(
    pedido: Pedido,
    status: String,
    statusColor: Color,
    yaCalificado: Boolean,
    canReorder: Boolean,
    onRateClick: () -> Unit,
    onReorderClick: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    OrderCardSurface(pedido = pedido, status = status, statusColor = statusColor) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = orderProductsSummary(pedido),
                        color = DarkGray,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MediumGray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(Res.string.orders_pickup),
                            color = MediumGray,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                OrderTotal(pedido.total)
            }
            HorizontalDivider(color = BorderGray.copy(alpha = 0.55f))
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRateClick()
                    },
                    enabled = !yaCalificado,
                    modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (yaCalificado) BorderGray else PrimaryOrange),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryOrange,
                        disabledContentColor = MediumGray
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(if (yaCalificado) Res.string.orders_rated else Res.string.orders_rate),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReorderClick()
                    },
                    enabled = canReorder,
                    modifier = Modifier.weight(1f).heightIn(min = 46.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(Res.string.orders_reorder),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderCardSurface(
    pedido: Pedido,
    status: String,
    statusColor: Color,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val title = pedido.establecimientoNombre.ifBlank { pedido.descripcion.ifBlank { "Establecimiento" } }
    Card(
        modifier = Modifier.fillMaxWidth().then(onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(58.dp).background(SoftOrangeBg, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Fastfood, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = title,
                        color = DarkGray,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Folio #${pedido.id.takeLast(6).uppercase()} · ${formatOrderDate(pedido.createdAt)}",
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (pedido.turno > 0) {
                        Surface(color = ExtraLightGray, shape = RoundedCornerShape(8.dp)) {
                            Column(modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)) {
                                Text(
                                    stringResource(Res.string.orders_turn),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MediumGray
                                )
                                Text(
                                    pedido.turno.toString(),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGray
                                )
                            }
                        }
                    }
                    Surface(color = statusColor.copy(alpha = 0.14f), shape = CircleShape) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (pedido.estado == EstadoPedido.ENTREGADO) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = statusColor, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = status,
                                color = statusColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun OrderTotal(total: Double) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = stringResource(Res.string.orders_total),
            color = MediumGray,
            style = MaterialTheme.typography.labelSmall
        )
        val cents = round(total * 100).toLong()
        Text(
            text = "$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}",
            color = DarkGray,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

private fun orderProductsSummary(pedido: Pedido): String =
    pedido.productos.takeIf { it.isNotEmpty() }
        ?.joinToString(", ") { "${it.cantidad}x ${it.nombre}" }
        ?: pedido.descripcion

private fun formatOrderDate(timestamp: Long): String {
    if (timestamp <= 0L) return ""
    val dateTime = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "$day/$month/${dateTime.year} · $hour:$minute"
}

