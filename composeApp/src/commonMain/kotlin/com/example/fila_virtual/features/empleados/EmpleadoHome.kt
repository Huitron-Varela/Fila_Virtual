package com.example.fila_virtual.features.empleados

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
import com.example.fila_virtual.core.theme.BorderGray
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightBackground
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.SoftOrangeBg
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.EstacionOperativa
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.ResumenTurno
import com.example.fila_virtual.features.empleados.comun.CodigoInvitacionCard
import com.example.fila_virtual.repository.EmpleadoRepository
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_delivery_ticket
import fila_virtual.composeapp.generated.resources.emp_home_active_hint
import fila_virtual.composeapp.generated.resources.emp_home_badge
import fila_virtual.composeapp.generated.resources.emp_home_delivered_empty
import fila_virtual.composeapp.generated.resources.emp_home_establishment
import fila_virtual.composeapp.generated.resources.emp_home_hello
import fila_virtual.composeapp.generated.resources.emp_home_recent_delivered
import fila_virtual.composeapp.generated.resources.emp_home_station
import fila_virtual.composeapp.generated.resources.emp_home_subtitle
import fila_virtual.composeapp.generated.resources.emp_home_title
import fila_virtual.composeapp.generated.resources.emp_gerente_home_hint
import fila_virtual.composeapp.generated.resources.emp_gerente_home_title
import fila_virtual.composeapp.generated.resources.emp_home_last_shift
import fila_virtual.composeapp.generated.resources.emp_home_stat_delivered
import fila_virtual.composeapp.generated.resources.emp_home_stat_incidents
import fila_virtual.composeapp.generated.resources.emp_home_stat_prepared
import fila_virtual.composeapp.generated.resources.emp_no_branch
import fila_virtual.composeapp.generated.resources.emp_role_gerente
import fila_virtual.composeapp.generated.resources.emp_shift_active
import fila_virtual.composeapp.generated.resources.emp_shift_end_slide
import fila_virtual.composeapp.generated.resources.emp_shift_select_station
import fila_virtual.composeapp.generated.resources.emp_shift_start_slide
import fila_virtual.composeapp.generated.resources.emp_shift_statistics_empty
import fila_virtual.composeapp.generated.resources.emp_summary_title
import fila_virtual.composeapp.generated.resources.emp_station_cajero
import fila_virtual.composeapp.generated.resources.emp_station_entrega
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun EmpleadoHome(
    nombre: String,
    correo: String,
    establecimiento: String,
    sinAsignacion: Boolean,
    cargando: Boolean,
    guardando: Boolean,
    error: String,
    soloGerente: Boolean,
    estaciones: List<EstacionOperativa>,
    pedidosEntregadosRecientes: List<Pedido>,
    estacionTurno: EstacionOperativa?,
    turnoEnCurso: Boolean,
    estadisticasSesion: ResumenTurno?,
    resumenUltimoTurno: ResumenTurno?,
    onIniciar: (EstacionOperativa) -> Unit,
    onTerminar: () -> Unit,
    onInvitacionAceptada: () -> Unit,
    onRefresh: () -> Unit
) {
    var estacionSeleccionadaName by rememberSaveable {
        mutableStateOf(estacionTurno?.name ?: estaciones.firstOrNull()?.name.orEmpty())
    }
    LaunchedEffect(estaciones, estacionTurno, turnoEnCurso) {
        if (turnoEnCurso && estacionTurno != null) {
            estacionSeleccionadaName = estacionTurno.name
        } else if (estaciones.none { it.name == estacionSeleccionadaName }) {
            estacionSeleccionadaName = estaciones.firstOrNull()?.name.orEmpty()
        }
    }

    val estacionSeleccionada = estaciones.firstOrNull { it.name == estacionSeleccionadaName }
    val estadisticasVisibles = estadisticasSesion ?: resumenUltimoTurno
    val haptic = LocalHapticFeedback.current

    AppPullToRefreshBox(
        isRefreshing = cargando,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize().background(LightBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            stringResource(Res.string.emp_home_hello, nombreYApellido(nombre).ifBlank { "—" }),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkGray,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(if (soloGerente) Res.string.emp_role_gerente else Res.string.emp_home_badge),
                            color = PrimaryOrange,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SoftOrangeBg)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = when {
                            turnoEnCurso -> stringResource(Res.string.emp_shift_active)
                            soloGerente -> stringResource(Res.string.emp_gerente_home_title)
                            else -> stringResource(Res.string.emp_home_title)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (turnoEnCurso) PrimaryOrange else DarkGray
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(
                            when {
                                sinAsignacion -> Res.string.emp_no_branch
                                turnoEnCurso -> Res.string.emp_home_active_hint
                                soloGerente -> Res.string.emp_gerente_home_hint
                                else -> Res.string.emp_home_subtitle
                            }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGray
                    )

                    if (establecimiento.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LightSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, BorderGray)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryOrange)
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(stringResource(Res.string.emp_home_establishment), color = MediumGray, style = MaterialTheme.typography.labelSmall)
                                        Text(establecimiento, color = DarkGray, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (!soloGerente && estacionSeleccionada != null) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Restaurant, contentDescription = null, tint = PrimaryOrange)
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text(stringResource(Res.string.emp_home_station), color = MediumGray, style = MaterialTheme.typography.labelSmall)
                                            Text(etiquetaEstacion(estacionSeleccionada), color = DarkGray, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }
                    } else if (cargando) {
                        Spacer(Modifier.height(12.dp))
                        AppListSkeleton(Modifier.fillMaxWidth(), itemCount = 2, imageSize = 44.dp)
                    }

                    if (sinAsignacion) {
                        var puedeUnirse by remember { mutableStateOf(false) }
                        LaunchedEffect(correo) {
                            puedeUnirse = correo.isNotBlank() && EmpleadoRepository().tieneInvitacionPendiente(correo)
                        }
                        if (puedeUnirse) {
                            Spacer(Modifier.height(12.dp))
                            CodigoInvitacionCard(onAceptada = {
                                puedeUnirse = false
                                onInvitacionAceptada()
                            })
                        }
                    }
                    if (error.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(error, color = TrafficRed, style = MaterialTheme.typography.bodySmall)
                    }
                    if (estaciones.size > 1 && !turnoEnCurso) {
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(Res.string.emp_shift_select_station), color = MediumGray, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            estaciones.forEach { estacion ->
                                FilterChip(
                                    selected = estacion.name == estacionSeleccionadaName,
                                    onClick = { estacionSeleccionadaName = estacion.name },
                                    label = { Text(etiquetaEstacion(estacion)) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    EstadisticasTurnoCard(estadisticasVisibles, turnoActivo = estadisticasSesion != null)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(Res.string.emp_home_recent_delivered), color = DarkGray, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (pedidosEntregadosRecientes.isEmpty()) {
                        Text(stringResource(Res.string.emp_home_delivered_empty), color = MediumGray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            pedidosEntregadosRecientes.take(3).forEach { PedidoEntregadoCard(it) }
                        }
                    }
                }
            }

            val estacionParaTurno = estacionSeleccionada
            if (estacionParaTurno != null) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
                    DeslizadorTurno(
                        enCurso = turnoEnCurso,
                        enabled = !guardando,
                        onConfirmar = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (turnoEnCurso) onTerminar() else onIniciar(estacionParaTurno)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PedidoEntregadoCard(pedido: Pedido) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).background(SoftOrangeBg, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("#${pedido.turno}", color = PrimaryOrange, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    pedido.descripcion.ifBlank { pedido.productos.joinToString(", ") { "${it.cantidad}× ${it.nombre}" } },
                    color = DarkGray,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(stringResource(Res.string.emp_delivery_ticket, pedido.id.takeLast(4).uppercase()), color = MediumGray, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryOrange)
        }
    }
}

@Composable
private fun DeslizadorTurno(enCurso: Boolean, enabled: Boolean, onConfirmar: () -> Unit) {
    var anchoPx by remember { mutableFloatStateOf(0f) }
    var desplazamientoPx by remember { mutableFloatStateOf(0f) }
    val densidad = LocalDensity.current
    val anchoControlPx = with(densidad) { 56.dp.toPx() }
    val maxDesplazamiento = (anchoPx - anchoControlPx - with(densidad) { 8.dp.toPx() }).coerceAtLeast(0f)
    val estadoArrastre = rememberDraggableState { delta ->
        desplazamientoPx = (desplazamientoPx + delta).coerceIn(0f, maxDesplazamiento)
    }
    val texto = stringResource(if (enCurso) Res.string.emp_shift_end_slide else Res.string.emp_shift_start_slide)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(CircleShape)
            .background(if (enCurso) Color(0xFFFFE7E2) else Color(0xFFE7E8EA))
            .onSizeChanged { anchoPx = it.width.toFloat() }
            .draggable(
                state = estadoArrastre,
                orientation = Orientation.Horizontal,
                enabled = enabled,
                onDragStopped = {
                    if (maxDesplazamiento > 0f && desplazamientoPx >= maxDesplazamiento * 0.72f) onConfirmar()
                    desplazamientoPx = 0f
                }
            )
            .semantics {
                role = Role.Button
                onClick(label = texto) {
                    if (enabled) {
                        onConfirmar()
                        true
                    } else false
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(texto, color = if (enabled) DarkGray else MediumGray, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 64.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 4.dp)
                .offset { IntOffset(desplazamientoPx.roundToInt(), 0) }
                .size(54.dp)
                .background(if (enCurso) TrafficRed else PrimaryOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(if (enCurso) Icons.Default.LockOpen else Icons.Default.Lock, contentDescription = texto, tint = Color.White)
        }
    }
}

@Composable
private fun EstadisticasTurnoCard(estadisticas: ResumenTurno?, turnoActivo: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGray)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(stringResource(Res.string.emp_summary_title), color = DarkGray, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            if (estadisticas == null) {
                Text(stringResource(Res.string.emp_shift_statistics_empty), color = MediumGray, style = MaterialTheme.typography.bodySmall)
            } else {
                Text(stringResource(if (turnoActivo) Res.string.emp_shift_active else Res.string.emp_home_last_shift), color = PrimaryOrange, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstadisticaTurno(Modifier.weight(1f), stringResource(Res.string.emp_home_stat_delivered), estadisticas.entregados)
                        EstadisticaTurno(Modifier.weight(1f), stringResource(Res.string.emp_home_stat_prepared), estadisticas.preparados)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        EstadisticaTurno(Modifier.weight(1f), stringResource(Res.string.emp_home_stat_incidents), estadisticas.abandonados)
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadisticaTurno(modifier: Modifier, label: String, value: Int) {
    Column(modifier.background(SoftOrangeBg, RoundedCornerShape(12.dp)).padding(12.dp)) {
        Text(value.toString(), color = DarkGray, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, color = MediumGray, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun etiquetaEstacion(estacion: EstacionOperativa): String = when (estacion) {
    EstacionOperativa.COCINA -> stringResource(Res.string.emp_home_title)
    EstacionOperativa.ENTREGA -> stringResource(Res.string.emp_station_entrega)
    EstacionOperativa.CAJERO -> stringResource(Res.string.emp_station_cajero)
}

private fun nombreYApellido(valor: String): String =
    valor.trim().split(Regex("\\s+")).filter(String::isNotEmpty).joinToString(" ") { parte ->
        parte.lowercase().replaceFirstChar { letra -> if (letra.isLowerCase()) letra.titlecase() else letra.toString() }
    }
