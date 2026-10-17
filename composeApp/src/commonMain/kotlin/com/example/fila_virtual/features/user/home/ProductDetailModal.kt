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
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Restaurant
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.RemoteImage
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.Producto

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
                    .heightIn(max = if (windowSize.isSmallScreen) 160.dp else 220.dp)
                    .aspectRatio(if (windowSize.isSmallScreen) 1.5f else 1.65f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E24)),
                contentAlignment = Alignment.Center
            ) {
                RemoteImage(
                    url = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    fallback = {
                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                )

                Surface(
                    color = ActionBlue,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .widthIn(max = 180.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Restaurant,
                            contentDescription = null,
                            tint = LightSurface,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = producto.categoria,
                            color = LightSurface,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = producto.nombre,
                        modifier = Modifier.weight(1f).padding(end = 12.dp),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$${producto.precio}",
                        color = PrimaryOrange,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1
                    )
                }
                Text(text = storeName, color = MediumGray, style = MaterialTheme.typography.bodyMedium)

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (producto.totalVotos > 0) {
                        val fullStars = producto.ratingPromedio.coerceIn(0.0, 5.0).toInt()
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < fullStars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = TrafficYellow,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "${producto.ratingPromedio} (${producto.totalVotos} reseñas)",
                            modifier = Modifier.padding(start = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MediumGray
                        )
                    } else {
                        Text("Aún sin reseñas", style = MaterialTheme.typography.labelMedium, color = MediumGray)
                    }
                }

                Text(
                    text = producto.descripcion.ifBlank { "Este producto no tiene descripción." },
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
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
                        onDecrease = { if (quantity > 1) quantity-- },
                        onIncrease = { quantity++ },
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
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                "Añadir al carrito",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
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
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(
                                "Añadir al carrito",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
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