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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fila_virtual.components.Heders.StoreHeader
import com.example.fila_virtual.components.RemoteImage
import com.example.fila_virtual.components.cards.RecommendationProductCard
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.Producto
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
fun CartTopBar(
    onBackClick: () -> Unit,
    onClearClick: () -> Unit,
    canClearCart: Boolean
) {
    val haptic = LocalHapticFeedback.current
    StoreHeader(
        title = stringResource(Res.string.cart_title),
        onBack = onBackClick,
        containerColor = LightBackground,
        backButtonContainerColor = Color.Transparent,
        titleStyle = MaterialTheme.typography.titleLarge,
        titleColor = DarkGray,
        trailingContent = {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClearClick()
                },
                enabled = canClearCart
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.cart_clear_action),
                    tint = if (canClearCart) MaterialTheme.colorScheme.error else MediumGray.copy(alpha = 0.45f)
                )
            }
        },
        horizontalPadding = 8.dp,
        verticalPadding = 12.dp
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
fun EmptyCartState(
    recommendations: List<Pair<Producto, String>>,
    onBrowseClick: () -> Unit,
    onAddRecommendation: (Producto, String, String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp).padding(20.dp),
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
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = stringResource(Res.string.cart_empty),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Parece que aún no has agregado nada. Explora nuestros productos y pide AlToque.",
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.widthIn(max = 300.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBrowseClick()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Explorar menú", fontWeight = FontWeight.Bold)
            }
        }

        if (recommendations.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(width = 4.dp, height = 20.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = PrimaryOrange
                ) {}
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(Res.string.cart_quick_recommendations),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
            }
            recommendations.forEach { (producto, establecimientoNombre) ->
                val addedMessage = stringResource(Res.string.user_cart_item_added, producto.nombre)
                RecommendationProductCard(
                    producto = producto,
                    storeName = establecimientoNombre,
                    onClick = onBrowseClick,
                    onAddToCartClick = {
                        onAddRecommendation(producto, establecimientoNombre, addedMessage)
                    }
                )
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
    val haptic = LocalHapticFeedback.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items.forEach { producto ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CartProductImage(producto)
                    Spacer(modifier = Modifier.width(8.dp))
                    CartItemDescription(producto, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(2.dp))
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

@Composable
fun PaymentMethodSection(
    tarjetas: List<TarjetaGuardada>,
    tarjetaSeleccionada: TarjetaGuardada?,
    onSelectCard: (TarjetaGuardada?) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val orderedCards = if (tarjetaSeleccionada == null) {
        tarjetas
    } else {
        listOf(tarjetaSeleccionada) + tarjetas.filterNot { it.selectorKey() == tarjetaSeleccionada.selectorKey() }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.cart_payment_method),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF15966B), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(Res.string.cart_secure_payment),
                    style = MaterialTheme.typography.bodySmall,
                    color = MediumGray
                )
            }
        }

        val mercadoPagoSeleccionado = tarjetaSeleccionada == null
        PaymentChoiceCard(
            selected = mercadoPagoSeleccionado,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSelectCard(null)
            },
            icon = Icons.Default.AccountBalanceWallet,
            title = "Mercado Pago",
            detail = stringResource(Res.string.cart_payment_checkout_methods),
            iconTint = Color(0xFF009EE3),
            recommended = true
        )

        if (orderedCards.isEmpty()) {
            Text(
                text = stringResource(Res.string.cart_no_cards_message),
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MediumGray
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                orderedCards.forEach { tarjeta ->
                    val isSelected = tarjeta.selectorKey() == tarjetaSeleccionada?.selectorKey()
                    PaymentChoiceCard(
                        selected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectCard(if (isSelected) null else tarjeta)
                        },
                        icon = Icons.Default.CreditCard,
                        title = "${tarjeta.marca} •••• ${tarjeta.ultimos4}",
                        detail = "${stringResource(Res.string.cart_expires)} ${tarjeta.expiracion}",
                        iconTint = DarkGray
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentChoiceCard(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    iconTint: Color,
    recommended: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) PrimaryOrange else BorderGray.copy(alpha = 0.65f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 1.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (iconTint == Color(0xFF009EE3)) Color(0xFFE5F6FC) else ExtraLightGray
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        modifier = Modifier.weight(1f, fill = false),
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (recommended) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = Color(0xFFE0F0FF), shape = RoundedCornerShape(5.dp)) {
                            Text(
                                text = stringResource(Res.string.cart_payment_recommended),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = Color(0xFF1677B8),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
                Text(
                    text = detail,
                    color = MediumGray,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .then(
                        if (selected) Modifier.background(PrimaryOrange, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else {
                    Surface(
                        modifier = Modifier.size(20.dp),
                        shape = CircleShape,
                        color = Color.Transparent,
                        border = androidx.compose.foundation.BorderStroke(2.dp, BorderGray)
                    ) {}
                }
            }
        }
    }
}

internal fun TarjetaGuardada.selectorKey(): String = "$marca|$ultimos4|$expiracion|$nombreTitular"

@Composable
private fun CartProductImage(producto: ProductoCarrito) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ExtraLightGray),
        contentAlignment = Alignment.Center
    ) {
        RemoteImage(
            url = producto.imagenUrl,
            contentDescription = producto.nombre,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            fallback = {
                Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray)
            }
        )
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
            text = producto.descripcion.ifBlank { stringResource(Res.string.cart_item_description_fallback) },
            color = MediumGray,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (producto.cantidad > 1) {
            Text(
                text = "${producto.precio.formatoMoneda()} c/u",
                color = MediumGray,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            text = (producto.precio * producto.cantidad).formatoMoneda(),
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
            modifier = Modifier.size(36.dp)
        ) {
            Box(
                modifier = Modifier.size(28.dp).background(PrimaryOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = stringResource(Res.string.cart_quantity_decrease), tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Text(
            text = quantity.toString(),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 24.dp).padding(horizontal = 2.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        IconButton(
            onClick = onIncrement,
            modifier = Modifier.size(36.dp)
        ) {
            Box(
                modifier = Modifier.size(28.dp).background(PrimaryOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.cart_quantity_increase), tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun SummarySection(items: List<ProductoCarrito>) {
    val total = items.sumOf { it.precio * it.cantidad }
    val cantidadTotal = items.sumOf { it.cantidad }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(Res.string.general_summary), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGray)
                Text(
                    stringResource(Res.string.cart_summary_item_count, cantidadTotal),
                    color = MediumGray,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            items.take(3).forEach { producto ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${producto.nombre} × ${producto.cantidad}",
                        modifier = Modifier.weight(1f),
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        (producto.precio * producto.cantidad).formatoMoneda(),
                        color = DarkGray,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            if (items.size > 3) {
                Text(
                    stringResource(Res.string.cart_summary_more, items.size - 3),
                    modifier = Modifier.padding(top = 4.dp),
                    color = MediumGray,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderGray)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(Res.string.general_total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkGray)
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