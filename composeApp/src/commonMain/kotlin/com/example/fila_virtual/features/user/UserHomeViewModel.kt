package com.example.fila_virtual.features.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.repository.EstablecimientoRepository
import com.example.fila_virtual.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class UserHomeViewModel : ViewModel() {

    private val establecimientoRepo = EstablecimientoRepository()
    private val productoRepo = ProductoRepository()

    private val _refreshVersion = MutableStateFlow(0)
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    private val refreshedSources = mutableSetOf<Int>()
    private var establishmentsLoaded = false
    private var productsLoaded = false

    private fun onSourceLoaded(source: Int) {
        if (source == 0) establishmentsLoaded = true else productsLoaded = true
        if (establishmentsLoaded && productsLoaded) _isLoading.value = false

        if (_isRefreshing.value && refreshedSources.add(source) && refreshedSources.size == 2) {
            refreshedSources.clear()
            _isRefreshing.value = false
        }
    }

    // Reopen both live streams when the user requests a refresh.
    private val _todosLosEstablecimientos = _refreshVersion
        .flatMapLatest {
            establecimientoRepo.getEstablecimientos().onEach { onSourceLoaded(0) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _todosLosProductos = _refreshVersion
        .flatMapLatest {
            productoRepo.getProductosGlobales().onEach { onSourceLoaded(1) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Estado para saber qué categoría presionó el usuario
    private val _categoriaSeleccionada = MutableStateFlow("Todos")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada

    // 3. Extraer categorías dinámicamente
    val categoriasDisponibles: StateFlow<List<String>> = _todosLosEstablecimientos.map { lista ->
        val categoriasExtraidas = lista.flatMap { it.categorias }.distinct().sorted()
        listOf("Todos") + categoriasExtraidas
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf("Todos")
    )

    // 4. Lista final filtrada que se pintará en la pantalla
    val establecimientosFiltrados: StateFlow<List<Establecimiento>> = combine(
        _todosLosEstablecimientos,
        _categoriaSeleccionada
    ) { lista, categoria ->
        if (categoria == "Todos") {
            lista.filter { it.activo }
        } else {
            lista.filter { it.activo && it.categorias.contains(categoria) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // 5. Variable para el TOP 5
    val recomendaciones: StateFlow<List<Pair<Producto, String>>> = combine(
        _todosLosEstablecimientos,
        _todosLosProductos
    ) { locales, productos ->
        productos.filter { it.enCatalogo && it.disponible }.map { producto ->
            val nombreLocal = locales.find { it.id == producto.establecimientoId }?.nombre ?: "Local"
            Pair(producto, nombreLocal)
        }.take(5) // Solo mostramos el Top 5
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun seleccionarCategoria(categoria: String) {
        _categoriaSeleccionada.value = categoria
    }

    fun recargar() {
        refreshedSources.clear()
        _isRefreshing.value = true
        _refreshVersion.value += 1
    }

    // --- 👇 LÓGICA DEL BUSCADOR INTEGRADA 👇 ---

    // Estado que guarda lo que el usuario escribe en tiempo real
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Función para actualizar el texto desde la interfaz
    fun actualizarBusqueda(query: String) {
        _searchQuery.value = query
    }

    // Lista dinámica que solo busca cuando hay texto escrito
    val resultadosBusqueda: StateFlow<List<Pair<Producto, String>>> = combine(
        _searchQuery,
        _todosLosEstablecimientos,
        _todosLosProductos
    ) { query, locales, productos ->
        if (query.isBlank()) {
            emptyList() // Si no ha escrito nada, mandamos una lista vacía
        } else {
            // Filtramos los productos que contengan las letras escritas (ignorando mayúsculas)
            productos.filter {
                it.enCatalogo && it.disponible && it.nombre.contains(query, ignoreCase = true)
            }.map { producto ->
                val nombreLocal = locales.find { it.id == producto.establecimientoId }?.nombre ?: "Local"
                Pair(producto, nombreLocal)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}