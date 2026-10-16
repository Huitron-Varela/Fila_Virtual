package com.example.fila_virtual.features.empleados.estaciones.entrega

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Block
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
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
import fila_virtual.composeapp.generated.resources.emp_scan_qr
import fila_virtual.composeapp.generated.resources.emp_scan_reading
import fila_virtual.composeapp.generated.resources.emp_scan_retry
import fila_virtual.composeapp.generated.resources.emp_delivery_code
import fila_virtual.composeapp.generated.resources.emp_delivery_confirm
import fila_virtual.composeapp.generated.resources.emp_delivery_hint
import fila_virtual.composeapp.generated.resources.emp_delivery_manual
import fila_virtual.composeapp.generated.resources.emp_delivery_manual_hint
import fila_virtual.composeapp.generated.resources.emp_delivery_manual_placeholder
import fila_virtual.composeapp.generated.resources.emp_delivery_validate
import fila_virtual.composeapp.generated.resources.emp_delivery_queue_title
import fila_virtual.composeapp.generated.resources.emp_delivery_queue_sorted
import fila_virtual.composeapp.generated.resources.emp_delivery_ready
import fila_virtual.composeapp.generated.resources.emp_delivery_ticket
import fila_virtual.composeapp.generated.resources.emp_delivery_unclaimed
import fila_virtual.composeapp.generated.resources.emp_delivery_unclaimed_body
import fila_virtual.composeapp.generated.resources.emp_delivery_unclaimed_title
import fila_virtual.composeapp.generated.resources.emp_delivery_waiting_pickup
import fila_virtual.composeapp.generated.resources.emp_empty_entrega
import fila_virtual.composeapp.generated.resources.emp_turn_label
import org.jetbrains.compose.resources.stringResource

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
    var camaraAutorizada by remember { mutableStateOf(false) }
    var permisoSolicitado by remember { mutableStateOf(false) }
    var intentoCamara by remember { mutableIntStateOf(0) }
    var codigoLeido by remember { mutableStateOf<String?>(null) }
    var permisoDenegado by remember { mutableStateOf(false) }
    var avisoCamara by remember { mutableStateOf("") }
    val permisos = rememberPermissionsManager()
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
            codigo = ""
            codigoLeido = null
            intentoCamara += 1
        }
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

    AppPullToRefreshBox(
        isRefreshing = viewModel.cargando,
        onRefresh = { viewModel.escuchar(establecimientoId, forceRefresh = true) },
        modifier = modifier.fillMaxSize()
    ) {
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
                        codigoLeido != null && viewModel.error.isNotBlank() -> viewModel.error
                        avisoCamara.isNotBlank() -> avisoCamara
                        permisoDenegado -> stringResource(Res.string.emp_scan_denied)
                        !puedeEntregar -> stringResource(Res.string.emp_delivery_blocked)
                        else -> stringResource(Res.string.emp_scan_hint)
                    },
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
                if (codigoLeido != null && viewModel.error.isNotBlank()) {
                    TextButton(onClick = {
                        viewModel.limpiarAviso()
                        codigoLeido = null
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it.trim() },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text(stringResource(Res.string.emp_delivery_manual_placeholder)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Button(
                        onClick = {
                            viewModel.entregarPorCodigo(
                                codigo,
                                establecimientoId,
                                empleadoUid,
                                sesionId
                            )
                        },
                        enabled = puedeEntregar && codigo.isNotBlank() && viewModel.pedidoEnCambio == null,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(stringResource(Res.string.emp_delivery_validate), fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                    stringResource(Res.string.emp_delivery_manual_hint),
                    color = MediumGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (viewModel.error.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(viewModel.error, color = TrafficRed, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(12.dp))
        Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Res.string.emp_delivery_queue_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
                Spacer(Modifier.width(8.dp))
                Surface(color = PrimaryOrange, shape = RoundedCornerShape(50)) {
                    Text(
                        viewModel.pedidos.size.toString(),
                        color = LightSurface,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(Res.string.emp_delivery_queue_sorted),
                    color = MediumGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(10.dp))
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                viewModel.cargando && viewModel.pedidos.isEmpty() -> {
                    AppListSkeleton(Modifier.fillMaxSize().padding(8.dp), itemCount = 4)
                }
                viewModel.pedidos.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            stringResource(Res.string.emp_empty_entrega),
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
