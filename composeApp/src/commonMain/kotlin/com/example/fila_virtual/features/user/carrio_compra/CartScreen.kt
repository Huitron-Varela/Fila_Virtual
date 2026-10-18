package com.example.fila_virtual.features.user.carrio_compra

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.AppSnackbarHost
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.features.user.UserHomeViewModel
import com.example.fila_virtual.data.TarjetaGuardada
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.btn_cancel
import fila_virtual.composeapp.generated.resources.cart_add_more_products
import fila_virtual.composeapp.generated.resources.cart_add_more_products_at
import fila_virtual.composeapp.generated.resources.cart_clear_action
import fila_virtual.composeapp.generated.resources.cart_clear_confirmation
import fila_virtual.composeapp.generated.resources.cart_clear_title
import fila_virtual.composeapp.generated.resources.cart_get_ticket
import fila_virtual.composeapp.generated.resources.cart_pay_button
import fila_virtual.composeapp.generated.resources.cart_payment_confirm_title
import fila_virtual.composeapp.generated.resources.cart_payment_confirm_question
import fila_virtual.composeapp.generated.resources.cart_payment_checkout_note
import fila_virtual.composeapp.generated.resources.cart_payment_saved_card_note
import fila_virtual.composeapp.generated.resources.cart_payment_continue
import fila_virtual.composeapp.generated.resources.cart_payment_review
import fila_virtual.composeapp.generated.resources.cart_payment_selected
import fila_virtual.composeapp.generated.resources.cart_your_order
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: UserViewModel
) {
    val windowSize = LocalWindowSize.current
    val padding = windowSize.compactDp(16)

    val cartItems by viewModel.carrito.collectAsState()
    val total = viewModel.calcularTotalCarrito()
    val recommendationsViewModel = remember { UserHomeViewModel() }
    val quickRecommendations by recommendationsViewModel.recomendaciones.collectAsState()

    var isGenerandoLink by remember { mutableStateOf(false) }
    var mostrarConfirmacionPago by remember { mutableStateOf(false) }
    var mostrarConfirmacionVaciar by remember { mutableStateOf(false) }
    var selectedCardKey by rememberSaveable { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    val tarjetas = viewModel.usuario?.metodosPago.orEmpty()
    val tarjetaSeleccionada = tarjetas.firstOrNull { it.selectorKey() == selectedCardKey }

    LaunchedEffect(tarjetas, selectedCardKey) {
        if (selectedCardKey.isNotBlank() && tarjetaSeleccionada == null) selectedCardKey = ""
    }

    val continuarACheckout: () -> Unit = {
        if (!isGenerandoLink) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            isGenerandoLink = true
            viewModel.clearWalletMessage()
            scope.launch {
                val linkPago = viewModel.generarLinkMercadoPago()
                isGenerandoLink = false
                if (linkPago != null) {
                    uriHandler.openUri(linkPago)
                    viewModel.isWaitingForPayment = true
                }
            }
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        if (viewModel.errorMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(viewModel.errorMessage)
        }
    }

    LaunchedEffect(cartItems.isEmpty()) {
        if (cartItems.isEmpty()) {
            viewModel.isWaitingForPayment = false
        }
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            CartTopBar(
                onBackClick = onBackClick,
                onClearClick = { mostrarConfirmacionVaciar = true },
                canClearCart = cartItems.isNotEmpty()
            )
        },
        snackbarHost = {
            AppSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        },
        bottomBar = {
            if (viewModel.isWaitingForPayment) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = padding, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.confirmarPedidoDelCarrito(
                                    onSuccess = {
                                        viewModel.isWaitingForPayment = false
                                        onOrderSuccess()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TrafficGreen)
                        ) {
                            Text("Confirmar pago", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.isWaitingForPayment = false
                                viewModel.errorMessage = "Pago cancelado. Intenta nuevamente."
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar pago", fontWeight = FontWeight.SemiBold)
                        }
                }
            } else if (cartItems.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LightBackground)
                            .padding(horizontal = padding, vertical = 10.dp)
                    ) {
                        Button(
                            onClick = { if (!isGenerandoLink) mostrarConfirmacionPago = true },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            enabled = !isGenerandoLink,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                        ) {
                            if (isGenerandoLink) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = "${stringResource(Res.string.cart_pay_button)} ${total.formatoMoneda()}",
                                        modifier = Modifier.weight(1f),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Surface(
                                        color = Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.cart_get_ticket),
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1
                                            )
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            if (cartItems.isEmpty()) {
                item {
                    EmptyCartState(
                        recommendations = quickRecommendations.take(3),
                        onBrowseClick = onBackClick,
                        onAddRecommendation = { producto, establecimientoNombre, addedMessage ->
                            viewModel.agregarAlCarrito(
                                idProducto = producto.id,
                                nombre = producto.nombre,
                                precio = producto.precio,
                                establecimientoId = producto.establecimientoId,
                                establecimientoNombre = establecimientoNombre,
                                imagenUrl = producto.imagenUrl,
                                descripcion = producto.descripcion
                            )
                            scope.launch {
                                snackbarHostState.showSnackbar(addedMessage)
                            }
                        }
                    )
                }
            } else {
                item {
                    if (viewModel.isWaitingForPayment) {
                        PaymentWaitingBanner()
                    } else {
                        InfoBanner()
                    }
                }
                item {
                    CartSectionHeading(itemCount = cartItems.sumOf { it.cantidad })
                    Spacer(modifier = Modifier.height(10.dp))
                    CartItemsList(
                        items = cartItems,
                        onIncrement = { idProducto -> viewModel.incrementarCantidad(idProducto) },
                        onDecrement = { idProducto -> viewModel.decrementarCantidad(idProducto) }
                    )
                }
                item {
                    Surface(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = SoftOrangeBg.copy(alpha = 0.28f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(Modifier.matchParentSize()) {
                                val lineWidth = 1.2.dp.toPx()
                                drawRoundRect(
                                    color = PrimaryOrange.copy(alpha = 0.55f),
                                    cornerRadius = CornerRadius(16.dp.toPx()),
                                    style = Stroke(
                                        width = lineWidth,
                                        pathEffect = PathEffect.dashPathEffect(
                                            floatArrayOf(6.dp.toPx(), 4.dp.toPx())
                                        )
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier.size(26.dp).border(1.5.dp, PrimaryOrange, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(17.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (viewModel.carritoEstablecimientoNombre.isBlank()) {
                                        stringResource(Res.string.cart_add_more_products)
                                    } else {
                                        stringResource(Res.string.cart_add_more_products_at, viewModel.carritoEstablecimientoNombre)
                                    },
                                    modifier = Modifier.widthIn(max = 280.dp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryOrange,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
                item {
                    PaymentMethodSection(
                        tarjetas = tarjetas,
                        tarjetaSeleccionada = tarjetaSeleccionada,
                        onSelectCard = { tarjeta -> selectedCardKey = tarjeta?.selectorKey().orEmpty() }
                    )
                }
                item { SummarySection(items = cartItems) }
            }
        }
    }

    if (mostrarConfirmacionPago && cartItems.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionPago = false },
            title = { Text(stringResource(Res.string.cart_payment_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(Res.string.cart_payment_confirm_question))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SoftOrangeBg),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (tarjetaSeleccionada == null) Icons.Default.AccountBalanceWallet else Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = if (tarjetaSeleccionada == null) Color(0xFF009EE3) else PrimaryOrange
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    stringResource(Res.string.cart_payment_selected),
                                    color = MediumGray,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    if (tarjetaSeleccionada == null) "Mercado Pago"
                                    else "${tarjetaSeleccionada.marca} •••• ${tarjetaSeleccionada.ultimos4}",
                                    color = DarkGray,
                                    fontWeight = FontWeight.Bold
                                )
                                if (tarjetaSeleccionada != null) {
                                    Text(
                                        "Vence ${tarjetaSeleccionada.expiracion}",
                                        color = MediumGray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryOrange)
                        }
                    }
                    Text(
                        stringResource(
                            if (tarjetaSeleccionada == null) Res.string.cart_payment_checkout_note
                            else Res.string.cart_payment_saved_card_note
                        ),
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacionPago = false
                        continuarACheckout()
                    },
                    enabled = !isGenerandoLink,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text(stringResource(Res.string.cart_payment_continue), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionPago = false }) {
                    Text(stringResource(Res.string.cart_payment_review), color = DarkGray)
                }
            }
        )
    }

    if (mostrarConfirmacionVaciar && cartItems.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacionVaciar = false },
            title = { Text(stringResource(Res.string.cart_clear_title)) },
            text = { Text(stringResource(Res.string.cart_clear_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.vaciarCarrito()
                        viewModel.isWaitingForPayment = false
                        selectedCardKey = ""
                        mostrarConfirmacionVaciar = false
                    }
                ) {
                    Text(stringResource(Res.string.cart_clear_action), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacionVaciar = false }) {
                    Text(stringResource(Res.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
private fun CartSectionHeading(itemCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(Res.string.cart_your_order),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1
        )
        Surface(
            color = Color(0xFFFFF0E6),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "$itemCount artículos",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                color = PrimaryOrange,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PaymentWaitingBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = "Completa el pago en el navegador. Al regresar, confirma si el pago se realizó o cancélalo.",
            modifier = Modifier.padding(16.dp),
            color = Color(0xFFE65100),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}