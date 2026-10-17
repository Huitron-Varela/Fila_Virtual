package com.example.fila_virtual.features.empleados.estaciones.entrega

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
import com.example.fila_virtual.components.AppSnackbarHost
import com.example.fila_virtual.core.PermissionType
import com.example.fila_virtual.core.VisorCodigoQr
import com.example.fila_virtual.core.rememberPermissionsManager
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.LightGray
import com.example.fila_virtual.core.theme.BorderGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.coincideCodigoRecogida
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.btn_cancel
import fila_virtual.composeapp.generated.resources.emp_delivery_blocked
import fila_virtual.composeapp.generated.resources.emp_scan_denied
import fila_virtual.composeapp.generated.resources.emp_scan_hint
import fila_virtual.composeapp.generated.resources.emp_scan_reading
import fila_virtual.composeapp.generated.resources.emp_scan_retry
import fila_virtual.composeapp.generated.resources.emp_delivery_code
import fila_virtual.composeapp.generated.resources.emp_delivery_confirm
import fila_virtual.composeapp.generated.resources.emp_delivery_manual
import fila_virtual.composeapp.generated.resources.emp_delivery_manual_hint
import fila_virtual.composeapp.generated.resources.emp_delivery_validate
import fila_virtual.composeapp.generated.resources.emp_delivery_history_empty
import fila_virtual.composeapp.generated.resources.emp_delivery_history_title
import fila_virtual.composeapp.generated.resources.emp_delivery_open_orders
import fila_virtual.composeapp.generated.resources.emp_delivery_ready_tab
import fila_virtual.composeapp.generated.resources.emp_delivery_queue_title
import fila_virtual.composeapp.generated.resources.emp_delivery_ready
import fila_virtual.composeapp.generated.resources.emp_delivery_ticket
import fila_virtual.composeapp.generated.resources.emp_delivery_unclaimed
import fila_virtual.composeapp.generated.resources.emp_delivery_unclaimed_body
import fila_virtual.composeapp.generated.resources.emp_delivery_unclaimed_title
import fila_virtual.composeapp.generated.resources.emp_delivery_waiting_pickup
import fila_virtual.composeapp.generated.resources.emp_delivery_delivered_tab
import fila_virtual.composeapp.generated.resources.emp_delivery_code_invalid
import fila_virtual.composeapp.generated.resources.emp_delivery_code_verified
import fila_virtual.composeapp.generated.resources.emp_empty_entrega
import fila_virtual.composeapp.generated.resources.emp_turn_label
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntregaEstacion(
    establecimientoId: String,
    empleadoUid: String,
    sesionId: String,
    turnoGuardado: Boolean,
    modifier: Modifier = Modifier,
    viewModel: EntregaViewModel = viewModel()
) {
    var codigo by remember { mutableStateOf("") }
    var pedidoNoReclamado by remember { mutableStateOf<Pedido?>(null) }
    var mostrarPedidos by remember { mutableStateOf(false) }
    var pestanaEntregados by remember { mutableStateOf(false) }
    var camaraAutorizada by remember { mutableStateOf(false) }
    var permisoSolicitado by remember { mutableStateOf(false) }
    var intentoCamara by remember { mutableIntStateOf(0) }
    var cierresConFeedback by remember { mutableIntStateOf(viewModel.cierreListo) }
    var codigoLeido by remember { mutableStateOf<String?>(null) }
    var codigoEnValidacion by remember { mutableStateOf(false) }
    var codigoValidado by remember { mutableStateOf<Boolean?>(null) }
    var permisoDenegado by remember { mutableStateOf(false) }
    var avisoCamara by remember { mutableStateOf("") }
    val permisos = rememberPermissionsManager()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val pedidosSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val puedeEntregar = turnoGuardado && sesionId.isNotBlank()

    LaunchedEffect(establecimientoId) {
        viewModel.escuchar(establecimientoId)
    }
    LaunchedEffect(puedeEntregar, permisoSolicitado) {
        if (puedeEntregar && !permisoSolicitado) {
            permisoSolicitado = true
            permisos.askPermission(PermissionType.CAMERA) { concedido ->
                permisoDenegado = !concedido
                avisoCamara = ""
                camaraAutorizada = concedido
            }
        }
    }
    LaunchedEffect(viewModel.cierreListo) {
        if (viewModel.cierreListo > 0) {
            if (viewModel.cierreListo > cierresConFeedback) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                cierresConFeedback = viewModel.cierreListo
            }
            if (codigoEnValidacion) {
                codigoEnValidacion = false
                codigoValidado = true
            }
            codigo = ""
            codigoLeido = null
            intentoCamara += 1
        }
    }
    LaunchedEffect(viewModel.error) {
        val mensaje = viewModel.error
        if (mensaje.isNotBlank()) {
            if (codigoEnValidacion) {
                codigoEnValidacion = false
                codigoValidado = false
            }
            snackbarHostState.showSnackbar(mensaje)
            viewModel.limpiarAviso()
        }
    }
    LaunchedEffect(avisoCamara) {
        if (avisoCamara.isNotBlank()) snackbarHostState.showSnackbar(avisoCamara)
    }

    if (pedidoNoReclamado != null) {
        AlertDialog(
            onDismissRequest = { pedidoNoReclamado = null },
            title = { Text(stringResource(Res.string.emp_delivery_unclaimed_title)) },
            text = { Text(stringResource(Res.string.emp_delivery_unclaimed_body)) },
            confirmButton = {
                TextButton(onClick = {
                    val pedido = pedidoNoReclamado
                    pedidoNoReclamado = null
                    if (pedido != null) {
                        viewModel.cerrar(
                            pedido,
                            EstadoPedido.NO_RECLAMADO,
                            establecimientoId,
                            empleadoUid,
                            sesionId
                        )
                    }
                }) {
                    Text(stringResource(Res.string.emp_delivery_unclaimed), color = TrafficRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { pedidoNoReclamado = null }) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            }
        )
    }

    if (mostrarPedidos) {
        ModalBottomSheet(
            onDismissRequest = { mostrarPedidos = false },
            sheetState = pedidosSheetState,
            containerColor = LightSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    stringResource(
                        if (pestanaEntregados) Res.string.emp_delivery_history_title
                        else Res.string.emp_delivery_queue_title
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LightGray, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = { pestanaEntregados = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(9.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (!pestanaEntregados) LightSurface else Color.Transparent,
                            contentColor = if (!pestanaEntregados) PrimaryOrange else MediumGray
                        )
                    ) {
                        Text(stringResource(Res.string.emp_delivery_ready_tab, viewModel.pedidos.size))
                    }
                    TextButton(
                        onClick = { pestanaEntregados = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(9.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (pestanaEntregados) LightSurface else Color.Transparent,
                            contentColor = if (pestanaEntregados) PrimaryOrange else MediumGray
                        )
                    ) {
                        Text(stringResource(Res.string.emp_delivery_delivered_tab, viewModel.entregados.size))
                    }
                }
                Spacer(Modifier.height(12.dp))
                when {
                    !pestanaEntregados && viewModel.cargando && viewModel.pedidos.isEmpty() -> {
                        AppListSkeleton(Modifier.fillMaxWidth().height(280.dp), itemCount = 3)
                    }
                    !pestanaEntregados && viewModel.pedidos.isEmpty() -> {
                        Text(
                            stringResource(Res.string.emp_empty_entrega),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            textAlign = TextAlign.Center,
                            color = MediumGray
                        )
                    }
                    pestanaEntregados && viewModel.entregados.isEmpty() -> {
                        Text(
                            stringResource(Res.string.emp_delivery_history_empty),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            textAlign = TextAlign.Center,
                            color = MediumGray
                        )
                    }
                    pestanaEntregados -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.entregados, key = { it.id }) { pedido ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BorderGray)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            pedido.descripcion.ifBlank {
                                                pedido.productos.joinToString(", ") { "${it.cantidad}× ${it.nombre}" }
                                            },
                                            color = DarkGray,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            stringResource(Res.string.emp_delivery_ticket, pedido.id.takeLast(4).uppercase()),
                                            color = MediumGray,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().heightIn(max = 520.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(viewModel.pedidos, key = { it.id }) { pedido ->
                                val turnoListo = turnoGuardado && sesionId.isNotBlank() && viewModel.pedidoEnCambio == null
                                PedidoEntregaCard(
                                    pedido = pedido,
                                    puedeEntregar = turnoListo && coincideCodigoRecogida(pedido, codigo),
                                    puedeAbandonar = turnoListo,
                                    enCambio = viewModel.pedidoEnCambio == pedido.id,
                                    onEntregar = {
                                        viewModel.cerrar(
                                            pedido,
                                            EstadoPedido.ENTREGADO,
                                            establecimientoId,
                                            empleadoUid,
                                            sesionId
                                        )
                                    },
                                    onNoReclamado = { pedidoNoReclamado = pedido }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    AppPullToRefreshBox(
        isRefreshing = viewModel.cargando,
        onRefresh = { viewModel.escuchar(establecimientoId, forceRefresh = true) },
        modifier = modifier.fillMaxSize()
    ) {
    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black)
        ) {
            if (camaraAutorizada && puedeEntregar && avisoCamara.isBlank() && codigoLeido == null) {
                key(intentoCamara) {
                    VisorCodigoQr(
                        modifier = Modifier.fillMaxSize(),
                        onCodigo = { valor ->
                            val limpio = valor.trim()
                            codigoLeido = limpio
                            codigo = limpio
                            codigoEnValidacion = true
                            codigoValidado = null
                            viewModel.limpiarAviso()
                            viewModel.entregarPorCodigo(
                                limpio,
                                establecimientoId,
                                empleadoUid,
                                sesionId
                            )
                        },
                        onError = { avisoCamara = it }
                    )
                }
                Canvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-16).dp)
                        .size(128.dp)
                ) {
                    val cornerLength = 28.dp.toPx()
                    val strokeWidth = 3.dp.toPx()
                    val markColor = Color.White.copy(alpha = 0.9f)
                    val cornerPath = Path().apply {
                        moveTo(0f, cornerLength)
                        lineTo(0f, 0f)
                        lineTo(cornerLength, 0f)
                        moveTo(size.width - cornerLength, 0f)
                        lineTo(size.width, 0f)
                        lineTo(size.width, cornerLength)
                        moveTo(0f, size.height - cornerLength)
                        lineTo(0f, size.height)
                        lineTo(cornerLength, size.height)
                        moveTo(size.width - cornerLength, size.height)
                        lineTo(size.width, size.height)
                        lineTo(size.width, size.height - cornerLength)
                    }
                    drawPath(
                        path = cornerPath,
                        color = markColor,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.62f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    when {
                        codigoLeido != null && viewModel.pedidoEnCambio != null -> stringResource(Res.string.emp_scan_reading)
                        codigoLeido != null && codigoValidado == false -> stringResource(Res.string.emp_delivery_code_invalid)
                        permisoDenegado -> stringResource(Res.string.emp_scan_denied)
                        !puedeEntregar -> stringResource(Res.string.emp_delivery_blocked)
                        else -> stringResource(Res.string.emp_scan_hint)
                    },
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
                if (codigoLeido != null && codigoValidado == false) {
                    TextButton(onClick = {
                        viewModel.limpiarAviso()
                        codigoEnValidacion = false
                        codigoValidado = null
                        codigo = ""
                        codigoLeido = null
                        intentoCamara += 1
                    }) {
                        Text(stringResource(Res.string.emp_scan_retry), color = Color.White)
                    }
                } else if (avisoCamara.isNotBlank()) {
                    TextButton(onClick = {
                        avisoCamara = ""
                        intentoCamara += 1
                    }) {
                        Text(stringResource(Res.string.emp_scan_retry), color = Color.White)
                    }
                } else if (permisoDenegado) {
                    TextButton(onClick = {
                        permisos.askPermission(PermissionType.CAMERA) { concedido ->
                            permisoDenegado = !concedido
                            avisoCamara = ""
                            camaraAutorizada = concedido
                        }
                    }) {
                        Text(stringResource(Res.string.emp_scan_retry), color = Color.White)
                    }
                }
            }
        }
        if (viewModel.confirmacion.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                    Text(viewModel.confirmacion, color = Color(0xFF1B5E20), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, BorderGray)
        ) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(Res.string.emp_delivery_code),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(color = LightGray, shape = RoundedCornerShape(50)) {
                        Text(
                            stringResource(Res.string.emp_delivery_manual),
                            color = MediumGray,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    BasicTextField(
                        value = codigo,
                        onValueChange = { valor ->
                            codigo = valor.filter { it in '0'..'9' }.take(4)
                            codigoLeido = null
                            codigoEnValidacion = false
                            codigoValidado = null
                            viewModel.limpiarAviso()
                        },
                        modifier = Modifier.widthIn(max = 240.dp).fillMaxWidth().height(52.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.Transparent),
                        cursorBrush = SolidColor(Color.Transparent),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        decorationBox = { innerTextField ->
                            Box {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    repeat(4) { index ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxSize()
                                                .border(
                                                    BorderStroke(
                                                        1.dp,
                                                        when (codigoValidado) {
                                                            true -> Color(0xFF2E7D32)
                                                            false -> TrafficRed
                                                            null -> if (index == codigo.length) PrimaryOrange else BorderGray
                                                        }
                                                    ),
                                                    RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (index < codigo.length) {
                                                Text(
                                                    codigo[index].toString(),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    color = DarkGray
                                                )
                                            }
                                        }
                                    }
                                }
                                innerTextField()
                            }
                        }
                    )
                }
                if (codigoValidado != null) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            if (codigoValidado == true) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = if (codigoValidado == true) Color(0xFF2E7D32) else TrafficRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            stringResource(if (codigoValidado == true) Res.string.emp_delivery_code_verified else Res.string.emp_delivery_code_invalid),
                            color = if (codigoValidado == true) Color(0xFF2E7D32) else TrafficRed,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Button(
                    onClick = {
                        codigoEnValidacion = true
                        codigoValidado = null
                        viewModel.limpiarAviso()
                        viewModel.entregarPorCodigo(
                            codigo,
                            establecimientoId,
                            empleadoUid,
                            sesionId
                        )
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally).height(40.dp),
                    enabled = puedeEntregar && codigo.length == 4 && viewModel.pedidoEnCambio == null,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 0.dp)
                ) {
                    Text(stringResource(Res.string.emp_delivery_validate), fontWeight = FontWeight.SemiBold)
                }
                Text(
                    stringResource(Res.string.emp_delivery_manual_hint),
                    color = MediumGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            OutlinedButton(
                onClick = {
                    pestanaEntregados = false
                    mostrarPedidos = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange),
                border = BorderStroke(1.dp, PrimaryOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ListAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.emp_delivery_open_orders), fontWeight = FontWeight.SemiBold)
            }
        }
    }
    AppSnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 8.dp, vertical = 8.dp)
    )
    }
    }
}

@Composable
private fun PedidoEntregaCard(
    pedido: Pedido,
    puedeEntregar: Boolean,
    puedeAbandonar: Boolean,
    enCambio: Boolean,
    onEntregar: () -> Unit,
    onNoReclamado: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFFFE0D8), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        pedido.turno.toString(),
                        color = PrimaryOrange,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(Res.string.emp_turn_label, pedido.turno),
                            fontWeight = FontWeight.Bold,
                            color = DarkGray,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(color = Color(0xFFE0F7FA), shape = RoundedCornerShape(50)) {
                            Text(
                                stringResource(Res.string.emp_delivery_ready),
                                color = Color(0xFF007C91),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        stringResource(Res.string.emp_delivery_waiting_pickup),
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    stringResource(Res.string.emp_delivery_ticket, pedido.id.takeLast(4).uppercase()),
                    color = PrimaryOrange,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightGray, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, tint = PrimaryOrange)
                Spacer(Modifier.width(10.dp))
                Text(
                    pedido.descripcion.ifBlank {
                        pedido.productos.joinToString(", ") { "${it.cantidad}× ${it.nombre}" }
                    },
                    color = DarkGray,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEntregar,
                    enabled = puedeEntregar && !enCambio,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.emp_delivery_confirm), fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onNoReclamado,
                    enabled = puedeAbandonar && !enCambio,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Block, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.emp_delivery_unclaimed), color = TrafficRed)
                }
            }
        }
    }
}
