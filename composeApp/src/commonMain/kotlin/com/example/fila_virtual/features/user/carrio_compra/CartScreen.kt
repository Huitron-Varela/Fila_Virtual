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

    // 🔥 ESTADO CLAVE: Controla si estamos esperando que regrese del navegador
    var isWaitingForPayment by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(tarjetasGuardadas) {
        if (tarjetaSeleccionada == null && tarjetasGuardadas.isNotEmpty()) {
            tarjetaSeleccionada = tarjetasGuardadas.first()
        }
    }

    // Si el carrito se vacía (por cancelar la orden, etc), reiniciamos el estado
    LaunchedEffect(cartItems.isEmpty()) {
        if (cartItems.isEmpty()) {
            isWaitingForPayment = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CartTopBar(onBackClick = onBackClick) },
        bottomBar = {
            Column {

                // MOSTRAR ERROR DE MERCADO PAGO SI OCURRE ALGUNO
                if (viewModel.errorMessage.isNotEmpty() && mercadoPagoVinculado) {
                    Text(
                        text = "Error: ${viewModel.errorMessage}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                if (mercadoPagoVinculado && cartItems.isNotEmpty()) {

                    if (isWaitingForPayment) {
                        // 🔥 BOTÓN VERDE CUANDO REGRESA DE MERCADO PAGO
                        Button(
                            onClick = {
                                viewModel.procesarCompraDelCarrito(
                                    establecimientoId = "local_prueba_123",
                                    establecimientoNombre = "AlToque Food",
                                    onSuccess = {
                                        isWaitingForPayment = false
                                        onOrderSuccess()
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 4.dp).height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Verde Éxito
                        ) {
                            Text("✅ Confirmar Pago Realizado", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // 🔥 BOTÓN AZUL ORIGINAL PARA IR A PAGAR
                        Button(
                            onClick = {
                                if (isGenerandoLink) return@Button

                                isGenerandoLink = true
                                viewModel.clearWalletMessage() // Limpia errores previos
                                scope.launch {
                                    val linkPago = viewModel.generarLinkMercadoPago()
                                    isGenerandoLink = false

                                    if (linkPago != null) {
                                        // Abre el link y cambia la UI para esperar el regreso
                                        uriHandler.openUri(linkPago)
                                        isWaitingForPayment = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 4.dp).height(56.dp),
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

                if (tarjetasGuardadas.isNotEmpty() && !mercadoPagoVinculado) {
                    CartBottomBar(
                        isProcessing = viewModel.isLoading,
                        errorMessage = viewModel.errorMessage,
                        isEnabled = cartItems.isNotEmpty() && tarjetaSeleccionada != null,
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
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Button(
                            onClick = { onNavigateToWallet() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = cartItems.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Configurar Billetera para Pagar", fontWeight = FontWeight.Bold)
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

            if (isWaitingForPayment) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                        Text(
                            text = "Por favor, completa tu pago en la pestaña del navegador. Cuando termines, regresa aquí y presiona 'Confirmar Pago Realizado'.",
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