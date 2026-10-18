package com.example.fila_virtual.features.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.features.user.UserHomeViewModel

import com.example.fila_virtual.components.AppSnackbarHost
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
import com.example.fila_virtual.components.SearchBar
import com.example.fila_virtual.core.LocalWindowSize
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch

@Composable
fun HomeView(
    cartCount: Int,
    onCartClick: () -> Unit,
    onEstablecimientoClick: (Establecimiento) -> Unit,
    onAddToCart: (Producto) -> Unit
) {
    val windowSize = LocalWindowSize.current
    val horizontalPadding = windowSize.adaptiveDp(24).value.dp
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedProduct by remember { mutableStateOf<Pair<Producto, String>?>(null) }
    val handleQuickAdd: (Producto) -> Unit = { producto ->
        onAddToCart(producto)
        scope.launch {
            snackbarHostState.showSnackbar(
                "${producto.nombre} añadido al carrito",
                duration = SnackbarDuration.Short
            )
        }
    }

    val viewModel = remember { UserHomeViewModel() }
    val categoriasDisponibles by viewModel.categoriasDisponibles.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val establecimientos by viewModel.establecimientosFiltrados.collectAsState()
    val listaRecomendaciones by viewModel.recomendaciones.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val resultadosBusqueda by viewModel.resultadosBusqueda.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        AppPullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = viewModel::recargar,
            modifier = Modifier.fillMaxSize()
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            HomeHeader(
                cartCount = cartCount,
                onCartClick = onCartClick
            )
            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.actualizarBusqueda(it) }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AppListSkeleton(
                        modifier = Modifier.padding(horizontal = horizontalPadding),
                        itemCount = 5,
                        imageSize = 72.dp
                    )
                } else if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = stringResource(Res.string.home_search_results), padding = horizontalPadding)
                    Spacer(modifier = Modifier.height(16.dp))

                    RecommendationsList(
                        productosConLocal = resultadosBusqueda,
                        padding = horizontalPadding,
                        onQuickAdd = handleQuickAdd,
                        onProductClick = { producto, local -> selectedProduct = producto to local }
                    )
                } else {
                    SectionHeader(title = stringResource(Res.string.home_categories), padding = horizontalPadding)
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryRow(
                        categorias = categoriasDisponibles,
                        seleccion = categoriaSeleccionada,
                        onSelect = { viewModel.seleccionarCategoria(it) },
                        padding = horizontalPadding
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    SectionHeader(title = stringResource(Res.string.home_featured), padding = horizontalPadding)
                    Spacer(modifier = Modifier.height(16.dp))
                    CafeteriasList(
                        establecimientos = establecimientos,
                        padding = horizontalPadding,
                        onEstablecimientoClick = onEstablecimientoClick
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                    SectionHeader(title = stringResource(Res.string.home_recommendations), padding = horizontalPadding)
                    Spacer(modifier = Modifier.height(16.dp))
                    RecommendationsList(
                        productosConLocal = listaRecomendaciones,
                        padding = horizontalPadding,
                        onQuickAdd = handleQuickAdd,
                        onProductClick = { producto, local -> selectedProduct = producto to local }
                    )
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        }

        AppSnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )

        selectedProduct?.let { (producto, local) ->
            ProductDetailModal(
                producto = producto,
                storeName = local,
                onDismiss = { selectedProduct = null },
                onAddToCart = { product, quantity ->
                    repeat(quantity) { onAddToCart(product) }
                    selectedProduct = null
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            "${product.nombre} añadido al carrito",
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            )
        }
    }
}
