package com.example.fila_virtual.features.user.carrio_compra

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.ProductoCarrito
import com.example.fila_virtual.data.TarjetaGuardada
import kotlin.math.round
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*

// 🔥 ESTA ES LA FUNCIÓN QUE LIMPIA LOS DATOS SUCIOS DE LA BASE DE DATOS 🔥
fun Double.formatoMoneda(): String {
    val redondeado = round(this * 100) / 100.0
    val partes = redondeado.toString().split(".")
    val enteros = partes[0]
    val decimales = if (partes.size > 1) partes[1].padEnd(2, '0') else "00"
    return "$$enteros.$decimales"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartTopBar(onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = "Carrito", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "Regresar") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun InfoBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.padding(top = 2.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Tu pedido generará un turno de atención", fontWeight = FontWeight.Bold, color = DarkGray)
                Text("Acércate al mostrador cuando tu turno aparezca en pantalla.", color = MediumGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun CartItemsList(
    items: List<ProductoCarrito>,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { producto ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(60.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = producto.nombre, fontWeight = FontWeight.Bold, color = DarkGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        // 🔥 APLICAMOS EL FORMATO LIMPIO AL PRECIO UNITARIO 🔥
                        Text(text = producto.precio.formatoMoneda(), color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    }

                    // CONTROLES DE CANTIDAD FUNCIONALES
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(PrimaryOrange).clickable { onDecrement(producto.idProducto) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "${producto.cantidad}",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(PrimaryOrange).clickable { onIncrement(producto.idProducto) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodSection(tarjeta: TarjetaGuardada?, onEditClick: () -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Método de Pago", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGray)
            Text("Editar", color = Color(0xFFE53935), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onEditClick() })
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth().clickable { onEditClick() },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = DarkGray)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (tarjeta != null) {
                        Text("${tarjeta.marca} terminada en ${tarjeta.ultimos4}", fontWeight = FontWeight.Bold, color = DarkGray)
                        Text("Expira ${tarjeta.expiracion}", color = MediumGray, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Selecciona una tarjeta", fontWeight = FontWeight.Bold, color = DarkGray)
                    }
                }
                if (tarjeta != null) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryOrange)
                }
            }
        }
    }
}

@Composable
fun SummarySection(subtotal: Double, tarifa: Double, total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal", color = DarkGray)
                // 🔥 APLICAMOS FORMATO LIMPIO 🔥
                Text(subtotal.formatoMoneda(), color = DarkGray, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tarifa de servicio", color = DarkGray)
                // 🔥 APLICAMOS FORMATO LIMPIO 🔥
                Text(tarifa.formatoMoneda(), color = DarkGray, fontWeight = FontWeight.Medium)
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderGray)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGray)
                // 🔥 APLICAMOS FORMATO LIMPIO AL TOTAL 🔥
                Text(total.formatoMoneda(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFE53935))
            }
        }
    }
}

@Composable
fun CartBottomBar(
    isProcessing: Boolean,
    errorMessage: String,
    isEnabled: Boolean,
    onPayClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding(16.dp)) {
        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        }
        Button(
            onClick = onPayClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            shape = RoundedCornerShape(16.dp),
            enabled = isEnabled && !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Pagar y Generar Turno", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorTarjetasModal(
    tarjetas: List<TarjetaGuardada>,
    tarjetaActual: TarjetaGuardada?,
    onTarjetaSelected: (TarjetaGuardada) -> Unit,
    onAddNewCardClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 8.dp, start = 24.dp, end = 24.dp)
        ) {
            Text(
                text = "Selecciona método de pago",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp),
                color = DarkGray
            )

            tarjetas.forEach { tarjeta ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onTarjetaSelected(tarjeta) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (tarjeta == tarjetaActual) Color(0xFFFFF0E6) else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (tarjeta == tarjetaActual) androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange) else androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = DarkGray)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${tarjeta.marca} terminada en ${tarjeta.ultimos4}", fontWeight = FontWeight.Bold, color = DarkGray)
                        }
                        if (tarjeta == tarjetaActual) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryOrange)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddNewCardClick() },
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MediumGray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Agregar nueva tarjeta", fontWeight = FontWeight.Medium, color = DarkGray)
                }
            }
        }
    }
}