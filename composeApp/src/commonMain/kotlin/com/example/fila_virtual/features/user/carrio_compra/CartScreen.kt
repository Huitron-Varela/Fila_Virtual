package com.example.fila_virtual.features.user.carrio_compra

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.data.TarjetaGuardada
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*

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

    var showPaymentModal by remember { mutableStateOf(false) }
    var tarjetaSeleccionada by remember { mutableStateOf<TarjetaGuardada?>(null) }

    LaunchedEffect(tarjetasGuardadas) {
        if (tarjetaSeleccionada == null && tarjetasGuardadas.isNotEmpty()) {
            tarjetaSeleccionada = tarjetasGuardadas.first()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { CartTopBar(onBackClick = onBackClick) },
        bottomBar = {
            CartBottomBar(
                isProcessing = viewModel.isLoading,
                errorMessage = viewModel.errorMessage,
                isEnabled = cartItems.isNotEmpty() && tarjetaSeleccionada != null,
                onPayClick = {
                    if (viewModel.isLoading) return@CartBottomBar
                    viewModel.procesarCompraDelCarrito(
                        establecimientoId = "local_prueba_123",
                        establecimientoNombre = "AlToque Food",
                        tarjetaSeleccionada = tarjetaSeleccionada,
                        onSuccess = { onOrderSuccess() }
                    )
                }
            )
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
            item { InfoBanner() }
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
                    // 🔥 PASAMOS LAS FUNCIONES DE LOS BOTONES 🔥
                    CartItemsList(
                        items = cartItems,
                        onIncrement = { idProducto -> viewModel.incrementarCantidad(idProducto) },
                        onDecrement = { idProducto -> viewModel.decrementarCantidad(idProducto) }
                    )
                }
            }

            item {
                PaymentMethodSection(
                    tarjeta = tarjetaSeleccionada,
                    onEditClick = { showPaymentModal = true }
                )
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