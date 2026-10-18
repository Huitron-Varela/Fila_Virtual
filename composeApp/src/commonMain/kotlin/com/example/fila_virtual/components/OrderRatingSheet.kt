package com.example.fila_virtual.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.ProductoCarrito
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.rating_sheet_empty
import fila_virtual.composeapp.generated.resources.rating_sheet_product_count
import fila_virtual.composeapp.generated.resources.rating_sheet_submit
import fila_virtual.composeapp.generated.resources.rating_sheet_subtitle
import fila_virtual.composeapp.generated.resources.rating_sheet_success
import fila_virtual.composeapp.generated.resources.rating_sheet_success_action
import fila_virtual.composeapp.generated.resources.rating_sheet_title
import fila_virtual.composeapp.generated.resources.rating_sheet_turn
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderRatingSheet(
    pedido: Pedido,
    ratings: Map<String, Int>,
    isSubmitting: Boolean,
    hasSubmitted: Boolean,
    errorMessage: String?,
    onRatingChanged: (String, Int) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    onSuccessAcknowledged: () -> Unit
) {
    val windowSize = LocalWindowSize.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val maxHeight = (windowSize.height * 0.88f).coerceAtMost(760.dp)

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = LightBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
        ) {
            if (hasSubmitted) {
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = TrafficGreen,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(Res.string.rating_sheet_success),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
                RatingSheetFooter(
                    label = stringResource(Res.string.rating_sheet_success_action),
                    enabled = true,
                    isSubmitting = false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSuccessAcknowledged()
                    }
                )
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = windowSize.compactDp(20))
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (pedido.turno > 0) {
                        Surface(color = SoftOrangeBg, shape = CircleShape) {
                            Text(
                                text = stringResource(Res.string.rating_sheet_turn, pedido.turno),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = DarkGray,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(Res.string.rating_sheet_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.rating_sheet_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    if (pedido.productos.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.rating_sheet_empty),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            color = MediumGray,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        pedido.productos.forEach { producto ->
                            ProductRatingCard(
                                producto = producto,
                                rating = ratings[producto.idProducto] ?: 0,
                                enabled = !isSubmitting,
                                onRatingSelected = { onRatingChanged(producto.idProducto, it) }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            color = TrafficRed,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                RatingSheetFooter(
                    label = stringResource(Res.string.rating_sheet_submit),
                    enabled = ratings.isNotEmpty() && !isSubmitting,
                    isSubmitting = isSubmitting,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSubmit()
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductRatingCard(
    producto: ProductoCarrito,
    rating: Int,
    enabled: Boolean,
    onRatingSelected: (Int) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)).background(SoftOrangeBg),
                    contentAlignment = Alignment.Center
                ) {
                    RemoteImage(
                        url = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        fallback = {
                            Icon(Icons.Default.Fastfood, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(22.dp))
                        }
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = producto.nombre,
                        color = DarkGray,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(Res.string.rating_sheet_product_count, producto.cantidad),
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                    if (producto.descripcion.isNotBlank()) {
                        Text(
                            text = producto.descripcion,
                            color = MediumGray,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (star in 1..5) {
                    IconButton(
                        onClick = {
                            if (enabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onRatingSelected(star)
                            }
                        },
                        enabled = enabled,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarOutline,
                            contentDescription = star.toString(),
                            tint = if (star <= rating) TrafficYellow else BorderGray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingSheetFooter(
    label: String,
    enabled: Boolean,
    isSubmitting: Boolean,
    onClick: () -> Unit
) {
    Surface(color = LightBackground, shadowElevation = 8.dp) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                Text(label, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

