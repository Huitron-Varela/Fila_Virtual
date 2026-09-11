package com.example.fila_virtual.features.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import com.example.fila_virtual.core.WindowSize
import com.example.fila_virtual.components.BottomNavigationBar
import com.example.fila_virtual.components.NavigationDefaults
import com.example.fila_virtual.perfil.ProfileComponent
import com.example.fila_virtual.features.user.home.HomeView
import com.example.fila_virtual.features.user.ordenes.OrdenesScreen
import com.example.fila_virtual.features.user.billetera.BilleteraScreen
import com.example.fila_virtual.features.user.carrio_compra.CartScreen
import com.example.fila_virtual.features.user.menu.UserMenuScreen
import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.perfil.EditProfileScreen
import com.example.fila_virtual.perfil.EnProcesoScreen
import com.example.fila_virtual.core.LegalConstants
import com.example.fila_virtual.core.navigation.LegalScreen

@Composable
fun ClienteMainScreen(
    viewModel: UserViewModel = remember { UserViewModel() },
    onLogout: () -> Unit
) {
    val usuario = viewModel.usuario
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })

    // 👇 ESCUCHAMOS EL CARRITO DESDE EL CEREBRO PRINCIPAL
    val carritoActual by viewModel.carrito.collectAsState(initial = emptyList())

    // ESTADOS PARA NAVEGACIÓN
    var isEditingProfile by remember { mutableStateOf(false) }
    var showCart by remember { mutableStateOf(false) }
    var showSecurity by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var selectedEstablecimiento by remember { mutableStateOf<Establecimiento?>(null) }

    if (isEditingProfile) {
        EditProfileScreen(
            usuario = usuario,
            viewModel = viewModel,
            onBack = { isEditingProfile = false }
        )
    } else if (showCart) {
        CartScreen(
            viewModel = viewModel, // Conectado al cerebro real
            onBackClick = { showCart = false },
            onOrderSuccess = {
                showCart = false
                scope.launch { pagerState.animateScrollToPage(1) }
            }
        )
    } else if (showSecurity) {
        EnProcesoScreen(
            titulo = "Configuración de Seguridad",
            onBack = { showSecurity = false }
        )
    } else if (showHelp) {
        EnProcesoScreen(
            titulo = "Centro de Ayuda",
            onBack = { showHelp = false }
        )
    } else if (showTerms) {
        LegalScreen(
            title = "Términos y Condiciones",
            content = LegalConstants.TERMINOS_Y_CONDICIONES,
            onBack = { showTerms = false }
        )
    } else if (selectedEstablecimiento != null) {
        UserMenuScreen(
            establecimientoId = selectedEstablecimiento!!.id,
            nombreEstablecimiento = selectedEstablecimiento!!.nombre,
            onBack = { selectedEstablecimiento = null },
            userViewModel = viewModel
        )
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val windowSize = WindowSize(maxWidth, maxHeight)
            val horizontalMargin = if (windowSize.isTablet) (maxWidth - 550.dp) / 2 else 0.dp

            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    BottomNavigationBar(
                        items = NavigationDefaults.userItems(),
                        selectedIndex = pagerState.currentPage,
                        onItemSelected = { index ->
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    )
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = horizontalMargin)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true
                    ) { page ->
                        when (page) {
                            0 -> HomeView(
                                usuario = usuario,
                                cartCount = carritoActual.size, // 👇 Pasamos la cantidad
                                onCartClick = { showCart = true },
                                onEstablecimientoClick = { local -> selectedEstablecimiento = local },
                                onAddToCart = { producto ->
                                    viewModel.agregarAlCarrito(
                                        producto.id,       // Pasa el ID como String
                                        producto.nombre,   // Pasa el nombre
                                        producto.precio    // Pasa el precio
                                    )
                                } // 👇 Disparamos al VM principal
                            )
                            1 -> OrdenesScreen()
                            2 -> BilleteraScreen(viewModel)
                            3 -> ProfileComponent(
                                usuario = usuario,
                                viewModel = viewModel,
                                onLogout = { viewModel.signOut(onLogout) },
                                onNavigateToEdit = { isEditingProfile = true },
                                onNavigateToSecurity = { showSecurity = true },
                                onNavigateToHelp = { showHelp = true },
                                onNavigateToTerms = { showTerms = true }
                            )
                        }
                    }
                }
            }
        }
    }
}