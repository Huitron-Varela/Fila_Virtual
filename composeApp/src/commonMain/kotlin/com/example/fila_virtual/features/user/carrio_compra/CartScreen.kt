package com.example.fila_virtual.features.user.carrio_compra

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.data.TarjetaGuardada
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*
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
    val padding = windowSize.adaptiveDp(16).value.dp

    val cartItems by viewModel.carrito.collectAsState()
    val subtotal = viewModel.calcularTotalCarrito()
    val tarifaServicio = if (cartItems.isEmpty()) 0.0 else 5.0
    val total = subtotal + tarifaServicio

    val usuario = viewModel.usuario
    val tarjetasGuardadas = usuario?.metodosPago ?: emptyList()
    val mercadoPagoVinculado = usuario?.mercadoPagoVinculado ?: false

    var showPaymentModal by remember { mutableStateOf(false) }
    var tarjetaSeleccionada by remember { mutableStateOf<TarjetaGuardada?>(null) }
    var isGenerandoLink by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(tarjetasGuardadas) {
        if (tarjetaSeleccionada == null && tarjetasGuardadas.isNotEmpty()) {
            tarjetaSeleccionada = tarjetasGuardadas.first()
        }
    }

    LaunchedEffect(cartItems.isEmpty()) {
        if (cartItems.isEmpty()) {
            viewModel.isWaitingForPayment = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CartTopBar(onBackClick = onBackClick) },
        bottomBar = {
            Column {

                if (viewModel.errorMessage.isNotEmpty() && !viewModel.isWaitingForPayment) {
                    Text(
                        text = viewModel.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        textAlign = TextAlign.Center
                    )
                }

                if (viewModel.isWaitingForPayment) {
                    // 🔥 UI KMP-Friendly con botón de Cancelar explícito
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Button(
                            onClick = {
                                viewModel.procesarCompraDelCarrito(
                                    establecimientoId = "local_prueba_123",
                                    establecimientoNombre = "AlToque Food",
                                    onSuccess = {
                                        viewModel.isWaitingForPayment = false
                                        onOrderSuccess()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Text("✅ Confirmar Pago Realizado", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Botón de escape sin usar el BackHandler de Android
                        OutlinedButton(
                            onClick = {
                                viewModel.isWaitingForPayment = false
                                viewModel.errorMessage = "Pago cancelado. Intenta nuevamente."
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancelar / Hubo un problema", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                else {
                    if (cartItems.isNotEmpty()) {

                        if (tarjetasGuardadas.isNotEmpty() && tarjetaSeleccionada != null) {
                            CartBottomBar(
                                isProcessing = viewModel.isLoading,
                                errorMessage = "",
                                isEnabled = cartItems.isNotEmpty() && !isGenerandoLink,
                                onPayClick = {
                                    if (viewModel.isLoading) return@CartBottomBar
                                    viewModel.procesarCompraDelCarrito(
                                        establecimientoId = "local_prueba_123",
                                        establecimientoNombre = "AlToque Food",
                                        onSuccess = { onOrderSuccess() }
                                    )
                                }
                            )
                        } else if (!mercadoPagoVinculado) {
                            Button(
                                onClick = { onNavigateToWallet() },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(56.dp),
                                enabled = cartItems.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Configurar Billetera para Pagar", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (mercadoPagoVinculado) {
                            if (tarjetasGuardadas.isNotEmpty() && tarjetaSeleccionada != null) {
                                Text("O paga online con:", modifier = Modifier.fillMaxWidth().padding(top = 8.dp), textAlign = TextAlign.Center, color = MediumGray, style = MaterialTheme.typography.bodySmall)
                            }

                            Button(
                                onClick = {
                                    if (isGenerandoLink) return@Button

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
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 16.dp).height(56.dp),
                                enabled = !isGenerandoLink,
                                colors = ButtonDefaults.buttonColors(containerColor = MPBlue)
                            ) {
                                if (isGenerandoLink) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                } else {
                                    Text("Pagar con Mercado Pago", fontWeight = FontWeight.Bold, color = Color.White)
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
            verticalArrangement = Arrangement.spacedBy(windowSize.adaptiveDp(24).value.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            if (viewModel.isWaitingForPayment) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                        Text(
                            text = "Completa tu pago en el navegador. Si regresaste sin pagar, presiona 'Cancelar' debajo.",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            } else {
                item { InfoBanner() }
            }

            item {
                Text(
                    text = "Tu Pedido",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = windowSize.adaptiveSp(18)
                    ),
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (cartItems.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(Res.string.cart_empty), color = MediumGray)
                    }
                } else {
                    CartItemsList(
                        items = cartItems,
                        onIncrement = { idProducto -> viewModel.incrementarCantidad(idProducto) },
                        onDecrement = { idProducto -> viewModel.decrementarCantidad(idProducto) }
                    )
                }
            }

            item {
                if (tarjetasGuardadas.isNotEmpty() || !mercadoPagoVinculado) {
                    PaymentMethodSection(
                        tarjeta = tarjetaSeleccionada,
                        onEditClick = { showPaymentModal = true }
                    )
                }
            }

            item { SummarySection(subtotal = subtotal, tarifa = tarifaServicio, total = total) }
            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showPaymentModal) {
        SelectorTarjetasModal(
            tarjetas = tarjetasGuardadas,
            tarjetaActual = tarjetaSeleccionada,
            onTarjetaSelected = { tarjetaElegida ->
                tarjetaSeleccionada = tarjetaElegida
                showPaymentModal = false
            },
            onAddNewCardClick = {
                showPaymentModal = false
                onNavigateToWallet()
            },
            onDismiss = { showPaymentModal = false }
        )
    }
}