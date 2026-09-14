package com.example.fila_virtual.features.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.BottomNavigationBar
import com.example.fila_virtual.components.NavigationDefaults
import com.example.fila_virtual.core.WindowSize
import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.features.user.billetera.BilleteraScreen
import com.example.fila_virtual.features.user.carrio_compra.CartScreen
import com.example.fila_virtual.features.user.home.HomeView
import com.example.fila_virtual.features.user.menu.UserMenuScreen
import com.example.fila_virtual.features.user.ordenes.OrdenesScreen
import com.example.fila_virtual.perfil.EditProfileScreen
import com.example.fila_virtual.perfil.ProfileComponent
import kotlinx.coroutines.launch

@Composable
fun ClienteMainScreen(
    viewModel: UserViewModel = remember { UserViewModel() },
    onLogout: () -> Unit
) {

    val usuario = viewModel.usuario

    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        pageCount = { 4 }
    )

    /*
     * ==========================================================
     * IMPORTANTE:
     * ==========================================================
     *
     * Observamos directamente el StateFlow del carrito.
     *
     * Cada vez que:
     *
     * - agregas un producto
     * - incrementas cantidad
     * - decrementas cantidad
     * - vacías el carrito
     *
     * Compose recompone esta pantalla.
     *
     * Esto permite que el numerito del carrito cambie
     * inmediatamente:
     *
     * 1 -> 2 -> 3 -> 4...
     */
    val carritoActual by viewModel.carrito.collectAsState()


    var isEditingProfile by remember {
        mutableStateOf(false)
    }

    var showCart by remember {
        mutableStateOf(false)
    }

    var selectedEstablecimiento by remember {
        mutableStateOf<Establecimiento?>(null)
    }


    /*
     * ==========================================================
     * EDITAR PERFIL
     * ==========================================================
     */
    if (isEditingProfile) {

        EditProfileScreen(
            usuario = usuario,
            viewModel = viewModel,
            onBack = {
                isEditingProfile = false
            }
        )


        /*
         * ==========================================================
         * CARRITO
         * ==========================================================
         */
    } else if (showCart) {

        CartScreen(

            viewModel = viewModel,

            onBackClick = {
                showCart = false
            },

            /*
             * Cuando termina correctamente el pago MOCK:
             *
             * 1. Cerramos carrito.
             * 2. Navegamos a Mis Órdenes.
             */
            onOrderSuccess = {

                showCart = false

                scope.launch {
                    pagerState.animateScrollToPage(1)
                }
            },

            /*
             * Si desde el carrito quiere agregar tarjeta,
             * lo mandamos a Billetera.
             */
            onNavigateToWallet = {

                viewModel.updatePendingWalletAction(
                    "abrir_formulario"
                )

                showCart = false

                scope.launch {
                    pagerState.animateScrollToPage(2)
                }
            }
        )


        /*
         * ==========================================================
         * MENÚ DE ESTABLECIMIENTO
         * ==========================================================
         */
    } else if (selectedEstablecimiento != null) {

        UserMenuScreen(

            establecimientoId =
                selectedEstablecimiento!!.id,

            nombreEstablecimiento =
                selectedEstablecimiento!!.nombre,

            onBack = {
                selectedEstablecimiento = null
            },

            userViewModel = viewModel
        )


        /*
         * ==========================================================
         * NAVEGACIÓN PRINCIPAL
         * ==========================================================
         */
    } else {

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {

            val windowSize =
                WindowSize(
                    maxWidth,
                    maxHeight
                )

            val horizontalMargin =
                if (windowSize.isTablet) {

                    (maxWidth - 550.dp) / 2

                } else {

                    0.dp
                }


            Scaffold(

                containerColor =
                    MaterialTheme.colorScheme.background,

                bottomBar = {

                    BottomNavigationBar(

                        items =
                            NavigationDefaults.userItems(),

                        selectedIndex =
                            pagerState.currentPage,

                        onItemSelected = { index ->

                            scope.launch {

                                pagerState.animateScrollToPage(
                                    index
                                )
                            }
                        }
                    )
                }

            ) { padding ->

                Box(

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(
                            horizontal =
                                horizontalMargin
                        )
                        .background(
                            MaterialTheme
                                .colorScheme
                                .background
                        )
                ) {

                    HorizontalPager(

                        state =
                            pagerState,

                        modifier =
                            Modifier.fillMaxSize(),

                        userScrollEnabled =
                            true

                    ) { page ->


                        when (page) {


                            /*
                             * ==================================================
                             * HOME
                             * ==================================================
                             */
                            0 -> {

                                HomeView(

                                    usuario =
                                        usuario,

                                    /*
                                     * ==================================================
                                     * CORRECCIÓN DEL CONTADOR
                                     * ==================================================
                                     *
                                     * NO usamos:
                                     *
                                     * carritoActual.size
                                     *
                                     * porque eso cuenta productos diferentes.
                                     *
                                     * Ejemplo:
                                     *
                                     * Hamburguesa x3
                                     *
                                     * size = 1
                                     *
                                     * Lo correcto es:
                                     *
                                     * sumOf { cantidad }
                                     *
                                     * Hamburguesa x3
                                     *
                                     * resultado = 3
                                     */
                                    cartCount =
                                        carritoActual.sumOf {
                                            it.cantidad
                                        },

                                    onCartClick = {
                                        showCart = true
                                    },

                                    onEstablecimientoClick = {
                                            establecimiento ->

                                        selectedEstablecimiento =
                                            establecimiento
                                    },

                                    onAddToCart = {
                                            producto ->

                                        viewModel.agregarAlCarrito(

                                            idProducto =
                                                producto.id,

                                            nombre =
                                                producto.nombre,

                                            precio =
                                                producto.precio
                                        )
                                    }
                                )
                            }


                            /*
                             * ==================================================
                             * MIS ÓRDENES
                             * ==================================================
                             */
                            1 -> {

                                OrdenesScreen()
                            }


                            /*
                             * ==================================================
                             * BILLETERA
                             * ==================================================
                             */
                            2 -> {

                                BilleteraScreen(
                                    viewModel
                                )
                            }


                            /*
                             * ==================================================
                             * PERFIL
                             * ==================================================
                             */
                            3 -> {

                                ProfileComponent(

                                    usuario =
                                        usuario,

                                    viewModel =
                                        viewModel,

                                    onLogout = {

                                        viewModel.signOut(
                                            onSuccess =
                                                onLogout
                                        )
                                    },

                                    onNavigateToEdit = {

                                        isEditingProfile =
                                            true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}