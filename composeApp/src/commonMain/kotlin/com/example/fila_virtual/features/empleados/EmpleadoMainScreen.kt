package com.example.fila_virtual.features.empleados

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.BottomNavigationBar
import com.example.fila_virtual.components.NavigationDefaults
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightBackground
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.data.EstacionOperativa
import com.example.fila_virtual.data.ResumenTurno
import com.example.fila_virtual.data.RolesEmpleado
import com.example.fila_virtual.features.empleados.comun.CodigoInvitacionCard
import com.example.fila_virtual.features.empleados.comun.EmpleadoTurnoViewModel
import com.example.fila_virtual.features.empleados.estaciones.cocina.CocinaEstacion
import com.example.fila_virtual.features.empleados.estaciones.cajero.CajeroEstacion
import com.example.fila_virtual.features.empleados.estaciones.entrega.EntregaEstacion
import com.example.fila_virtual.features.empleados.gerencia.AgotadoTemporalScreen
import com.example.fila_virtual.features.empleados.gerencia.GerenteMenuScreen
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.perfil.EditProfileScreen
import com.example.fila_virtual.perfil.EnProcesoScreen
import com.example.fila_virtual.perfil.ProfileComponent
import com.example.fila_virtual.core.LegalConstants
import com.example.fila_virtual.core.navigation.LegalScreen
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_shift_branch
import fila_virtual.composeapp.generated.resources.emp_no_branch
import fila_virtual.composeapp.generated.resources.emp_station_cajero
import fila_virtual.composeapp.generated.resources.emp_station_cocina
import fila_virtual.composeapp.generated.resources.emp_station_entrega
import fila_virtual.composeapp.generated.resources.emp_stock_open
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmpleadoMainScreen(
    viewModel: UserViewModel,
    onLogout: () -> Unit,
    turnoViewModel: EmpleadoTurnoViewModel = viewModel()
) {
    val uid = viewModel.usuario?.uid.orEmpty()
    var estacionName by rememberSaveable { mutableStateOf<String?>(null) }
    var turnoGuardado by rememberSaveable { mutableStateOf(false) }
    var resumen by remember { mutableStateOf<ResumenTurno?>(null) }
    var verAgotados by rememberSaveable { mutableStateOf(false) }
    var verGerente by rememberSaveable { mutableStateOf(false) }
    var verPerfil by rememberSaveable { mutableStateOf(false) }
    var editarPerfil by rememberSaveable { mutableStateOf(false) }
    var mostrarSeguridad by rememberSaveable { mutableStateOf(false) }
    var mostrarAyuda by rememberSaveable { mutableStateOf(false) }
    var mostrarTerminos by rememberSaveable { mutableStateOf(false) }
    var indiceSeleccionado by rememberSaveable { mutableIntStateOf(0) }
    var indiceAnteriorAgotados by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) turnoViewModel.cargar(uid)
    }
    LaunchedEffect(turnoViewModel.estadisticasSesion?.estacion, turnoViewModel.estadisticasSesion?.inicioAt) {
        val estacionActiva = turnoViewModel.estadisticasSesion?.estacion
            ?.let { clave -> EstacionOperativa.values().firstOrNull { it.clave() == clave } }
        if (estacionActiva != null) {
            turnoGuardado = true
            estacionName = estacionActiva.name
        }
    }
    val establecimientoId = turnoViewModel.asignacion?.establecimientoId.orEmpty()
    val esGerente = RolesEmpleado.esGerente(turnoViewModel.asignacion?.roles.orEmpty())
    val puedeGestionarAgotados = RolesEmpleado.puedeGestionarAgotados(turnoViewModel.asignacion?.roles.orEmpty()) &&
        establecimientoId.isNotBlank()
    val sinAsignacion = !turnoViewModel.cargando && turnoViewModel.asignacion == null
    val puedeGestionar = esGerente && establecimientoId.isNotBlank()
    val estaciones = turnoViewModel.estaciones.sortedBy { it.ordinal }
    val elementosNav = NavigationDefaults.employeeItems(estaciones, puedeGestionar, puedeGestionarAgotados)
    val indiceGerencia = estaciones.size + 1
    val indiceAgotados = indiceGerencia + if (puedeGestionar) 1 else 0
    val estacion = estacionName?.let { name ->
        runCatching { EstacionOperativa.valueOf(name) }.getOrNull()
    }
    val contentKey = when {
        verGerente && puedeGestionar -> "manager"
        verPerfil -> "profile"
        estacion != null -> "station:${estacion.name}"
        else -> "home"
    }
    val agotadosSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cerrarAgotados = {
        verAgotados = false
        indiceSeleccionado = indiceAnteriorAgotados
    }
    val terminarTurno = {
        turnoViewModel.terminar { cerrado ->
            turnoGuardado = false
            estacionName = null
            indiceSeleccionado = 0
            resumen = cerrado
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
    ) {
        if (editarPerfil) {
            EditProfileScreen(usuario = viewModel.usuario, viewModel = viewModel, onBack = { editarPerfil = false })
        } else if (mostrarSeguridad) {
            EnProcesoScreen(titulo = "Configuración de Seguridad", onBack = { mostrarSeguridad = false })
        } else if (mostrarAyuda) {
            EnProcesoScreen(titulo = "Centro de Ayuda", onBack = { mostrarAyuda = false })
        } else if (mostrarTerminos) {
            LegalScreen(title = "Términos y Condiciones", content = LegalConstants.TERMINOS_Y_CONDICIONES, onBack = { mostrarTerminos = false })
        } else {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    BottomNavigationBar(
                        items = elementosNav,
                        selectedIndex = indiceSeleccionado,
                        onItemSelected = { index ->
                            if (index in 1..estaciones.size && turnoViewModel.guardando) return@BottomNavigationBar
                            val indiceAnterior = indiceSeleccionado
                            indiceSeleccionado = index
                            verPerfil = false
                            verGerente = false
                            verAgotados = false
                            when {
                                index == 0 -> estacionName = null
                                index in 1..estaciones.size -> {
                                    estacionName = null
                                    turnoViewModel.iniciar(uid, estaciones[index - 1]) { guardada ->
                                        turnoGuardado = guardada
                                        estacionName = estaciones[index - 1].name
                                    }
                                }
                                puedeGestionar && index == indiceGerencia -> {
                                    estacionName = null
                                    verGerente = true
                                }
                                puedeGestionarAgotados && index == indiceAgotados -> {
                                    indiceAnteriorAgotados = indiceAnterior
                                    verAgotados = true
                                }
                                else -> verPerfil = true
                            }
                        }
                    )
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    Crossfade(targetState = contentKey, label = "employee_screen_transition") { screen ->
                        when {
                            screen == "manager" -> GerenteMenuScreen(
                                establecimientoId = establecimientoId,
                                establecimientoNombre = turnoViewModel.asignacion?.establecimientoNombre.orEmpty(),
                                onBack = { verGerente = false; indiceSeleccionado = 0 }
                            )
                            screen == "profile" -> ProfileComponent(
                                usuario = viewModel.usuario,
                                viewModel = viewModel,
                                onLogout = { viewModel.signOut(onLogout) },
                                onNavigateToEdit = { editarPerfil = true },
                                onNavigateToSecurity = { mostrarSeguridad = true },
                                onNavigateToHelp = { mostrarAyuda = true },
                                onNavigateToTerms = { mostrarTerminos = true }
                            )
                            screen.startsWith("station:") -> EstacionOperativaScreen(
                                estacion = EstacionOperativa.valueOf(screen.substringAfter(':')),
                                turnoGuardado = turnoGuardado,
                                establecimientoId = establecimientoId,
                                empleadoUid = uid,
                                sesionId = turnoViewModel.sesion?.id.orEmpty(),
                                puedeGestionarAgotados = puedeGestionarAgotados,
                                onAgotados = {
                                    indiceAnteriorAgotados = indiceSeleccionado
                                    verAgotados = true
                                }
                            )
                            else -> EmpleadoHome(
                                nombre = viewModel.usuario?.nombre?.ifBlank { viewModel.usuario?.email }.orEmpty(),
                                establecimiento = turnoViewModel.asignacion?.establecimientoNombre.orEmpty(),
                                correo = viewModel.usuario?.email.orEmpty(),
                                sinAsignacion = sinAsignacion,
                                cargando = turnoViewModel.cargando,
                                guardando = turnoViewModel.guardando,
                                error = turnoViewModel.error,
                                soloGerente = esGerente && estaciones.isEmpty() && !sinAsignacion,
                                estaciones = estaciones,
                                estacionTurno = turnoViewModel.estadisticasSesion?.estacion
                                    ?.let { clave -> EstacionOperativa.values().firstOrNull { it.clave() == clave } },
                                turnoEnCurso = turnoGuardado,
                                estadisticasSesion = turnoViewModel.estadisticasSesion,
                                resumenUltimoTurno = resumen,
                                onIniciar = { estacionElegida ->
                                    turnoViewModel.iniciar(uid, estacionElegida) { guardada ->
                                        turnoGuardado = guardada
                                        if (guardada) estacionName = estacionElegida.name
                                    }
                                },
                                onTerminar = terminarTurno,
                                onInvitacionAceptada = {
                                    viewModel.loadUserData()
                                    turnoViewModel.cargar(uid)
                                },
                                onRefresh = {
                                    viewModel.loadUserData()
                                    turnoViewModel.cargar(uid)
                                }
                            )
                        }
                    }
                }
            }
        }
        if (verAgotados && puedeGestionarAgotados) {
            ModalBottomSheet(
                onDismissRequest = cerrarAgotados,
                sheetState = agotadosSheetState,
                containerColor = LightBackground
            ) {
                AgotadoTemporalScreen(
                    establecimientoId = establecimientoId,
                    onBack = cerrarAgotados
                )
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
    puedeGestionarAgotados: Boolean,
    onAgotados: () -> Unit
) {
    val title = when (estacion) {
        EstacionOperativa.COCINA -> stringResource(Res.string.emp_station_cocina)
        EstacionOperativa.ENTREGA -> stringResource(Res.string.emp_station_entrega)
        EstacionOperativa.CAJERO -> stringResource(Res.string.emp_station_cajero)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
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
        if (puedeGestionarAgotados) {
            OutlinedButton(
                onClick = onAgotados,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange)
            ) {
                Icon(Icons.Default.RemoveShoppingCart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.emp_stock_open), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

