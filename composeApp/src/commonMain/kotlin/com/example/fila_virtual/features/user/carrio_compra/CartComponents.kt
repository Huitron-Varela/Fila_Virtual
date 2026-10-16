package com.example.fila_virtual.features.user.carrio_compra

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fila_virtual.components.FormHeader
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
    FormHeader(
        title = stringResource(Res.string.cart_title),
        onBack = onBackClick
    )
}

@Composable
fun InfoBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ExtraLightGray),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.padding(top = 2.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Tu pedido generará un turno de atención", fontWeight = FontWeight.Bold, color = DarkGray)
                Text("Acércate al mostrador cuando tu turno aparezca en pantalla.", color = MediumGray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun EmptyCartState(onBrowseClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 380.dp)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(112.dp),
            shape = CircleShape,
            color = Color(0xFFFFF0E6)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = PrimaryOrange,
                    modifier = Modifier.size(52.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.cart_empty),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DarkGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Cuando encuentres algo que se te antoje, aparecerá aquí para revisar tu pedido.",
            style = MaterialTheme.typography.bodyMedium,
            color = MediumGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.widthIn(max = 300.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onBrowseClick()
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
        ) {
            Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Explorar menú", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun CartItemsList(
    items: List<ProductoCarrito>,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { producto ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                BoxWithConstraints {
                    val compact = maxWidth < 380.dp

                    if (compact) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ProductPlaceholder()
                                Spacer(modifier = Modifier.width(12.dp))
                                CartItemDescription(producto, Modifier.weight(1f))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${producto.cantidad} × ${producto.precio.formatoMoneda()}",
                                    color = MediumGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                CartQuantityControls(
                                    quantity = producto.cantidad,
                                    onDecrement = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onDecrement(producto.idProducto)
                                    },
                                    onIncrement = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onIncrement(producto.idProducto)
                                    }
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductPlaceholder()
                            Spacer(modifier = Modifier.width(12.dp))
                            CartItemDescription(producto, Modifier.weight(1f))
                            Spacer(modifier = Modifier.width(8.dp))
                            CartQuantityControls(
                                quantity = producto.cantidad,
                                onDecrement = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDecrement(producto.idProducto)
                                },
                                onIncrement = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onIncrement(producto.idProducto)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodSection(tarjetas: List<TarjetaGuardada>, onEditClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var selectedCardKey by rememberSaveable { mutableStateOf("") }
    val selectedCard = tarjetas.firstOrNull { it.selectorKey() == selectedCardKey }
    val orderedCards = if (selectedCard == null) {
        tarjetas
    } else {
        listOf(selectedCard) + tarjetas.filterNot { it.selectorKey() == selectedCardKey }
    }

    LaunchedEffect(selectedCardKey, tarjetas) {
        if (selectedCardKey.isNotBlank() && selectedCard == null) {
            selectedCardKey = ""
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Método de pago",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkGray
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = PrimaryOrange)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mercado Pago", fontWeight = FontWeight.Bold, color = DarkGray)
                    Text(
                        "Elige forma de pago en el checkout seguro.",
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Icon(Icons.Default.CheckCircle, contentDescription = "Disponible", tint = PrimaryOrange)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tarjetas guardadas",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MediumGray
            )
            Text(
                text = "${tarjetas.size} ${if (tarjetas.size == 1) "tarjeta" else "tarjetas"}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryOrange
            )
        }

        if (orderedCards.isEmpty()) {
            Text(
                text = "Aún no tienes tarjetas guardadas.",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MediumGray
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                orderedCards.forEach { tarjeta ->
                    val cardKey = tarjeta.selectorKey()
                    val isSelected = cardKey == selectedCardKey
                    Card(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedCardKey = if (isSelected) "" else cardKey
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) {
                                SoftOrangeBg.copy(alpha = 0.78f)
                            } else {
                                LightSurface.copy(alpha = 0.68f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.dp else 0.5.dp,
                            color = if (isSelected) PrimaryOrange else BorderGray.copy(alpha = 0.7f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 1.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, tint = DarkGray)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${tarjeta.marca} •••• ${tarjeta.ultimos4}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkGray,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Vence ${tarjeta.expiracion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MediumGray
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Seleccionada", tint = PrimaryOrange)
                            }
                        }
                    }
                }
            }
        }

        TextButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEditClick()
            },
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Text("Administrar tarjetas en Wallet", color = PrimaryOrange, fontWeight = FontWeight.Bold)
        }
    }
}

private fun TarjetaGuardada.selectorKey(): String = "$marca|$ultimos4|$expiracion|$nombreTitular"

@Composable
private fun ProductPlaceholder() {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(ExtraLightGray, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray)
    }
}

@Composable
private fun CartItemDescription(producto: ProductoCarrito, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = producto.nombre,
            fontWeight = FontWeight.Bold,
            color = DarkGray,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = producto.precio.formatoMoneda(),
            color = PrimaryOrange,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CartQuantityControls(
    quantity: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onDecrement,
            modifier = Modifier.size(40.dp).background(PrimaryOrange, CircleShape)
        ) {
            Icon(Icons.Default.Remove, contentDescription = stringResource(Res.string.cart_quantity_decrease), tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Text(
            text = quantity.toString(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 32.dp).padding(horizontal = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(40.dp).background(PrimaryOrange, CircleShape)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.cart_quantity_increase), tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun SummarySection(subtotal: Double, tarifa: Double, total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
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
                Text(total.formatoMoneda(), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryOrange)
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