package com.example.fila_virtual.features.user.carrio_compra

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.AppSnackbarHost
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.features.user.UserViewModel
import kotlinx.coroutines.launch

private val MPBlue = Color(0xFF009EE3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onOrderSuccess: () -> Unit,
    onNavigateToWallet: () -> Unit,
    viewModel: UserViewModel
) {
    val windowSize = LocalWindowSize.current
    val padding = windowSize.compactDp(16)

    val cartItems by viewModel.carrito.collectAsState()
    val subtotal = viewModel.calcularTotalCarrito()
    val tarifaServicio = if (cartItems.isEmpty()) 0.0 else 5.0
    val total = subtotal + tarifaServicio

    var isGenerandoLink by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }

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
        topBar = { CartTopBar(onBackClick = onBackClick) },
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
                Button(
                    onClick = {
                        if (isGenerandoLink) return@Button

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
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = padding, vertical = 12.dp)
                        .height(54.dp),
                    enabled = !isGenerandoLink,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MPBlue)
                ) {
                    if (isGenerandoLink) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Continuar con Mercado Pago", fontWeight = FontWeight.Bold, color = Color.White)
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
                    EmptyCartState(onBrowseClick = onBackClick)
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

                item { SummarySection(subtotal = subtotal, tarifa = tarifaServicio, total = total) }

                item {
                    PaymentMethodSection(
                        tarjetas = viewModel.usuario?.metodosPago.orEmpty(),
                        onEditClick = onNavigateToWallet
                    )
                }
            }
        }
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
            text = "Tu pedido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
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