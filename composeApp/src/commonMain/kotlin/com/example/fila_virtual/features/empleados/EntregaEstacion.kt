package com.example.fila_virtual.features.empleados

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.core.PermissionType
import com.example.fila_virtual.core.VisorCodigoQr
import com.example.fila_virtual.core.rememberPermissionsManager
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.coincideCodigoRecogida
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.btn_cancel
import fila_virtual.composeapp.generated.resources.emp_delivery_blocked
import fila_virtual.composeapp.generated.resources.emp_scan_close
import fila_virtual.composeapp.generated.resources.emp_scan_denied
import fila_virtual.composeapp.generated.resources.emp_scan_hint
import fila_virtual.composeapp.generated.resources.emp_scan_next
import fila_virtual.composeapp.generated.resources.emp_scan_qr
import fila_virtual.composeapp.generated.resources.emp_scan_reading
import fila_virtual.composeapp.generated.resources.emp_scan_retry
import fila_virtual.composeapp.generated.resources.emp_delivery_code
import fila_virtual.composeapp.generated.resources.emp_delivery_confirm
import fila_virtual.composeapp.generated.resources.emp_delivery_hint
import fila_virtual.composeapp.generated.resources.emp_delivery_unclaimed
import fila_virtual.composeapp.generated.resources.emp_delivery_unclaimed_body
import fila_virtual.composeapp.generated.resources.emp_delivery_unclaimed_title
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
    var escaneando by remember { mutableStateOf(false) }
    var intentoCamara by remember { mutableIntStateOf(0) }
    var codigoLeido by remember { mutableStateOf<String?>(null) }
    var permisoDenegado by remember { mutableStateOf(false) }
    var avisoCamara by remember { mutableStateOf("") }
    val permisos = rememberPermissionsManager()
    val puedeEntregar = turnoGuardado && sesionId.isNotBlank()

    LaunchedEffect(establecimientoId) {
        viewModel.escuchar(establecimientoId)
    }
    LaunchedEffect(viewModel.cierreListo) {
        if (viewModel.cierreListo > 0) codigo = ""
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

    if (escaneando) {
        Dialog(
            onDismissRequest = { escaneando = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val leido = codigoLeido
            val entregado = leido != null && viewModel.confirmacion.isNotBlank()
            val fallo = leido != null && viewModel.pedidoEnCambio == null && viewModel.error.isNotBlank()
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (!entregado && !fallo) {
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
                if (entregado || fallo) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (entregado) Color(0xFF1B5E20) else Color(0xFFB71C1C))
                            .padding(horizontal = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            if (entregado) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (entregado) viewModel.confirmacion else viewModel.error,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        if (leido != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(leido, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = {
                                if (fallo) {
                                    viewModel.limpiarAviso()
                                    codigoLeido = null
                                    intentoCamara += 1
                                } else {
                                    escaneando = false
                                    codigoLeido = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Text(
                                stringResource(if (fallo) Res.string.emp_scan_retry else Res.string.emp_scan_next),
                                color = if (entregado) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(Modifier.fillMaxWidth().weight(1f).background(Color.Black.copy(alpha = 0.55f)))
                        Row(Modifier.fillMaxWidth().height(280.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f).fillMaxHeight().background(Color.Black.copy(alpha = 0.55f)))
                            Box(
                                Modifier
                                    .size(280.dp)
                                    .border(3.dp, if (leido != null) PrimaryOrange else Color.White, RoundedCornerShape(28.dp))
                            )
                            Box(Modifier.weight(1f).fillMaxHeight().background(Color.Black.copy(alpha = 0.55f)))
                        }
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.Black.copy(alpha = 0.72f))
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                stringResource(if (leido == null) Res.string.emp_scan_hint else Res.string.emp_scan_reading),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(16.dp))
                            TextButton(onClick = {
                                escaneando = false
                                codigoLeido = null
                            }) {
                                Text(stringResource(Res.string.emp_scan_close), color = Color.White, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = {
                if (!puedeEntregar) return@Button
                permisos.askPermission(PermissionType.CAMERA) { concedido ->
                    permisoDenegado = !concedido
                    avisoCamara = ""
                    escaneando = concedido
                }
            },
            enabled = puedeEntregar && viewModel.pedidoEnCambio == null,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                Text(stringResource(Res.string.emp_scan_qr), fontWeight = FontWeight.SemiBold)
            }
        }
        if (viewModel.confirmacion.isNotBlank() && !escaneando) {
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
        OutlinedTextField(
            value = codigo,
            onValueChange = { codigo = it.trim() },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text(stringResource(Res.string.emp_delivery_code)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(if (turnoGuardado) Res.string.emp_delivery_hint else Res.string.emp_delivery_blocked),
            color = MediumGray,
            style = MaterialTheme.typography.bodySmall
        )
        if (permisoDenegado) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(Res.string.emp_scan_denied),
                color = TrafficRed,
                style = MaterialTheme.typography.bodySmall
            )
        } else if (avisoCamara.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(avisoCamara, color = TrafficRed, style = MaterialTheme.typography.bodySmall)
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
            Text(
                stringResource(Res.string.emp_turn_label, pedido.turno),
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                pedido.descripcion.ifBlank {
                    pedido.productos.joinToString(", ") { "${it.cantidad}× ${it.nombre}" }
                },
                color = MediumGray,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEntregar,
                    enabled = puedeEntregar && !enCambio,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text(stringResource(Res.string.emp_delivery_confirm), fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onNoReclamado,
                    enabled = puedeAbandonar && !enCambio,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(Res.string.emp_delivery_unclaimed), color = TrafficRed)
                }
            }
        }
    }
}
