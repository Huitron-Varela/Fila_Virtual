package com.example.fila_virtual.features.user.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.AppSnackbarHost
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
import com.example.fila_virtual.components.CategoryFilterRow
import com.example.fila_virtual.components.RemoteImage
import com.example.fila_virtual.components.SearchBar
import com.example.fila_virtual.components.Heders.StoreHeader
import com.example.fila_virtual.features.user.home.ProductDetailModal
import com.example.fila_virtual.components.cards.RecommendationProductCard
import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.core.BackHandler
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.core.theme.*
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.menu_search
import fila_virtual.composeapp.generated.resources.user_cart_item_added
import fila_virtual.composeapp.generated.resources.user_menu_empty
import fila_virtual.composeapp.generated.resources.user_menu_empty_desc
import fila_virtual.composeapp.generated.resources.user_menu_load_error
import fila_virtual.composeapp.generated.resources.user_menu_no_results
import fila_virtual.composeapp.generated.resources.user_menu_retry
import fila_virtual.composeapp.generated.resources.user_cart_view
import fila_virtual.composeapp.generated.resources.store_closed_now
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMenuScreen(
    establecimiento: Establecimiento,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    userViewModel: UserViewModel
) {
    val establecimientoId = establecimiento.id
    val menuViewModel = remember { UserMenuViewModel() }
    val productos by menuViewModel.productos.collectAsState()
    val menuCargando by menuViewModel.cargando.collectAsState()
    val menuError by menuViewModel.error.collectAsState()

    // Leemos el carrito directamente del UserViewModel para saber cuántos items hay
    val cartItems by userViewModel.carrito.collectAsState()
    val cartItemsCount = cartItems.sumOf { it.cantidad }
    val totalCarrito = userViewModel.calcularTotalCarrito()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember(establecimientoId) { mutableStateOf("Todos") }
    val categoriasMenu = remember(productos) {
        listOf("Todos") + productos.map { it.categoria }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val productosFiltrados = productos.filter { producto ->
        (categoriaSeleccionada == "Todos" || producto.categoria == categoriaSeleccionada) &&
            (producto.nombre.contains(searchQuery, ignoreCase = true) ||
                producto.descripcion.contains(searchQuery, ignoreCase = true) ||
                producto.categoria.contains(searchQuery, ignoreCase = true))
    }

    BackHandler(onBack = onBack)

    LaunchedEffect(establecimientoId) {
        menuViewModel.cargarMenu(establecimientoId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                StoreHeader(
                    title = establecimiento.nombre,
                    isOpen = establecimiento.activo,
                    cartCount = cartItemsCount,
                    onBack = onBack,
                    onCartClick = onNavigateToCart
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { AppSnackbarHost(snackbarHostState) },
            bottomBar = {
                if (cartItemsCount > 0) {
                    Button(
                        onClick = onNavigateToCart,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.22f)) {
                                Text(
                                    text = cartItemsCount.toString(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(Res.string.user_cart_view),
                                modifier = Modifier.weight(1f),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = formatMenuCurrency(totalCarrito),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        ) { padding ->
            AppPullToRefreshBox(
                isRefreshing = menuCargando,
                onRefresh = { menuViewModel.cargarMenu(establecimientoId) },
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item { EstablishmentMenuHero(establecimiento) }
                    item {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            placeholder = stringResource(Res.string.menu_search),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    item {
                        CategoryFilterRow(
                            categories = categoriasMenu,
                            selectedCategory = categoriaSeleccionada,
                            onCategorySelected = { categoriaSeleccionada = it },
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            showIcons = false
                        )
                    }

                    when {
                        menuCargando && productos.isEmpty() -> item {
                            AppListSkeleton(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                itemCount = 4,
                                imageSize = 64.dp
                            )
                        }
                        menuError -> item {
                            MenuLoadError(
                                onRetry = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuViewModel.cargarMenu(establecimientoId)
                                }
                            )
                        }
                        productos.isEmpty() -> item { MenuEmptyState() }
                        productosFiltrados.isEmpty() -> item {
                            Text(
                                text = stringResource(Res.string.user_menu_no_results),
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                color = MediumGray,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        else -> {
                            productosFiltrados
                                .groupBy { it.categoria.ifBlank { "Otros" } }
                                .toSortedMap()
                                .forEach { (categoria, productosCategoria) ->
                                    item(key = "category-$categoria") {
                                        Text(
                                            text = categoria.uppercase(),
                                            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkGray
                                        )
                                    }
                                    items(
                                        items = productosCategoria,
                                        key = { producto -> "${categoria}-${producto.id}" }
                                    ) { producto ->
                                        val mensajeAgregado = stringResource(
                                            Res.string.user_cart_item_added,
                                            producto.nombre
                                        )
                                        RecommendationProductCard(
                                            producto = producto,
                                            storeName = establecimiento.nombre,
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            onClick = { productoSeleccionado = producto },
                                            onAddToCartClick = {
                                                userViewModel.agregarAlCarrito(
                                                    idProducto = producto.id,
                                                    nombre = producto.nombre,
                                                    precio = producto.precio,
                                                    establecimientoId = establecimientoId,
                                                    establecimientoNombre = establecimiento.nombre,
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
                storeName = establecimiento.nombre,
                onDismiss = { productoSeleccionado = null },
                onAddToCart = { product, quantity ->
                    repeat(quantity) {
                        userViewModel.agregarAlCarrito(
                            idProducto = product.id,
                            nombre = product.nombre,
                            precio = product.precio,
                            establecimientoId = establecimientoId,
                            establecimientoNombre = establecimiento.nombre,
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

@Composable
private fun EstablishmentMenuHero(establecimiento: Establecimiento) {
    Column {
        Box(
            modifier = Modifier.fillMaxWidth().height(168.dp).background(ExtraLightGray),
            contentAlignment = Alignment.Center
        ) {
            RemoteImage(
                url = establecimiento.logoUrl,
                contentDescription = establecimiento.nombre,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                fallback = {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = MediumGray, modifier = Modifier.size(48.dp))
                }
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = establecimiento.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                maxLines = 1
            )
            if (establecimiento.descripcion.isNotBlank()) {
                Text(
                    text = establecimiento.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MediumGray,
                    maxLines = 2
                )
            }
            if (establecimiento.ubicacion.direccion.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = establecimiento.ubicacion.direccion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGray,
                        maxLines = 1
                    )
                }
            }
            val horario = establecimiento.horario.values.firstOrNull()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (establecimiento.ratingCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${(establecimiento.ratingAvg * 10).toInt() / 10.0} (${establecimiento.ratingCount})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkGray
                        )
                    }
                }
                if (horario != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (horario.cerrado) stringResource(Res.string.store_closed_now) else "${horario.apertura} - ${horario.cierre}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MediumGray,
                            maxLines = 1
                        )
                    }
                }
            }
            if (establecimiento.categorias.isNotEmpty()) {
                Text(
                    text = establecimiento.categorias.joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MediumGray,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MenuLoadError(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(12.dp))
        Text(stringResource(Res.string.user_menu_load_error), color = DarkGray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.user_menu_retry))
        }
    }
}

@Composable
private fun MenuEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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

private fun formatMenuCurrency(value: Double): String {
    val cents = (value * 100).toLong()
    return "$${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"
}
