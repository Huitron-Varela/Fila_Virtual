package com.example.fila_virtual.features.empleados

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightBackground
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.SoftOrangeBg
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.EstacionOperativa
import com.example.fila_virtual.data.ResumenTurno
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.repository.EmpleadoRepository
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_back
import fila_virtual.composeapp.generated.resources.emp_shift_branch
import fila_virtual.composeapp.generated.resources.emp_end_shift
import fila_virtual.composeapp.generated.resources.emp_no_branch
import fila_virtual.composeapp.generated.resources.emp_shift_active
import fila_virtual.composeapp.generated.resources.emp_shift_unsaved
import fila_virtual.composeapp.generated.resources.emp_home_badge
import fila_virtual.composeapp.generated.resources.emp_home_hello
import fila_virtual.composeapp.generated.resources.emp_home_subtitle
import fila_virtual.composeapp.generated.resources.emp_home_title
import fila_virtual.composeapp.generated.resources.emp_logout
import fila_virtual.composeapp.generated.resources.emp_station_cajero
import fila_virtual.composeapp.generated.resources.emp_station_cajero_desc
import fila_virtual.composeapp.generated.resources.emp_station_cocina
import fila_virtual.composeapp.generated.resources.emp_station_cocina_desc
import fila_virtual.composeapp.generated.resources.emp_station_entrega
import fila_virtual.composeapp.generated.resources.emp_station_entrega_desc
import fila_virtual.composeapp.generated.resources.emp_summary_delivered
import fila_virtual.composeapp.generated.resources.emp_summary_minutes
import fila_virtual.composeapp.generated.resources.emp_summary_note
import fila_virtual.composeapp.generated.resources.emp_summary_ok
import fila_virtual.composeapp.generated.resources.emp_summary_prepared
import fila_virtual.composeapp.generated.resources.emp_summary_saved
import fila_virtual.composeapp.generated.resources.emp_summary_title
import fila_virtual.composeapp.generated.resources.emp_summary_under_minute
import fila_virtual.composeapp.generated.resources.emp_summary_unsaved
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmpleadoMainScreen(
    viewModel: UserViewModel,
    onLogout: () -> Unit,
    turnoViewModel: EmpleadoTurnoViewModel = viewModel()
) {
    val uid = viewModel.usuario?.uid.orEmpty()
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) turnoViewModel.cargar(uid)
    }

    var estacionName by rememberSaveable { mutableStateOf<String?>(null) }
    var turnoGuardado by rememberSaveable { mutableStateOf(false) }
    var resumen by remember { mutableStateOf<ResumenTurno?>(null) }
    val estacion = estacionName?.let { name ->
        runCatching { EstacionOperativa.valueOf(name) }.getOrNull()
    }

    if (estacion == null) {
        EmpleadoHome(
            nombre = viewModel.usuario?.nombre?.ifBlank { viewModel.usuario?.email }.orEmpty(),
            establecimiento = turnoViewModel.asignacion?.establecimientoNombre.orEmpty(),
            correo = viewModel.usuario?.email.orEmpty(),
            sinAsignacion = !turnoViewModel.cargando && turnoViewModel.asignacion == null,
            cargando = turnoViewModel.cargando,
            guardando = turnoViewModel.guardando,
            error = turnoViewModel.error,
            estaciones = turnoViewModel.estaciones,
            onEstacion = { elegida ->
                turnoViewModel.iniciar(uid, elegida) { guardada ->
                    turnoGuardado = guardada
                    estacionName = elegida.name
                }
            },
            onInvitacionAceptada = {
                viewModel.loadUserData()
                turnoViewModel.cargar(uid)
            },
            onLogout = { viewModel.signOut(onLogout) }
        )
    } else {
        EstacionOperativaScreen(
            estacion = estacion,
            turnoGuardado = turnoGuardado,
            guardando = turnoViewModel.guardando,
            establecimientoId = turnoViewModel.asignacion?.establecimientoId.orEmpty(),
            empleadoUid = uid,
            sesionId = turnoViewModel.sesion?.id.orEmpty(),
            onBack = { estacionName = null },
            onTerminar = {
                turnoViewModel.terminar { cerrado ->
                    turnoGuardado = false
                    estacionName = null
                    resumen = cerrado
                }
            }
        )
    }

    val resumenActual = resumen
    if (resumenActual != null) {
        val segundos = ((resumenActual.finAt - resumenActual.inicioAt) / 1000L).coerceAtLeast(0L).toInt()
        val duracion = if (segundos < 60) {
            stringResource(Res.string.emp_summary_under_minute)
        } else {
            stringResource(Res.string.emp_summary_minutes, segundos / 60)
        }
        val estacionTexto = when (resumenActual.estacion) {
            EstacionOperativa.COCINA.clave() -> stringResource(Res.string.emp_station_cocina)
            EstacionOperativa.ENTREGA.clave() -> stringResource(Res.string.emp_station_entrega)
            EstacionOperativa.CAJERO.clave() -> stringResource(Res.string.emp_station_cajero)
            else -> resumenActual.estacion
        }
        AlertDialog(
            onDismissRequest = { resumen = null },
            title = { Text(stringResource(Res.string.emp_summary_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(estacionTexto, color = DarkGray, fontWeight = FontWeight.SemiBold)
                    Text(duracion)
                    Text(stringResource(Res.string.emp_summary_prepared, resumenActual.preparados))
                    Text(stringResource(Res.string.emp_summary_delivered, resumenActual.entregados))
                    Text(stringResource(Res.string.emp_summary_note), color = MediumGray)
                    Text(
                        stringResource(
                            if (resumenActual.registroGuardado) Res.string.emp_summary_saved
                            else Res.string.emp_summary_unsaved
                        ),
                        color = if (resumenActual.registroGuardado) DarkGray else TrafficRed
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { resumen = null }) {
                    Text(stringResource(Res.string.emp_summary_ok), color = PrimaryOrange)
                }
            }
        )
    }
}

@Composable
private fun EmpleadoHome(
    nombre: String,
    correo: String,
    establecimiento: String,
    sinAsignacion: Boolean,
    cargando: Boolean,
    guardando: Boolean,
    error: String,
    estaciones: List<EstacionOperativa>,
    onEstacion: (EstacionOperativa) -> Unit,
    onInvitacionAceptada: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(
                text = stringResource(Res.string.emp_home_hello, nombre.ifBlank { "—" }),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.emp_home_badge),
                color = PrimaryOrange,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SoftOrangeBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text = stringResource(Res.string.emp_home_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.emp_home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGray
            )
            Spacer(Modifier.height(12.dp))
            when {
                cargando -> CircularProgressIndicator(color = PrimaryOrange, modifier = Modifier.size(28.dp))
                establecimiento.isNotBlank() -> Text(
                    text = stringResource(Res.string.emp_shift_branch, establecimiento),
                    color = DarkGray,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                sinAsignacion -> {
                    var puedeUnirse by remember { mutableStateOf(false) }
                    LaunchedEffect(correo) {
                        puedeUnirse = correo.isNotBlank() &&
                            EmpleadoRepository().tieneInvitacionPendiente(correo)
                    }
                    Column {
                        Text(
                            text = stringResource(Res.string.emp_no_branch),
                            color = MediumGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (puedeUnirse) {
                            Spacer(Modifier.height(12.dp))
                            CodigoInvitacionCard(onAceptada = {
                                puedeUnirse = false
                                onInvitacionAceptada()
                            })
                        }
                    }
                }
            }
            if (error.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = TrafficRed, style = MaterialTheme.typography.bodySmall)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(estaciones.size) { index ->
                val estacion = estaciones[index]
                val (icono, titulo, descripcion) = when (estacion) {
                    EstacionOperativa.COCINA -> Triple(
                        Icons.Filled.Restaurant,
                        stringResource(Res.string.emp_station_cocina),
                        stringResource(Res.string.emp_station_cocina_desc)
                    )
                    EstacionOperativa.ENTREGA -> Triple(
                        Icons.Filled.QrCodeScanner,
                        stringResource(Res.string.emp_station_entrega),
                        stringResource(Res.string.emp_station_entrega_desc)
                    )
                    EstacionOperativa.CAJERO -> Triple(
                        Icons.Filled.PointOfSale,
                        stringResource(Res.string.emp_station_cajero),
                        stringResource(Res.string.emp_station_cajero_desc)
                    )
                }
                EstacionCard(
                    icon = icono,
                    title = titulo,
                    description = descripcion,
                    enabled = !guardando,
                    onClick = { onEstacion(estacion) }
                )
            }
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = PrimaryOrange)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.emp_logout), color = PrimaryOrange, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun EstacionCard(
    icon: ImageVector,
    title: String,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SoftOrangeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = DarkGray, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(description, color = MediumGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun EstacionOperativaScreen(
    estacion: EstacionOperativa,
    turnoGuardado: Boolean,
    guardando: Boolean,
    establecimientoId: String,
    empleadoUid: String,
    sesionId: String,
    onBack: () -> Unit,
    onTerminar: () -> Unit
) {
    val title = when (estacion) {
        EstacionOperativa.COCINA -> stringResource(Res.string.emp_station_cocina)
        EstacionOperativa.ENTREGA -> stringResource(Res.string.emp_station_entrega)
        EstacionOperativa.CAJERO -> stringResource(Res.string.emp_station_cajero)
    }
    val description = when (estacion) {
        EstacionOperativa.COCINA -> stringResource(Res.string.emp_station_cocina_desc)
        EstacionOperativa.ENTREGA -> stringResource(Res.string.emp_station_entrega_desc)
        EstacionOperativa.CAJERO -> stringResource(Res.string.emp_station_cajero_desc)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.emp_back),
                tint = DarkGray,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onBack)
                    .padding(12.dp)
            )
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkGray)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            Text(description, color = MediumGray, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(if (turnoGuardado) Res.string.emp_shift_active else Res.string.emp_shift_unsaved),
                color = if (turnoGuardado) PrimaryOrange else MediumGray,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(20.dp))

            if (estacion == EstacionOperativa.COCINA) {
                CocinaEstacion(
                    establecimientoId = establecimientoId,
                    empleadoUid = empleadoUid,
                    sesionId = sesionId,
                    turnoGuardado = turnoGuardado,
                    modifier = Modifier.weight(1f)
                )
            } else if (estacion == EstacionOperativa.ENTREGA) {
                EntregaEstacion(
                    establecimientoId = establecimientoId,
                    empleadoUid = empleadoUid,
                    sesionId = sesionId,
                    turnoGuardado = turnoGuardado,
                    modifier = Modifier.weight(1f)
                )
            } else {
                CajeroEstacion(
                    establecimientoId = establecimientoId,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = onTerminar,
            enabled = !guardando,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
        ) {
            Text(stringResource(Res.string.emp_end_shift), fontWeight = FontWeight.SemiBold)
        }
    }
}

