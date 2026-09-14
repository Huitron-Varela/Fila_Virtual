package com.example.fila_virtual.features.user.ordenes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.repository.ProductoRepository

import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

import kotlinx.coroutines.launch

// ==========================================================
// TEXTO Y COLOR DE ESTADO
// ==========================================================
fun EstadoPedido.toUIText(): String {
    return when (this) {
        EstadoPedido.PENDIENTE -> "Pendiente"
        EstadoPedido.RECIBIDO -> "Recibido"
        EstadoPedido.EN_PREPARACION -> "Preparando"
        EstadoPedido.LISTO -> "¡Listo para recoger!"
        EstadoPedido.ENTREGADO -> "Entregado"
        EstadoPedido.CANCELADO -> "Cancelado"
    }
}

fun EstadoPedido.toUIColor(): Color {
    return when (this) {
        EstadoPedido.PENDIENTE, EstadoPedido.RECIBIDO -> Color(0xFFFFA726)
        EstadoPedido.EN_PREPARACION -> Color(0xFF29B6F6)
        EstadoPedido.LISTO -> Color(0xFF66BB6A)
        EstadoPedido.ENTREGADO -> MediumGray
        EstadoPedido.CANCELADO -> TrafficRed
    }
}

// ==========================================================
// SCREEN
// ==========================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdenesScreen(
    viewModel: OrdenesViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Activas", "Historial")

    val pedidosActivos by viewModel.pedidosActivos.collectAsState()
    val pedidosHistorial by viewModel.pedidosHistorial.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showQRModal by remember { mutableStateOf(false) }
    var selectedPedidoForQR by remember { mutableStateOf<Pedido?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRatingModal by remember { mutableStateOf(false) }
    var selectedPedidoForRating by remember { mutableStateOf<Pedido?>(null) }

    // 🔥 Memoria del ViewModel que sobrevive a la navegación
    val ratedOrders by viewModel.pedidosCalificados.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(LightBackground)
    ) {
        Text(
            text = "Mis Órdenes",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MediumGray
                        )
                    }
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (selectedTabIndex == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    if (pedidosActivos.isEmpty()) {
                        item {
                            Text("No tienes órdenes activas.", color = MediumGray, modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(items = pedidosActivos, key = { pedido -> pedido.id }) { pedido ->
                            OrderCard(
                                // 🔥 El título es la comida
                                restaurantName = pedido.descripcion,

                                // 🔥 Adiós "Al Toque Food". Hola folio limpio y profesional:
                                description = "Folio de compra: #${pedido.id.takeLast(6).uppercase()}",

                                price = "$${pedido.total}",
                                status = pedido.estado.toUIText(),
                                statusColor = pedido.estado.toUIColor(),
                                onClick = {
                                    selectedPedidoForQR = pedido
                                    showQRModal = true
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                ) {
                    if (pedidosHistorial.isEmpty()) {
                        item {
                            Text("No hay historial de órdenes.", color = MediumGray, modifier = Modifier.padding(16.dp))
                        }
                    } else {
                        items(items = pedidosHistorial, key = { pedido -> pedido.id }) { pedido ->
                            OrderHistoryCard(
                                // 🔥 El título es la comida
                                restaurantName = pedido.descripcion,

                                // 🔥 Adiós "Al Toque Food". Hola folio limpio y profesional:
                                description = "Folio de compra: #${pedido.id.takeLast(6).uppercase()}",

                                price = "$${pedido.total}",
                                status = pedido.estado.toUIText(),
                                yaCalificado = ratedOrders.contains(pedido.id),
                                onRateClick = {
                                    selectedPedidoForRating = pedido
                                    showRatingModal = true
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showQRModal && selectedPedidoForQR != null) {
        val pedidoActual = pedidosActivos.find { it.id == selectedPedidoForQR!!.id } ?: selectedPedidoForQR!!

        ModalBottomSheet(
            onDismissRequest = { showQRModal = false },
            sheetState = sheetState,
            containerColor = LightSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth().fillMaxHeight(0.85f)
                    .padding(horizontal = 24.dp).padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OrderProgressTimeline(estadoActual = pedidoActual.estado)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))

                if (pedidoActual.estado == EstadoPedido.LISTO) {
                    Text(
                        text = "TURNO #${pedidoActual.turno}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryOrange
                    )
                    Text(
                        text = "Muestra este código en mostrador",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediumGray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.size(220.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                            val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=ALTOQUE-${pedidoActual.id}"
                            KamelImage(resource = asyncPainterResource(data = qrUrl), contentDescription = "QR", modifier = Modifier.fillMaxSize())
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showQRModal = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cerrar Ticket", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth().height(250.dp)
                            .background(pedidoActual.estado.toUIColor().copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Fastfood, contentDescription = null, tint = pedidoActual.estado.toUIColor(), modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = when (pedidoActual.estado) {
                                    EstadoPedido.RECIBIDO -> "Pedido recibido"
                                    EstadoPedido.EN_PREPARACION -> "Preparando tu orden..."
                                    else -> "Procesando tu orden..."
                                },
                                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkGray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    OutlinedButton(
                        onClick = { showQRModal = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MediumGray)
                    ) {
                        Text("Ocultar", style = MaterialTheme.typography.titleMedium, color = DarkGray)
                    }
                }
            }
        }
    }

    if (showRatingModal && selectedPedidoForRating != null) {
        RatingModal(
            pedido = selectedPedidoForRating!!,
            onDismiss = { showRatingModal = false },
            onRatingSubmitted = {
                showRatingModal = false
                viewModel.marcarPedidoComoCalificado(selectedPedidoForRating!!.id)
            }
        )
    }
}

@Composable
fun OrderProgressTimeline(estadoActual: EstadoPedido) {
    val pasoActual = when (estadoActual) {
        EstadoPedido.PENDIENTE, EstadoPedido.RECIBIDO -> 1
        EstadoPedido.EN_PREPARACION -> 2
        EstadoPedido.LISTO, EstadoPedido.ENTREGADO -> 3
        EstadoPedido.CANCELADO -> 0
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ProgressStep("Recibido", pasoActual >= 1)
        ProgressLine(pasoActual >= 2, Modifier.weight(1f))
        ProgressStep("Preparando", pasoActual >= 2)
        ProgressLine(pasoActual >= 3, Modifier.weight(1f))
        ProgressStep("Listo", pasoActual >= 3)
    }
}

@Composable
fun ProgressStep(text: String, isActive: Boolean) {
    val color = if (isActive) PrimaryOrange else LightGray
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(24.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {}
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = if (isActive) DarkGray else MediumGray)
    }
}

@Composable
fun ProgressLine(isActive: Boolean, modifier: Modifier = Modifier) {
    val color = if (isActive) PrimaryOrange else LightGray
    Box(modifier = modifier.height(3.dp).padding(horizontal = 8.dp).background(color, RoundedCornerShape(50)))
}

@Composable
fun OrderCard(restaurantName: String, description: String, price: String, status: String, statusColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = LightSurface)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(LightBackground), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                // 🔥 Nativos maxLines = 1 y Ellipsis para que no brinque la línea
                Text(
                    text = restaurantName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = DarkGray.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(price, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            }
            Box(modifier = Modifier.fillMaxHeight().padding(start = 8.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = statusColor, modifier = Modifier.align(Alignment.TopEnd)) {
                    Text(status, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(restaurantName: String, description: String, price: String, status: String, yaCalificado: Boolean, onRateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = LightSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)).background(LightBackground), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // 🔥 Nativos maxLines = 1 y Ellipsis para que no brinque la línea
                    Text(
                        text = restaurantName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        color = DarkGray.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(price, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                }

                // 🔥 BADGE RESTAURADO A LA DERECHA
                Surface(shape = RoundedCornerShape(16.dp), color = LightGray.copy(alpha = 0.5f)) {
                    Text(status, color = MediumGray, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRateClick,
                enabled = !yaCalificado,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (yaCalificado) LightGray else PrimaryOrange),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange, disabledContentColor = MediumGray)
            ) {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (yaCalificado) "Platillos calificados" else "Calificar platillos", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingModal(
    pedido: Pedido,
    onDismiss: () -> Unit,
    onRatingSubmitted: () -> Unit
) {
    var productRatings by remember { mutableStateOf(mapOf<String, Int>()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val productoRepo = remember { ProductoRepository() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val productosDeLaOrden = pedido.productos ?: emptyList()

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        if (showSuccessMessage) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("¡Gracias por calificar!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { onRatingSubmitted() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("¿Qué te pareció tu orden?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (productosDeLaOrden.isEmpty()) {
                    Text("Esta orden es antigua y no tiene detalles para calificar.", color = MediumGray, modifier = Modifier.padding(vertical = 24.dp))
                } else {
                    productosDeLaOrden.forEach { producto ->
                        Text(text = producto.nombre, fontWeight = FontWeight.Bold, color = DarkGray, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 1..5) {
                                val currentRating = productRatings[producto.idProducto] ?: 0
                                Icon(
                                    imageVector = if (i <= currentRating) Icons.Default.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "Estrella",
                                    tint = if (i <= currentRating) TrafficYellow else BorderGray,
                                    modifier = Modifier.size(36.dp).clickable {
                                        if (!isSubmitting) {
                                            val newMap = productRatings.toMutableMap()
                                            newMap[producto.idProducto] = i
                                            productRatings = newMap
                                        }
                                    }
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(top = 16.dp), color = LightGray.copy(alpha = 0.3f))
                    }
                }

                if (errorMessage != null) {
                    Text("Error: $errorMessage", color = TrafficRed, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(vertical = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isSubmitting = true
                        errorMessage = null
                        scope.launch {
                            var allSuccess = true
                            for ((idProd, rating) in productRatings) {
                                val result = productoRepo.calificarProducto(idProd, rating)
                                if (result.isFailure) allSuccess = false
                            }
                            isSubmitting = false
                            if (allSuccess) showSuccessMessage = true else errorMessage = "No se pudieron enviar todas las calificaciones."
                        }
                    },
                    enabled = productRatings.isNotEmpty() && !isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Enviar calificaciones", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Cancelar", fontWeight = FontWeight.Bold, color = MediumGray, modifier = Modifier.clickable { if (!isSubmitting) onDismiss() })
            }
        }
    }
}