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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.components.Heders.StoreHeader
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.TarjetaGuardada
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val MPBlue = Color(0xFF009EE3)
private val LightBlueBg = Color(0xFFE1F5FE)
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
    val haptic = LocalHapticFeedback.current

    val usuario = viewModel.usuario
    val metodosPago = usuario?.metodosPago ?: emptyList()
    val mercadoPagoVinculado = usuario?.mercadoPagoVinculado ?: false

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var currentSheetView by rememberSaveable { mutableStateOf(BottomSheetStateView.SELECCION_METODO) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tarjetaVinculadaSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var tarjetaAEliminar by remember { mutableStateOf<TarjetaGuardada?>(null) }
    var tarjetaAEditar by remember { mutableStateOf<TarjetaGuardada?>(null) }
    var tarjetaConfirmada by remember { mutableStateOf<TarjetaGuardada?>(null) }
    var tarjetaFueActualizada by remember { mutableStateOf(false) }

    var showProximamente by rememberSaveable { mutableStateOf(false) }
    var showTarjetaVinculadaModal by rememberSaveable { mutableStateOf(false) }
    var showMPVinculadoModal by rememberSaveable { mutableStateOf(false) }

    // Alta de tarjetas deshabilitada temporalmente: no abrir el formulario desde acciones pendientes.
    // val pendingAction = viewModel.pendingWalletAction
    // LaunchedEffect(pendingAction) {
    //     if (pendingAction == "abrir_formulario") {
    //         tarjetaAEditar = null
    //         viewModel.resetWalletCardForm()
    //         currentSheetView = BottomSheetStateView.FORMULARIO_TARJETA
    //         showBottomSheet = true
    //         viewModel.clearPendingWalletAction()
    //     }
    // }

    LaunchedEffect(showBottomSheet) {
        if (!showBottomSheet) {
            showProximamente = false
            viewModel.clearWalletMessage()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp)) {

            StoreHeader(
                title = stringResource(Res.string.wallet_payment_methods_title),
                modifier = Modifier.padding(top = 24.dp, bottom = 16.dp),
                containerColor = colorScheme.background,
                titleStyle = typography.titleLarge.copy(fontSize = windowSize.adaptiveSp(20), fontWeight = FontWeight.Bold),
                titleColor = colorScheme.onBackground,
                includeStatusBarPadding = false,
                minHeight = 0.dp,
                verticalPadding = 0.dp
            )

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                // // =======================================================
                // // 1. MIS TARJETAS (ARRIBA COMO LO TENÍAS ORIGINALMENTE)
                // // =======================================================
                // Row(
                //     modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp),
                //     verticalAlignment = Alignment.CenterVertically
                // ) {
                //     Text(
                //         text = "MIS TARJETAS",
                //         style = typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp),
                //         color = MediumGray
                //     )

                //     Spacer(modifier = Modifier.width(8.dp))

                //     Box(
                //         modifier = Modifier.background(Color(0xFFFFF0E6), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                //     ) {
                //         Text(
                //             text = "${metodosPago.size} activas",
                //             color = PrimaryOrange,
                //             style = typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                //         )
                //     }

                //     Spacer(modifier = Modifier.weight(1f))

                //     if (metodosPago.isNotEmpty()) {
                //         Text(
                //             text = "Desliza para ver",
                //             style = typography.labelSmall,
                //             color = MediumGray
                //         )
                //     }
                // }

                // if (metodosPago.isEmpty()) {
                //     Text(
                //         text = stringResource(Res.string.wallet_no_linked_cards),
                //         style = typography.bodyMedium,
                //         color = MediumGray,
                //         modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)
                //     )
                // } else {
                //     LazyRow(
                //         contentPadding = PaddingValues(horizontal = 24.dp),
                //         horizontalArrangement = Arrangement.spacedBy(16.dp),
                //         modifier = Modifier.fillMaxWidth()
                //     ) {
                //         items(metodosPago) { tarjeta ->
                //             Column(
                //                 modifier = Modifier.fillParentMaxWidth(0.85f),
                //                 verticalArrangement = Arrangement.spacedBy(10.dp)
                //             ) {
                //                 CreditCardView(
                //                     cardNumber = tarjeta.ultimos4,
                //                     cardHolder = tarjeta.nombreTitular,
                //                     expiryDate = tarjeta.expiracion,
                //                     cardBrand = tarjeta.marca
                //                 )
                //                 Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                //                     OutlinedButton(
                //                         onClick = {
                //                             tarjetaAEditar = tarjeta
                //                             viewModel.resetWalletCardForm()
                //                             currentSheetView = BottomSheetStateView.FORMULARIO_TARJETA
                //                             showBottomSheet = true
                //                         },
                //                         modifier = Modifier.weight(1f).height(44.dp),
                //                         shape = RoundedCornerShape(12.dp),
                //                         border = androidx.compose.foundation.BorderStroke(1.dp, BorderGray.copy(alpha = 0.7f)),
                //                         contentPadding = PaddingValues(horizontal = 8.dp)
                //                     ) {
                //                         Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                //                         Spacer(modifier = Modifier.width(6.dp))
                //                         Text(stringResource(Res.string.wallet_edit_card), maxLines = 1)
                //                     }
                //                     OutlinedButton(
                //                         onClick = { tarjetaAEliminar = tarjeta },
                //                         modifier = Modifier.weight(1f).height(44.dp),
                //                         shape = RoundedCornerShape(12.dp),
                //                         border = androidx.compose.foundation.BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.55f)),
                //                         colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.error),
                //                         contentPadding = PaddingValues(horizontal = 8.dp)
                //                     ) {
                //                         Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                //                         Spacer(modifier = Modifier.width(6.dp))
                //                         Text(stringResource(Res.string.wallet_delete_card), maxLines = 1)
                //                     }
                //                 }
                //             }
                //         }
                //     }
                // }

                // Spacer(modifier = Modifier.height(40.dp))

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

                }

                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    LinkedMPView(
                        isLinked = mercadoPagoVinculado,
                        onDesvincular = { viewModel.toggleMercadoPagoVinculado(false) },
                        onConnect = {
                            currentSheetView = BottomSheetStateView.MOCK_CARGANDO_MP
                            showBottomSheet = true
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

        //     // BOTÓN DE AÑADIR MÉTODO
        //     Column(
        //         modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        //         horizontalAlignment = Alignment.CenterHorizontally
        //     ) {
        //         Button(
        //             onClick = {
        //                 haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        //                 tarjetaAEditar = null
        //                 viewModel.resetWalletCardForm()
        //                 currentSheetView = BottomSheetStateView.FORMULARIO_TARJETA
        //                 showProximamente = false
        //                 showBottomSheet = true
        //             },
        //             modifier = Modifier.fillMaxWidth().height(56.dp),
        //             colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
        //             shape = RoundedCornerShape(12.dp)
        //         ) {
        //             Row(
        //                 verticalAlignment = Alignment.CenterVertically,
        //                 horizontalArrangement = Arrangement.Center,
        //                 modifier = Modifier.fillMaxSize()
        //             ) {
        //                 Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White)
        //                 Spacer(Modifier.width(8.dp))
        //                 Text(
        //                     text = stringResource(Res.string.wallet_new_card),
        //                     style = typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = windowSize.adaptiveSp(16)),
        //                     color = Color.White
        //                 )
        //             }
        //         }

        //     }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                    tarjetaAEditar = null
                },
                sheetState = sheetState,
                containerColor = colorScheme.surface
            ) {
                when (currentSheetView) {
                    BottomSheetStateView.SELECCION_METODO -> {
                        AddMPMethodContent(
                            onConnectMP = {
                                currentSheetView = BottomSheetStateView.MOCK_CARGANDO_MP
                            },
                            isMPAlreadyLinked = mercadoPagoVinculado
                        )
                    }
                    // Alta de tarjetas deshabilitada temporalmente; se conserva el estado para reactivarla luego.
                    BottomSheetStateView.FORMULARIO_TARJETA -> Unit
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
            val ultimaTarjeta = tarjetaConfirmada ?: viewModel.usuario?.metodosPago?.lastOrNull()
            ModalBottomSheet(
                onDismissRequest = {
                    showTarjetaVinculadaModal = false
                    tarjetaConfirmada = null
                    tarjetaFueActualizada = false
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
                    Text(
                        text = stringResource(if (tarjetaFueActualizada) Res.string.wallet_update_success_title else Res.string.wallet_success_title),
                        style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        color = colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(if (tarjetaFueActualizada) Res.string.wallet_update_success_desc else Res.string.wallet_success_desc),
                        style = typography.bodyMedium,
                        color = MediumGray,
                        textAlign = TextAlign.Center
                    )

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
                                tarjetaConfirmada = null
                                tarjetaFueActualizada = false
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
            val tarjeta = tarjetaAEliminar!!
            Dialog(onDismissRequest = { tarjetaAEliminar = null }) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.94f).widthIn(max = 420.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = LightSurface,
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(76.dp),
                            shape = RoundedCornerShape(50),
                            color = colorScheme.error.copy(alpha = 0.08f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = null,
                                    tint = colorScheme.error,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(Res.string.wallet_delete_title),
                            style = typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.wallet_delete_confirmation, tarjeta.ultimos4),
                            style = typography.bodyMedium,
                            color = MediumGray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = ExtraLightGray
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(42.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = DarkGray
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(23.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = "${tarjeta.marca} ${tarjeta.nombreTitular}".trim(),
                                        color = DarkGray,
                                        style = typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = stringResource(Res.string.wallet_card_ending_in, tarjeta.ultimos4),
                                        color = MediumGray,
                                        style = typography.bodySmall
                                    )
                                }
                                if (tarjeta.expiracion.isNotBlank()) {
                                    Text(
                                        text = tarjeta.expiracion,
                                        color = MediumGray,
                                        style = typography.labelSmall
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.eliminarTarjeta(tarjeta.ultimos4)
                                tarjetaAEliminar = null
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.wallet_delete_card), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { tarjetaAEliminar = null },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = stringResource(Res.string.wallet_keep_card),
                                color = colorScheme.onSurfaceVariant,
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
fun LinkedMPView(
    isLinked: Boolean,
    onDesvincular: () -> Unit,
    onConnect: () -> Unit
) {
    val windowSize = LocalWindowSize.current
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLinked) Color(0xFFB7DFC2) else BorderGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(LightBlueBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, null, tint = MPBlue, modifier = Modifier.size(26.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mercado Pago",
                        color = DarkGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = windowSize.adaptiveSp(17)
                    )
                    Text(
                        text = if (isLinked) "Cuenta conectada" else "Vincula tu cuenta para continuar",
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLinked) Color(0xFFE8F5E9) else LightBlueBg
                ) {
                    Text(
                        text = if (isLinked) "ACTIVA" else "PENDIENTE",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        color = if (isLinked) Color(0xFF2E7D32) else MPBlue,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLinked) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDesvincular()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text("Desvincular cuenta", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onConnect()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MPBlue)
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Vincular Mercado Pago", fontWeight = FontWeight.Bold, color = Color.White)
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

        // Alta de tarjetas deshabilitada temporalmente.
        // Card(
        //     modifier = Modifier.fillMaxWidth().clickable { onAddCard() },
        //     colors = CardDefaults.cardColors(containerColor = colorScheme.background),
        //     shape = RoundedCornerShape(16.dp)
        // ) {
        //     Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        //         Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(colorScheme.surface), contentAlignment = Alignment.Center) {
        //             Icon(Icons.Default.CreditCard, null, tint = MediumGray)
        //         }
        //         Spacer(Modifier.width(16.dp))
        //         Column {
        //             Text(stringResource(Res.string.wallet_new_card), style = typography.bodyLarge.copy(fontWeight = FontWeight.Medium, fontSize = windowSize.adaptiveSp(16)), color = colorScheme.onSurface)
        //             Text("Validar y guardar tarjeta", style = typography.bodySmall, color = MediumGray)
        //         }
        //     }
        // }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun CreditCardView(
    cardNumber: String,
    cardHolder: String,
    expiryDate: String,
    cardBrand: String
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