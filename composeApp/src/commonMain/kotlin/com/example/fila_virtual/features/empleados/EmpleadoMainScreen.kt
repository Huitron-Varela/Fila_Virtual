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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material.icons.filled.Storefront
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
import com.example.fila_virtual.core.theme.SoftRedBg
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.EstacionOperativa
import com.example.fila_virtual.data.ResumenTurno
import com.example.fila_virtual.data.RolesEmpleado
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.repository.EmpleadoRepository
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_back
import fila_virtual.composeapp.generated.resources.emp_shift_branch
import fila_virtual.composeapp.generated.resources.emp_end_shift
import fila_virtual.composeapp.generated.resources.emp_no_branch
import fila_virtual.composeapp.generated.resources.emp_shift_active
import fila_virtual.composeapp.generated.resources.emp_shift_unsaved
import fila_virtual.composeapp.generated.resources.emp_stock_help
import fila_virtual.composeapp.generated.resources.emp_stock_open
import fila_virtual.composeapp.generated.resources.emp_home_badge
import fila_virtual.composeapp.generated.resources.emp_role_gerente
import fila_virtual.composeapp.generated.resources.emp_home_active_hint
import fila_virtual.composeapp.generated.resources.emp_gerente_desc
import fila_virtual.composeapp.generated.resources.emp_gerente_home_hint
import fila_virtual.composeapp.generated.resources.emp_gerente_home_title
import fila_virtual.composeapp.generated.resources.emp_gerente_open
import fila_virtual.composeapp.generated.resources.emp_home_hello
import fila_virtual.composeapp.generated.resources.emp_home_subtitle
import fila_virtual.composeapp.generated.resources.emp_home_title
import fila_virtual.composeapp.generated.resources.profile_logout
import fila_virtual.composeapp.generated.resources.emp_station_cajero
import fila_virtual.composeapp.generated.resources.emp_station_cajero_desc
import fila_virtual.composeapp.generated.resources.emp_station_cocina
import fila_virtual.composeapp.generated.resources.emp_station_cocina_desc
import fila_virtual.composeapp.generated.resources.emp_station_entrega
import fila_virtual.composeapp.generated.resources.emp_station_entrega_desc
import fila_virtual.composeapp.generated.resources.emp_summary_abandoned
import fila_virtual.composeapp.generated.resources.emp_summary_delivered
import fila_virtual.composeapp.generated.resources.emp_summary_minutes
import fila_virtual.composeapp.generated.resources.emp_summary_history
import fila_virtual.composeapp.generated.resources.emp_summary_note
import fila_virtual.composeapp.generated.resources.emp_summary_shift_cashier
import fila_virtual.composeapp.generated.resources.emp_summary_shift_delivery
import fila_virtual.composeapp.generated.resources.emp_summary_shift_kitchen
import fila_virtual.composeapp.generated.resources.emp_summary_ok
import fila_virtual.composeapp.generated.resources.emp_summary_prepared
import fila_virtual.composeapp.generated.resources.emp_summary_roles
import fila_virtual.composeapp.generated.resources.emp_summary_saved
import fila_virtual.composeapp.generated.resources.emp_summary_title
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
    var verAgotados by rememberSaveable { mutableStateOf(false) }
    var verGerente by rememberSaveable { mutableStateOf(false) }
    val establecimientoId = turnoViewModel.asignacion?.establecimientoId.orEmpty()
    val esGerente = RolesEmpleado.esGerente(turnoViewModel.asignacion?.roles.orEmpty())
    val estacion = estacionName?.let { name ->
        runCatching { EstacionOperativa.valueOf(name) }.getOrNull()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
    if (verGerente && esGerente) {
        GerenteMenuScreen(
            establecimientoId = establecimientoId,
            establecimientoNombre = turnoViewModel.asignacion?.establecimientoNombre.orEmpty(),
            onBack = { verGerente = false }
        )
    } else if (verAgotados) {
        AgotadoTemporalScreen(
            establecimientoId = establecimientoId,
            onBack = { verAgotados = false }
        )
    } else if (estacion == null) {
        EmpleadoHome(
            nombre = viewModel.usuario?.nombre?.ifBlank { viewModel.usuario?.email }.orEmpty(),
            establecimiento = turnoViewModel.asignacion?.establecimientoNombre.orEmpty(),
            correo = viewModel.usuario?.email.orEmpty(),
            sinAsignacion = !turnoViewModel.cargando && turnoViewModel.asignacion == null,
            cargando = turnoViewModel.cargando,
            guardando = turnoViewModel.guardando,
            error = turnoViewModel.error,
            estaciones = turnoViewModel.estaciones,
            esGerente = esGerente,
            turnoEnCurso = turnoGuardado,
            onTerminar = {
                turnoViewModel.terminar { cerrado ->
                    turnoGuardado = false
                    estacionName = null
                    resumen = cerrado
                }
            },
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
            onLogout = { viewModel.signOut(onLogout) },
            onAgotados = if (establecimientoId.isNotBlank()) {
                { verAgotados = true }
            } else {
                null
            },
            onGerente = if (esGerente && establecimientoId.isNotBlank()) {
                { verGerente = true }
            } else {
                null
            }
        )
    } else {
        EstacionOperativaScreen(
            estacion = estacion,
            turnoGuardado = turnoGuardado,
            establecimientoId = turnoViewModel.asignacion?.establecimientoId.orEmpty(),
            empleadoUid = uid,
            sesionId = turnoViewModel.sesion?.id.orEmpty(),
            onBack = { estacionName = null },
            onAgotados = { verAgotados = true }
        )
    }
    }

    val resumenActual = resumen
    if (resumenActual != null) {
        val nombre = viewModel.usuario?.nombre?.ifBlank { viewModel.usuario?.email }.orEmpty()
        val puestos = turnoViewModel.estaciones
        val nombresPuestos = puestos.map { puesto ->
            when (puesto) {
                EstacionOperativa.COCINA -> stringResource(Res.string.emp_station_cocina)
                EstacionOperativa.ENTREGA -> stringResource(Res.string.emp_station_entrega)
                EstacionOperativa.CAJERO -> stringResource(Res.string.emp_station_cajero)
            }
        }
        val muestraPreparados = puestos.any { it == EstacionOperativa.COCINA }
        val muestraEntrega = puestos.any { it == EstacionOperativa.ENTREGA }
        AlertDialog(
            onDismissRequest = { resumen = null },
            title = { Text(stringResource(Res.string.emp_summary_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(nombre.ifBlank { "—" }, color = DarkGray, fontWeight = FontWeight.Bold)
                    if (nombresPuestos.isNotEmpty()) {
                        Text(stringResource(Res.string.emp_summary_roles, nombresPuestos.listaNatural()))
                    }
                    if (muestraPreparados) {
                        Text(stringResource(Res.string.emp_summary_prepared, resumenActual.diaPreparados))
                    }
                    if (muestraEntrega) {
                        Text(stringResource(Res.string.emp_summary_delivered, resumenActual.diaEntregados))
                        Text(stringResource(Res.string.emp_summary_abandoned, resumenActual.diaAbandonados))
                        Text(stringResource(Res.string.emp_summary_note), color = MediumGray)
                    }
                    if (resumenActual.turnosDia.isNotEmpty()) {
                        Text(stringResource(Res.string.emp_summary_history), fontWeight = FontWeight.SemiBold)
                        resumenActual.turnosDia.forEach { turno ->
                            Text(textoTurnoDia(turno.estacion, turno.preparados, turno.entregados, turno.abandonados))
                        }
                    }
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
private fun textoTurnoDia(
    estacion: String,
    preparados: Int,
    entregados: Int,
    abandonados: Int
): String {
    return when (estacion) {
        EstacionOperativa.COCINA.clave() -> stringResource(Res.string.emp_summary_shift_kitchen, preparados)
        EstacionOperativa.ENTREGA.clave() -> stringResource(Res.string.emp_summary_shift_delivery, entregados, abandonados)
        else -> stringResource(Res.string.emp_summary_shift_cashier)
    }
}

private fun nombreYApellido(valor: String): String {
    return valor.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.joinToString(" ") { parte ->
        parte.lowercase().replaceFirstChar { letra ->
            if (letra.isLowerCase()) letra.titlecase() else letra.toString()
        }
    }
}

private fun List<String>.listaNatural(): String {
    return when (size) {
        0 -> ""
        1 -> first()
        2 -> "${this[0]} y ${this[1]}"
        else -> dropLast(1).joinToString(", ") + " y " + last()
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
    esGerente: Boolean,
    onEstacion: (EstacionOperativa) -> Unit,
    turnoEnCurso: Boolean,
    onTerminar: () -> Unit,
    onInvitacionAceptada: () -> Unit,
    onLogout: () -> Unit,
    onAgotados: (() -> Unit)? = null,
    onGerente: (() -> Unit)? = null
) {
    val soloGerente = esGerente && estaciones.isEmpty() && !sinAsignacion
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.emp_home_hello, nombreYApellido(nombre).ifBlank { "—" }),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(
                            if (soloGerente) Res.string.emp_role_gerente else Res.string.emp_home_badge
                        ),
                        color = PrimaryOrange,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SoftOrangeBg)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SoftRedBg)
                        .clickable(onClick = onLogout),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = stringResource(Res.string.profile_logout),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = when {
                    turnoEnCurso -> stringResource(Res.string.emp_shift_active)
                    soloGerente && establecimiento.isNotBlank() -> establecimiento
                    soloGerente -> stringResource(Res.string.emp_gerente_home_title)
                    else -> stringResource(Res.string.emp_home_title)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (turnoEnCurso) PrimaryOrange else DarkGray
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(
                    when {
                        sinAsignacion -> Res.string.emp_no_branch
                        turnoEnCurso -> Res.string.emp_home_active_hint
                        soloGerente -> Res.string.emp_gerente_home_hint
                        else -> Res.string.emp_home_subtitle
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGray
            )
            if (establecimiento.isNotBlank() && !soloGerente) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.emp_shift_branch, establecimiento),
                    color = DarkGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else if (cargando) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(color = PrimaryOrange, modifier = Modifier.size(28.dp))
            }
            if (sinAsignacion) {
                var puedeUnirse by remember { mutableStateOf(false) }
                LaunchedEffect(correo) {
                    puedeUnirse = correo.isNotBlank() &&
                        EmpleadoRepository().tieneInvitacionPendiente(correo)
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
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val ordenadas = estaciones.sortedBy { it.ordinal }
            items(ordenadas.size) { index ->
                val estacion = ordenadas[index]
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
            if (onGerente != null) {
                item {
                    EstacionCard(
                        icon = Icons.Filled.Storefront,
                        title = stringResource(Res.string.emp_gerente_open),
                        description = stringResource(Res.string.emp_gerente_desc),
                        enabled = !guardando,
                        onClick = onGerente
                    )
                }
            }
            if (onAgotados != null) {
                item {
                    EstacionCard(
                        icon = Icons.Filled.RemoveShoppingCart,
                        title = stringResource(Res.string.emp_stock_open),
                        description = stringResource(Res.string.emp_stock_help),
                        enabled = !guardando,
                        onClick = onAgotados
                    )
                }
            }
        }

        if (turnoEnCurso) {
            TextButton(
                onClick = onTerminar,
                enabled = !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(stringResource(Res.string.emp_end_shift), color = TrafficRed, fontWeight = FontWeight.SemiBold)
            }
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
    establecimientoId: String,
    empleadoUid: String,
    sesionId: String,
    onBack: () -> Unit,
    onAgotados: () -> Unit
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
                .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
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
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onAgotados) {
                Text(stringResource(Res.string.emp_stock_open), color = PrimaryOrange)
            }
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
            Spacer(Modifier.height(16.dp))

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
    }
}

