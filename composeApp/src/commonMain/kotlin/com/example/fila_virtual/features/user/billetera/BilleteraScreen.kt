package com.example.fila_virtual.features.user.billetera

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.TarjetaGuardada
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val MPBlue = Color(0xFF009EE3)
private val LightBlueBg = Color(0xFFE1F5FE)
private val OrangeGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFE94E1B),
        Color(0xFFF26522)
    )
)

enum class BottomSheetStateView {
    SELECCION_METODO,
    FORMULARIO_TARJETA,
    MOCK_CARGANDO_MP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BilleteraScreen(
    viewModel: UserViewModel
) {
    val windowSize = LocalWindowSize.current
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val usuario = viewModel.usuario
    val metodosPago = usuario?.metodosPago ?: emptyList()
    val mercadoPagoVinculado = usuario?.mercadoPagoVinculado ?: false

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var currentSheetView by rememberSaveable { mutableStateOf(BottomSheetStateView.SELECCION_METODO) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tarjetaVinculadaSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var tarjetaAEliminar by remember { mutableStateOf<TarjetaGuardada?>(null) }

    var showProximamente by rememberSaveable { mutableStateOf(false) }
    var showTarjetaVinculadaModal by rememberSaveable { mutableStateOf(false) }
    var showMPVinculadoModal by rememberSaveable { mutableStateOf(false) }

    val pendingAction = viewModel.pendingWalletAction

    LaunchedEffect(pendingAction) {
        if (pendingAction == "abrir_formulario") {
            currentSheetView = BottomSheetStateView.FORMULARIO_TARJETA
            showBottomSheet = true
            viewModel.clearPendingWalletAction()
        }
    }

    LaunchedEffect(showBottomSheet) {
        if (!showBottomSheet) {
            showProximamente = false
            viewModel.clearWalletMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.wallet_payment_methods_title),
                    style = typography.titleLarge.copy(fontSize = windowSize.adaptiveSp(20), fontWeight = FontWeight.Bold),
                    color = colorScheme.onBackground
                )
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                // =======================================================
                // 1. MIS TARJETAS (ARRIBA COMO LO TENÍAS ORIGINALMENTE)
                // =======================================================
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MIS TARJETAS",
                        style = typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                        color = MediumGray
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier.background(Color(0xFFFFF0E6), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${metodosPago.size} activas",
                            color = PrimaryOrange,
                            style = typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (metodosPago.isNotEmpty()) {
                        Text(
                            text = "Desliza para ver",
                            style = typography.labelSmall,
                            color = MediumGray
                        )
                    }
                }

                if (metodosPago.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.wallet_no_linked_cards),
                        style = typography.bodyMedium,
                        color = MediumGray,
                        modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(metodosPago) { tarjeta ->
                            Box(modifier = Modifier.fillParentMaxWidth(0.85f)) {
                                CreditCardView(
                                    cardNumber = tarjeta.ultimos4,
                                    cardHolder = tarjeta.nombreTitular,
                                    expiryDate = tarjeta.expiracion,
                                    cardBrand = tarjeta.marca,
                                    onDelete = { tarjetaAEliminar = tarjeta }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // =======================================================
                // 2. BILLETERAS DIGITALES (MERCADO PAGO ABAJO)
                // =======================================================
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "BILLETERAS DIGITALES",
                        style = typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                        color = MediumGray
                    )

                    Surface(shape = RoundedCornerShape(12.dp), color = LightBlueBg) {
                        Text(
                            text = if (mercadoPagoVinculado) "ACTIVA" else "SIN VINCULAR",
                            color = MPBlue,
                            style = typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    LinkedMPView(
                        isLinked = mercadoPagoVinculado,
                        onDesvincular = { viewModel.toggleMercadoPagoVinculado(false) }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // BOTÓN DE AÑADIR MÉTODO
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        currentSheetView = BottomSheetStateView.SELECCION_METODO
                        showProximamente = false
                        showBottomSheet = true
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp).background(OrangeGradient, RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Añadir nuevo método de pago",
                            style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = windowSize.adaptiveSp(16)),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = "Seguridad", tint = MediumGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Los datos sensibles se validan antes de guardar el método de pago",
                        color = MediumGray,
                        style = typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                containerColor = colorScheme.surface
            ) {
                when (currentSheetView) {
                    BottomSheetStateView.SELECCION_METODO -> {
                        AddMPMethodContent(
                            onAddCard = {
                                showProximamente = false
                                currentSheetView = BottomSheetStateView.FORMULARIO_TARJETA
                            },
                            onConnectMP = {
                                currentSheetView = BottomSheetStateView.MOCK_CARGANDO_MP
                            },
                            isMPAlreadyLinked = mercadoPagoVinculado
                        )
                    }
                    BottomSheetStateView.FORMULARIO_TARJETA -> {
                        FormularioTarjetaScreen(
                            viewModel = viewModel,
                            onBack = {
                                viewModel.clearWalletMessage()
                                currentSheetView = BottomSheetStateView.SELECCION_METODO
                            },
                            onSuccess = {
                                coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                    showBottomSheet = false
                                    showTarjetaVinculadaModal = true
                                }
                            }
                        )
                    }
                    BottomSheetStateView.MOCK_CARGANDO_MP -> {
                        MockConectandoMercadoPagoScreen {
                            viewModel.toggleMercadoPagoVinculado(true)
                            coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                                showBottomSheet = false
                                showMPVinculadoModal = true
                            }
                        }
                    }
                }
            }
        }

        // ==========================================================
        // 🔥 MODAL ÉXITO MERCADO PAGO VINCULADO (COLOR ALTOQUE)
        // ==========================================================
        if (showMPVinculadoModal) {
            ModalBottomSheet(
                onDismissRequest = { showMPVinculadoModal = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Palomita VERDE
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("¡Cuenta vinculada!", style = typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tu cuenta de Mercado Pago está lista para usarse.", style = typography.bodyMedium, color = MediumGray, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón NARANJA ALTOQUE
                    Button(
                        onClick = { showMPVinculadoModal = false },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showTarjetaVinculadaModal) {
            val ultimaTarjeta = viewModel.usuario?.metodosPago?.lastOrNull()
            ModalBottomSheet(
                onDismissRequest = {
                    showTarjetaVinculadaModal = false
                    viewModel.clearWalletMessage()
                },
                sheetState = tarjetaVinculadaSheetState,
                containerColor = colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().fillMaxHeight(0.48f).padding(horizontal = 24.dp).padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Tarjeta vinculada", style = typography.titleLarge.copy(fontWeight = FontWeight.Bold), textAlign = TextAlign.Center, color = colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Tu tarjeta fue validada correctamente y se agregó a tu Wallet.", style = typography.bodyMedium, color = MediumGray, textAlign = TextAlign.Center)

                    if (ultimaTarjeta != null) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFE8F5E9)) {
                            Row(modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "${ultimaTarjeta.marca} •••• ${ultimaTarjeta.ultimos4}", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch { tarjetaVinculadaSheetState.hide() }.invokeOnCompletion {
                                showTarjetaVinculadaModal = false
                                viewModel.clearWalletMessage()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Aceptar", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (tarjetaAEliminar != null) {
            AlertDialog(
                onDismissRequest = { tarjetaAEliminar = null },
                title = { Text(text = "Eliminar Tarjeta", style = typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                text = { Text(text = "¿Estás seguro que deseas desvincular la tarjeta terminada en ${tarjetaAEliminar?.ultimos4}? Esta acción no se puede deshacer.", style = typography.bodyMedium) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            tarjetaAEliminar?.let { tarjeta -> viewModel.eliminarTarjeta(tarjeta.ultimos4) }
                            tarjetaAEliminar = null
                        }
                    ) {
                        Text(text = "Eliminar", color = colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { tarjetaAEliminar = null }) {
                        Text(text = "Cancelar", color = MediumGray)
                    }
                },
                containerColor = colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun LinkedMPView(isLinked: Boolean, onDesvincular: () -> Unit) {
    val windowSize = LocalWindowSize.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if(isLinked) MPBlue else LightBlueBg),
        elevation = CardDefaults.cardElevation(if(isLinked) 8.dp else 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(if(isLinked) Color.White else MPBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = if(isLinked) MPBlue else Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Mercado Pago",
                            color = if(isLinked) Color.White else MPBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = windowSize.adaptiveSp(18)
                        )
                        Text(
                            text = if(isLinked) "Conectado y listo para usar" else "No configurado",
                            color = if(isLinked) Color.White.copy(alpha = 0.8f) else MPBlue.copy(alpha = 0.7f),
                            fontSize = windowSize.adaptiveSp(14)
                        )
                    }
                }
                Icon(if(isLinked) Icons.Default.CheckCircle else Icons.Default.Schedule, contentDescription = null, tint = if(isLinked) Color.White else MPBlue)
            }

            if (isLinked) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onDesvincular,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                ) {
                    Text("Desvincular cuenta", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MockConectandoMercadoPagoScreen(onSimulacionCompletada: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000)
        onSimulacionCompletada()
    }

    Column(
        modifier = Modifier.fillMaxWidth().height(250.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = MPBlue, modifier = Modifier.size(48.dp), strokeWidth = 4.dp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Conectando con Mercado Pago...",
            style = MaterialTheme.typography.titleMedium,
            color = DarkGray
        )
        Text(
            text = "Redirigiendo de forma segura",
            style = MaterialTheme.typography.bodyMedium,
            color = MediumGray
        )
    }
}

@Composable
fun AddMPMethodContent(
    onAddCard: () -> Unit,
    onConnectMP: () -> Unit,
    isMPAlreadyLinked: Boolean
) {
    val windowSize = LocalWindowSize.current
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 48.dp, top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "¿Cómo quieres pagar?",
            style = typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = windowSize.adaptiveSp(20)),
            modifier = Modifier.padding(bottom = 24.dp),
            color = colorScheme.onSurface
        )

        if (!isMPAlreadyLinked) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onConnectMP() },
                colors = CardDefaults.cardColors(containerColor = LightBlueBg),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(colorScheme.surface), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Bolt, null, tint = MPBlue)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Mercado Pago", style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = windowSize.adaptiveSp(16)), color = MPBlue)
                        Text("Vincular cuenta de Mercado Pago", style = typography.bodySmall, color = MPBlue.copy(alpha = 0.7f))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth().clickable { onAddCard() },
            colors = CardDefaults.cardColors(containerColor = colorScheme.background),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(colorScheme.surface), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.CreditCard, null, tint = MediumGray)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(stringResource(Res.string.wallet_new_card), style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium, fontSize = windowSize.adaptiveSp(16)), color = colorScheme.onSurface)
                    Text("Validar y guardar tarjeta", style = typography.bodySmall, color = MediumGray)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CreditCardView(
    cardNumber: String,
    cardHolder: String,
    expiryDate: String,
    cardBrand: String,
    onDelete: () -> Unit
) {
    val cardGradient =
        when (cardBrand.uppercase()) {
            "VISA" -> Brush.horizontalGradient(listOf(Color(0xFF1434CB), Color(0xFF0B195E)))
            "MASTERCARD" -> Brush.horizontalGradient(listOf(Color(0xFF141414), Color(0xFF2B2B2B)))
            "AMEX" -> Brush.horizontalGradient(listOf(Color(0xFF007BC1), Color(0xFF005696)))
            else -> Brush.horizontalGradient(listOf(Color(0xFF424242), Color(0xFF212121)))
        }

    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.58f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(cardGradient)) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    when (cardBrand.uppercase()) {
                        "MASTERCARD" -> {
                            Box(modifier = Modifier.size(width = 46.dp, height = 30.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(color = Color(0xCCEB001B), radius = size.height / 2, center = Offset(size.width * 0.35f, size.height / 2))
                                    drawCircle(color = Color(0xCCF79E1B), radius = size.height / 2, center = Offset(size.width * 0.65f, size.height / 2))
                                }
                            }
                        }
                        "VISA" -> {
                            Text(text = "VISA", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, fontStyle = FontStyle.Italic, letterSpacing = (-1).sp)
                        }
                        "AMEX" -> {
                            Box(modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(text = "AMEX", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.sp)
                            }
                        }
                        else -> {
                            Icon(Icons.Default.CreditCard, contentDescription = "Tarjeta", tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(width = 36.dp, height = 26.dp).background(Color(0xFFD4AF37), RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = Color.White.copy(alpha = 0.8f))
                        }
                    }
                }

                Text(text = "••••  ••••  ••••  $cardNumber", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp, letterSpacing = 3.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text(text = "CARDHOLDER", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = cardHolder.uppercase().ifBlank { "—" }, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "EXPIRES", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = expiryDate.ifBlank { "—" }, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}