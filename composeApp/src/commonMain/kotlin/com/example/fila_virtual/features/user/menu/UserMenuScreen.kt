package com.example.fila_virtual.features.user.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ShoppingCart // 🔥 IMPORTACIÓN AGREGADA
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.AppSnackbarHost
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.core.BackHandler
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.components.RemoteImage
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.general_back
import fila_virtual.composeapp.generated.resources.user_cart_item_added
import fila_virtual.composeapp.generated.resources.user_cart_open
import fila_virtual.composeapp.generated.resources.user_menu_empty
import fila_virtual.composeapp.generated.resources.user_menu_empty_desc
import fila_virtual.composeapp.generated.resources.user_menu_load_error
import fila_virtual.composeapp.generated.resources.user_menu_loading
import fila_virtual.composeapp.generated.resources.user_menu_retry
import fila_virtual.composeapp.generated.resources.user_product_add
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

    BackHandler(onBack = onBack)

    LaunchedEffect(establecimientoId) {
        menuViewModel.cargarMenu(establecimientoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreEstablecimiento, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.general_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToCart,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(48.dp)
                            .background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        BadgedBox(
                            badge = {
                                if (cartItemsCount > 0) {
                                    Badge(
                                        containerColor = PrimaryOrange,
                                        contentColor = Color.White,
                                        modifier = Modifier.offset(x = 2.dp, y = (-2).dp)
                                    ) {
                                        Text("$cartItemsCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = stringResource(Res.string.user_cart_open),
                                tint = Color(0xFF1E1E24)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { AppSnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                menuCargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PrimaryOrange)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(Res.string.user_menu_loading), color = MediumGray)
                    }
                }
                menuError -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(Res.string.user_menu_load_error), color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { menuViewModel.cargarMenu(establecimientoId) }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.user_menu_retry))
                        }
                    }
                }
                productos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray)
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(Res.string.user_menu_empty), color = DarkGray, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(Res.string.user_menu_empty_desc),
                            color = MediumGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(productos.size) { index ->
                        val producto = productos[index]
                        val mensajeAgregado = stringResource(
                            Res.string.user_cart_item_added,
                            producto.nombre
                        )
                        ProductoClienteCard(
                            producto = producto,
                            onAddClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                userViewModel.agregarAlCarrito(
                                    idProducto = producto.id,
                                    nombre = producto.nombre,
                                    precio = producto.precio,
                                    establecimientoId = establecimientoId,
                                    establecimientoNombre = nombreEstablecimiento
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar(mensajeAgregado)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoClienteCard(producto: Producto, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF1E1E24)),
                contentAlignment = Alignment.Center
            ) {
                RemoteImage(
                    url = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    fallback = {
                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = producto.nombre, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = producto.descripcion.ifEmpty { "Sin descripción" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$${producto.precio}", color = PrimaryOrange, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }

                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryOrange, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.user_product_add), tint = Color.White)
                }
            }
        }
    }
}