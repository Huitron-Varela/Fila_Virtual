package com.example.fila_virtual.features.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlin.math.round
import kotlin.math.roundToInt
import com.example.fila_virtual.components.RemoteImage
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.Producto
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.product_no_reviews
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailModal(
    producto: Producto,
    storeName: String,
    onDismiss: () -> Unit,
    onAddToCart: (Producto, Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val windowSize = LocalWindowSize.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var quantity by remember(producto.id) { mutableIntStateOf(1) }
    val horizontalPadding = windowSize.compactDp(20)
    val modalMaxHeight = (windowSize.height * 0.82f).coerceAtMost(760.dp)
    val totalCents = round(producto.precio * quantity * 100).toLong()
    val totalFormatted = "$${totalCents / 100}.${(totalCents % 100).toString().padStart(2, '0')}"
    val priceFormatted = formatProductPrice(producto.precio)
    val rating = (producto.ratingPromedio.coerceIn(0.0, 5.0) * 10).roundToInt() / 10.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = modalMaxHeight)
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = if (windowSize.isSmallScreen) 180.dp else 240.dp)
                    .aspectRatio(if (windowSize.isSmallScreen) 2.1f else 2.4f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ExtraLightGray),
                contentAlignment = Alignment.Center
            ) {
                RemoteImage(
                    url = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallback = {
                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray, modifier = Modifier.size(48.dp))
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(7.dp).background(TrafficGreen, CircleShape))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = listOf(storeName, producto.categoria).filter(String::isNotBlank).joinToString(" • "),
                        modifier = Modifier.weight(1f),
                        color = MediumGray,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (producto.totalVotos > 0) {
                        Surface(color = SoftOrangeBg, shape = RoundedCornerShape(8.dp)) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = TrafficYellow, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "$rating (${producto.totalVotos})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkGray,
                                    maxLines = 1
                                )
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(Res.string.product_no_reviews),
                            style = MaterialTheme.typography.labelSmall,
                            color = MediumGray,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = producto.nombre,
                        modifier = Modifier.weight(1f).padding(end = 12.dp),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = priceFormatted,
                        color = PrimaryOrange,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                }

                Text(
                    text = producto.descripcion.ifBlank { "Este producto no tiene una descripción disponible." },
                    modifier = Modifier.padding(top = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGray
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding, vertical = 12.dp)
            ) {
                val addToCart: () -> Unit = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddToCart(producto, quantity)
                }
                val quantitySelector: @Composable (Modifier) -> Unit = { modifier ->
                    QuantitySelector(
                        quantity = quantity,
                        onDecrease = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (quantity > 1) quantity--
                        },
                        onIncrease = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            quantity = (quantity + 1).coerceAtMost(99)
                        },
                        modifier = modifier
                    )
                }

                if (maxWidth < 300.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        quantitySelector(Modifier)
                        Button(
                            onClick = addToCart,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            AddToCartButtonContent(totalFormatted)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quantitySelector(Modifier.width(136.dp))
                        Button(
                            onClick = addToCart,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            AddToCartButtonContent(totalFormatted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddToCartButtonContent(total: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Añadir al carrito",
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Surface(
            color = Color.White.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50)
        ) {
            Text(
                text = total,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(ExtraLightGray, RoundedCornerShape(28.dp))
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onDecrease,
            enabled = quantity > 1,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = PrimaryOrange,
                contentColor = Color.White,
                disabledContainerColor = PrimaryOrange.copy(alpha = 0.45f),
                disabledContentColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(PrimaryOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Reducir cantidad", modifier = Modifier.size(16.dp))
            }
        }
        Text(
            text = quantity.toString().padStart(2, '0'),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        IconButton(
            onClick = onIncrease,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = PrimaryOrange,
                contentColor = Color.White
            )
        ) {
            Box(
                modifier = Modifier.size(32.dp).background(PrimaryOrange, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aumentar cantidad", modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun formatProductPrice(value: Double): String {
    val cents = round(value * 100).toLong()
    return "$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
}