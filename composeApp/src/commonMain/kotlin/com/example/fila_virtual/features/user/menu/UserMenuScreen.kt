package com.example.fila_virtual.features.user.menu

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ShoppingCart // 🔥 IMPORTACIÓN AGREGADA
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.AppSnackbarHost
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
import com.example.fila_virtual.features.user.home.ProductCard
import com.example.fila_virtual.features.user.home.ProductDetailModal
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.core.BackHandler
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.core.theme.*
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.general_back
import fila_virtual.composeapp.generated.resources.user_cart_item_added
import fila_virtual.composeapp.generated.resources.user_cart_open
import fila_virtual.composeapp.generated.resources.user_menu_empty
import fila_virtual.composeapp.generated.resources.user_menu_empty_desc
import fila_virtual.composeapp.generated.resources.user_menu_load_error
import fila_virtual.composeapp.generated.resources.user_menu_loading
import fila_virtual.composeapp.generated.resources.user_menu_retry
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMenuScreen(
    establecimientoId: String,
    nombreEstablecimiento: String,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    userViewModel: UserViewModel
) {
    val windowSize = LocalWindowSize.current
    val menuViewModel = remember { UserMenuViewModel() }
    val productos by menuViewModel.productos.collectAsState()
    val menuCargando by menuViewModel.cargando.collectAsState()
    val menuError by menuViewModel.error.collectAsState()

    // Leemos el carrito directamente del UserViewModel para saber cuántos items hay
    val cartItems by userViewModel.carrito.collectAsState()
    val cartItemsCount = cartItems.sumOf { it.cantidad }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }

    BackHandler(onBack = onBack)

    LaunchedEffect(establecimientoId) {
        menuViewModel.cargarMenu(establecimientoId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .height(56.dp)
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onBack()
                            },
                            modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.general_back))
                        }

                        Text(
                            text = nombreEstablecimiento,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(horizontal = 88.dp),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToCart()
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                                .size(44.dp)
                                .background(ExtraLightGray, CircleShape)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (cartItemsCount > 0) {
                                        Badge(containerColor = PrimaryOrange, contentColor = Color.White) {
                                            Text("$cartItemsCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = stringResource(Res.string.user_cart_open),
                                    tint = DarkGray
                                )
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { AppSnackbarHost(snackbarHostState) }
        ) { padding ->
            AppPullToRefreshBox(
                isRefreshing = menuCargando,
                onRefresh = { menuViewModel.cargarMenu(establecimientoId) },
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val contentState = when {
                    menuCargando && productos.isEmpty() -> "loading"
                    menuError -> "error"
                    productos.isEmpty() -> "empty"
                    else -> "products"
                }

                Crossfade(
                    targetState = contentState,
                    modifier = Modifier.fillMaxSize(),
                    label = "user_menu_content"
                ) { state ->
                    when (state) {
                        "loading" -> Box(Modifier.fillMaxSize().padding(horizontal = windowSize.compactDp(24), vertical = 16.dp)) {
                            AppListSkeleton(itemCount = 5, imageSize = 72.dp)
                        }
                        "error" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = windowSize.compactDp(24)),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = LightSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.height(12.dp))
                                    Text(stringResource(Res.string.user_menu_load_error), color = DarkGray, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuViewModel.cargarMenu(establecimientoId)
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(stringResource(Res.string.user_menu_retry))
                                    }
                                }
                            }
                        }
                        "empty" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Surface(modifier = Modifier.size(88.dp), shape = CircleShape, color = SoftOrangeBg) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(40.dp))
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(stringResource(Res.string.user_menu_empty), color = DarkGray, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    stringResource(Res.string.user_menu_empty_desc),
                                    color = MediumGray,
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                        else -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                horizontal = windowSize.compactDp(16),
                                vertical = windowSize.compactDp(12)
                            ),
                            verticalArrangement = Arrangement.spacedBy(windowSize.compactDp(12))
                        ) {
                            items(items = productos, key = { producto -> producto.id }) { producto ->
                                val mensajeAgregado = stringResource(
                                    Res.string.user_cart_item_added,
                                    producto.nombre
                                )
                                ProductCard(
                                    producto = producto,
                                    storeName = nombreEstablecimiento,
                                    onClick = { productoSeleccionado = producto },
                                    onAddToCartClick = {
                                        userViewModel.agregarAlCarrito(
                                            idProducto = producto.id,
                                            nombre = producto.nombre,
                                            precio = producto.precio,
                                            establecimientoId = establecimientoId,
                                            establecimientoNombre = nombreEstablecimiento,
                                            imagenUrl = producto.imagenUrl,
                                            descripcion = producto.descripcion
                                        )
                                        scope.launch { snackbarHostState.showSnackbar(mensajeAgregado) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            }
        }

        productoSeleccionado?.let { producto ->
            ProductDetailModal(
                producto = producto,
                storeName = nombreEstablecimiento,
                onDismiss = { productoSeleccionado = null },
                onAddToCart = { product, quantity ->
                    repeat(quantity) {
                        userViewModel.agregarAlCarrito(
                            idProducto = product.id,
                            nombre = product.nombre,
                            precio = product.precio,
                            establecimientoId = establecimientoId,
                            establecimientoNombre = nombreEstablecimiento,
                            imagenUrl = product.imagenUrl,
                            descripcion = product.descripcion
                        )
                    }
                    productoSeleccionado = null
                    scope.launch { snackbarHostState.showSnackbar("${product.nombre} añadido al carrito") }
                }
            )
        }
    }
}
