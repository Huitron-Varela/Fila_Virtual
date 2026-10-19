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
import androidx.compose.ui.window.Dialog

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
import com.example.fila_virtual.components.ActiveOrderCard
import com.example.fila_virtual.components.HistoryOrderCard
import com.example.fila_virtual.components.OrderRatingSheet
import com.example.fila_virtual.components.OrdersSelector
import com.example.fila_virtual.components.Heders.StoreHeader

import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.codigoVisible
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.repository.ProductoRepository
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.orders_reorder_confirmation
import fila_virtual.composeapp.generated.resources.orders_reorder_title
import fila_virtual.composeapp.generated.resources.btn_cancel
import org.jetbrains.compose.resources.stringResource

import androidx.compose.foundation.Image
import fila_virtual.composeapp.generated.resources.orders_reorder
import fila_virtual.composeapp.generated.resources.orders_reorder_confirm_action
import fila_virtual.composeapp.generated.resources.orders_reorder_confirm_title
import fila_virtual.composeapp.generated.resources.orders_reorder_done
import fila_virtual.composeapp.generated.resources.orders_reorder_review
import fila_virtual.composeapp.generated.resources.orders_reorder_success
import fila_virtual.composeapp.generated.resources.orders_reorder_success_title
import fila_virtual.composeapp.generated.resources.orders_title
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
        EstadoPedido.NO_RECLAMADO -> "Incidencia"
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
    userViewModel: UserViewModel,
    viewModel: OrdenesViewModel = viewModel()
) {
    val windowSize = LocalWindowSize.current
    val haptic = LocalHapticFeedback.current
    val horizontalPadding = windowSize.compactDp(20)
    var selectedTabIndex by remember { mutableStateOf(0) }

    val pedidosActivos by viewModel.pedidosActivos.collectAsState()
    val pedidosHistorial by viewModel.pedidosHistorial.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showQRModal by remember { mutableStateOf(false) }
    var selectedPedidoForQR by remember { mutableStateOf<Pedido?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRatingModal by remember { mutableStateOf(false) }
    var selectedPedidoForRating by remember { mutableStateOf<Pedido?>(null) }
    var selectedPedidoForReorder by remember { mutableStateOf<Pedido?>(null) }
    var reorderAddedToCart by remember(selectedPedidoForReorder?.id) { mutableStateOf(false) }

    // 🔥 Memoria del ViewModel que sobrevive a la navegación
    val ratedOrders by viewModel.pedidosCalificados.collectAsState()
    val cartItems by userViewModel.carrito.collectAsState()
    val restoreOrderToCart: (Pedido) -> Unit = { pedido ->
        pedido.productos.forEach { producto ->
            repeat(producto.cantidad) {
                userViewModel.agregarAlCarrito(
                    idProducto = producto.idProducto,
                    nombre = producto.nombre,
                    precio = producto.precio,
                    establecimientoId = pedido.establecimientoId,
                    establecimientoNombre = pedido.establecimientoNombre,
                    imagenUrl = producto.imagenUrl,
                    descripcion = producto.descripcion
                )
            }
        }
    }

    AppPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = viewModel::refrescarPedidos,
        modifier = Modifier.fillMaxSize().background(LightBackground)
    ) {
    Column(modifier = Modifier.fillMaxSize()) {
        StoreHeader(
            title = stringResource(Res.string.orders_title),
            modifier = Modifier.padding(top = windowSize.compactDp(24), bottom = 16.dp),
            containerColor = LightBackground,
            titleStyle = MaterialTheme.typography.titleLarge.copy(fontSize = windowSize.adaptiveSp(20)),
            titleColor = MaterialTheme.colorScheme.onBackground,
            includeStatusBarPadding = false,
            minHeight = 0.dp,
            verticalPadding = 0.dp
        )

        OrdersSelector(
            selectedTab = selectedTabIndex,
            activeCount = pedidosActivos.size,
            historyCount = pedidosHistorial.size,
            onTabSelected = { selectedTabIndex = it },
            modifier = Modifier.padding(horizontal = horizontalPadding)
        )

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
                                ActiveOrderCard(
                                    pedido = pedido,
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
                                HistoryOrderCard(
                                    pedido = pedido,
                                    status = pedido.estado.toUIText(),
                                    statusColor = pedido.estado.toUIColor(),
                                    yaCalificado = ratedOrders.contains(pedido.id),
                                    onRateClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedPedidoForRating = pedido
                                        showRatingModal = true
                                    },
                                    canReorder = pedido.productos.isNotEmpty(),
                                    onReorderClick = {
                                        selectedPedidoForReorder = pedido
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
                        text = "Listo para recoger",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Muestra este QR al lector del mostrador. Si no lo lee, dicta el código de recogida.",
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

    selectedPedidoForReorder?.let { pedido ->
        val cambiaEstablecimiento = cartItems.isNotEmpty() &&
            userViewModel.carritoEstablecimientoId != pedido.establecimientoId
        val dismissReorderDialog = {
            selectedPedidoForReorder = null
            reorderAddedToCart = false
        }
        Dialog(onDismissRequest = dismissReorderDialog) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.94f).widthIn(max = 420.dp),
                shape = RoundedCornerShape(28.dp),
                color = LightSurface,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = windowSize.height * 0.88f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        color = if (reorderAddedToCart) Color(0xFFE8F5E9) else SoftOrangeBg
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (reorderAddedToCart) Icons.Default.ThumbUp else Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = if (reorderAddedToCart) Color(0xFF2E7D32) else PrimaryOrange,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(
                            if (reorderAddedToCart) Res.string.orders_reorder_success_title
                            else Res.string.orders_reorder_confirm_title
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (reorderAddedToCart) {
                        Text(
                            text = stringResource(Res.string.orders_reorder_success),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MediumGray,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = if (cambiaEstablecimiento) {
                                stringResource(
                                    Res.string.orders_reorder_confirmation,
                                    userViewModel.carritoEstablecimientoNombre,
                                    pedido.establecimientoNombre
                                )
                            } else {
                                stringResource(Res.string.orders_reorder_review)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MediumGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = ExtraLightGray
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .verticalScroll(rememberScrollState())
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                pedido.productos.forEach { producto ->
                                    Text(
                                        text = "${producto.cantidad}x ${producto.nombre}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = DarkGray
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            if (reorderAddedToCart) {
                                dismissReorderDialog()
                            } else {
                                if (cambiaEstablecimiento) userViewModel.vaciarCarrito()
                                restoreOrderToCart(pedido)
                                reorderAddedToCart = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Text(
                            stringResource(
                                if (reorderAddedToCart) Res.string.orders_reorder_done
                                else Res.string.orders_reorder_confirm_action
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (!reorderAddedToCart) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = dismissReorderDialog,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = stringResource(Res.string.btn_cancel),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
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
    var productRatings by remember(pedido.id) { mutableStateOf(mapOf<String, Int>()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val productoRepo = remember { ProductoRepository() }
    OrderRatingSheet(
        pedido = pedido,
        ratings = productRatings,
        isSubmitting = isSubmitting,
        hasSubmitted = showSuccessMessage,
        errorMessage = errorMessage,
        onRatingChanged = { productId, rating ->
            productRatings = productRatings + (productId to rating)
        },
        onSubmit = {
            isSubmitting = true
            errorMessage = null
            scope.launch {
                var allSuccess = true
                for ((productId, rating) in productRatings) {
                    if (productoRepo.calificarProducto(productId, rating).isFailure) {
                        allSuccess = false
                    }
                }
                isSubmitting = false
                if (allSuccess) {
                    showSuccessMessage = true
                } else {
                    errorMessage = "No se pudieron enviar todas las calificaciones."
                }
            }
        },
        onDismiss = onDismiss,
        onSuccessAcknowledged = onRatingSubmitted
    )
}