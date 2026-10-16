package com.example.fila_virtual.features.user.ordenes

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox

import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.codigoVisible
import com.example.fila_virtual.repository.ProductoRepository

import androidx.compose.foundation.Image
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

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
        EstadoPedido.NO_RECLAMADO -> "No reclamado"
        EstadoPedido.CANCELADO -> "Cancelado"
    }
}

fun EstadoPedido.toUIColor(): Color {
    return when (this) {
        EstadoPedido.PENDIENTE, EstadoPedido.RECIBIDO -> Color(0xFFFFA726)
        EstadoPedido.EN_PREPARACION -> Color(0xFF29B6F6)
        EstadoPedido.LISTO -> Color(0xFF66BB6A)
        EstadoPedido.ENTREGADO, EstadoPedido.NO_RECLAMADO -> MediumGray
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
    val windowSize = LocalWindowSize.current
    val haptic = LocalHapticFeedback.current
    val horizontalPadding = windowSize.compactDp(20)
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Activas", "Historial")

    val pedidosActivos by viewModel.pedidosActivos.collectAsState()
    val pedidosHistorial by viewModel.pedidosHistorial.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showQRModal by remember { mutableStateOf(false) }
    var selectedPedidoForQR by remember { mutableStateOf<Pedido?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRatingModal by remember { mutableStateOf(false) }
    var selectedPedidoForRating by remember { mutableStateOf<Pedido?>(null) }

    // 🔥 Memoria del ViewModel que sobrevive a la navegación
    val ratedOrders by viewModel.pedidosCalificados.collectAsState()

    AppPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refrescarPedidos,
        modifier = Modifier.fillMaxSize().background(LightBackground)
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Mis Órdenes",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = windowSize.adaptiveSp(20)),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = windowSize.compactDp(24), bottom = 16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            modifier = Modifier.padding(horizontal = horizontalPadding),
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
                    onClick = {
                        if (selectedTabIndex != index) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTabIndex = index
                        }
                    },
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
            AppListSkeleton(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = horizontalPadding),
                itemCount = 5,
                imageSize = 72.dp
            )
        } else {
            Crossfade(
                targetState = selectedTabIndex,
                modifier = Modifier.weight(1f),
                label = "orders_tab_content"
            ) { tabIndex ->
                if (tabIndex == 0) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        if (pedidosActivos.isEmpty()) {
                            item { OrdersEmptyState(isHistory = false) }
                        } else {
                            items(items = pedidosActivos, key = { pedido -> pedido.id }) { pedido ->
                                OrderCard(
                                    restaurantName = pedido.descripcion,
                                    description = "Folio de compra: #${pedido.id.takeLast(6).uppercase()}",
                                    price = "$${pedido.total}",
                                    status = pedido.estado.toUIText(),
                                    statusColor = pedido.estado.toUIColor(),
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedPedidoForQR = pedido
                                        showQRModal = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
                    ) {
                        if (pedidosHistorial.isEmpty()) {
                            item { OrdersEmptyState(isHistory = true) }
                        } else {
                            items(items = pedidosHistorial, key = { pedido -> pedido.id }) { pedido ->
                                OrderHistoryCard(
                                    restaurantName = pedido.descripcion,
                                    description = "Folio de compra: #${pedido.id.takeLast(6).uppercase()}",
                                    price = "$${pedido.total}",
                                    status = pedido.estado.toUIText(),
                                    yaCalificado = ratedOrders.contains(pedido.id),
                                    onRateClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedPedidoForRating = pedido
                                        showRatingModal = true
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
        }
    }
    }

    val pedidoSeleccionadoId = selectedPedidoForQR?.id
    val pedidoEnVivo = pedidosActivos.find { it.id == pedidoSeleccionadoId }
        ?: pedidosHistorial.find { it.id == pedidoSeleccionadoId }
    var idsActivosPrevios by remember { mutableStateOf<Set<String>?>(null) }
    LaunchedEffect(pedidosActivos, pedidosHistorial) {
        val idsAhora = pedidosActivos.map { it.id }.toSet()
        val previos = idsActivosPrevios
        if (previos != null) {
            val salieron = previos - idsAhora
            val yaEnHistorial = salieron.any { id -> pedidosHistorial.any { it.id == id } }
            if (yaEnHistorial) {
                showQRModal = false
                selectedPedidoForQR = null
                selectedTabIndex = 1
            } else if (salieron.isNotEmpty()) {
                return@LaunchedEffect
            }
        }
        idsActivosPrevios = idsAhora
    }

    if (showQRModal && pedidoEnVivo != null) {
        val pedidoActual = pedidoEnVivo
        val qrSize = windowSize.compactDp(260).coerceIn(180.dp, 260.dp)
        val modalMaxHeight = (windowSize.height * 0.88f).coerceAtMost(760.dp)

        ModalBottomSheet(
            onDismissRequest = { showQRModal = false },
            sheetState = sheetState,
            containerColor = LightSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth().heightIn(max = modalMaxHeight)
                    .padding(horizontal = 24.dp).padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OrderProgressTimeline(estadoActual = pedidoActual.estado)
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = LightGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(24.dp))

                if (pedidoActual.estado == EstadoPedido.LISTO) {
                    val codigo = codigoVisible(pedidoActual)
                    Text(
                        text = "TURNO #${pedidoActual.turno}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryOrange
                    )
                    Text(
                        text = "Listo para recoger",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Acerca este QR al lector del mostrador. Si no lo lee, dicta el código.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediumGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.size(qrSize)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().padding(18.dp), contentAlignment = Alignment.Center) {
                            Image(
                                painter = rememberQrCodePainter(codigo) {
                                    errorCorrectionLevel = QrErrorCorrectionLevel.High
                                },
                                contentDescription = "Código QR",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CÓDIGO DE RECOGIDA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MediumGray,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = codigo,
                        style = MaterialTheme.typography.displaySmall.copy(fontSize = windowSize.adaptiveSp(30)),
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkGray,
                        letterSpacing = 4.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showQRModal = false
                        },
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
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showQRModal = false
                        },
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
        EstadoPedido.NO_RECLAMADO, EstadoPedido.CANCELADO -> 0
    }
    Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = LocalWindowSize.current.compactDp(32), vertical = 16.dp),
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
    val color by animateColorAsState(
        targetValue = if (isActive) PrimaryOrange else LightGray,
        label = "progress_step_color"
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(24.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {}
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = if (isActive) DarkGray else MediumGray)
    }
}

@Composable
fun ProgressLine(isActive: Boolean, modifier: Modifier = Modifier) {
    val color by animateColorAsState(
        targetValue = if (isActive) PrimaryOrange else LightGray,
        label = "progress_line_color"
    )
    Box(modifier = modifier.height(3.dp).padding(horizontal = 8.dp).background(color, RoundedCornerShape(50)))
}

@Composable
fun OrderCard(restaurantName: String, description: String, price: String, status: String, statusColor: Color, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BoxWithConstraints {
            val compact = maxWidth < 380.dp
            if (compact) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        OrderFoodIcon(size = 56.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        OrderDetails(restaurantName, description, price, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(6.dp))
                        OrderStatusAndArrow(status, statusColor)
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OrderFoodIcon(size = 68.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    OrderDetails(restaurantName, description, price, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    OrderStatusAndArrow(status, statusColor)
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(restaurantName: String, description: String, price: String, status: String, yaCalificado: Boolean, onRateClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        BoxWithConstraints {
            val compact = maxWidth < 380.dp
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                if (compact) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OrderFoodIcon(size = 56.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        OrderDetails(restaurantName, description, price, Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OrderStatusBadge(status, MediumGray)
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OrderFoodIcon(size = 68.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        OrderDetails(restaurantName, description, price, Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        OrderStatusBadge(status, MediumGray)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = LightGray.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRateClick()
                    },
                    enabled = !yaCalificado,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
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
}

@Composable
private fun OrderFoodIcon(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(ExtraLightGray),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray, modifier = Modifier.size(size * 0.46f))
    }
}

@Composable
private fun OrderDetails(
    name: String,
    folio: String,
    price: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            color = DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = folio,
            color = MediumGray,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(price, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = PrimaryOrange)
    }
}

@Composable
private fun OrderStatusBadge(status: String, color: Color) {
    val animatedColor by animateColorAsState(targetValue = color, label = "order_status_color")
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = animatedColor.copy(alpha = 0.13f)
    ) {
        Text(
            text = status,
            color = animatedColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun OrderStatusAndArrow(status: String, statusColor: Color) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        OrderStatusBadge(status, statusColor)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Ver detalle de la orden",
            tint = MediumGray
        )
    }
}

@Composable
private fun OrdersEmptyState(isHistory: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(modifier = Modifier.size(84.dp), shape = CircleShape, color = SoftOrangeBg) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Fastfood, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(38.dp))
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = if (isHistory) "Aún no hay órdenes en tu historial" else "Aún no tienes órdenes activas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkGray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isHistory) "Tus pedidos completados aparecerán aquí." else "Tus pedidos aparecerán aquí cuando confirmes una compra.",
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingModal(
    pedido: Pedido,
    onDismiss: () -> Unit,
    onRatingSubmitted: () -> Unit
) {
    val windowSize = LocalWindowSize.current
    val haptic = LocalHapticFeedback.current
    var productRatings by remember { mutableStateOf(mapOf<String, Int>()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val productoRepo = remember { ProductoRepository() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val modalMaxHeight = (windowSize.height * 0.82f).coerceAtMost(720.dp)

    val productosDeLaOrden = pedido.productos

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        if (showSuccessMessage) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = modalMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = windowSize.compactDp(24))
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("¡Gracias por calificar!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRatingSubmitted()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Aceptar", color = Color.White)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = modalMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = windowSize.compactDp(24))
                    .padding(bottom = 32.dp),
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
                                    modifier = Modifier.size(windowSize.compactDp(36)).clickable {
                                        if (!isSubmitting) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
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
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                Text(
                    "Cancelar",
                    fontWeight = FontWeight.Bold,
                    color = MediumGray,
                    modifier = Modifier.clickable {
                        if (!isSubmitting) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}