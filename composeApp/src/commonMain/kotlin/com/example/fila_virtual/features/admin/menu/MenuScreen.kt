package com.example.fila_virtual.features.admin.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.SearchBar
import com.example.fila_virtual.components.RemoteImage
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.features.admin.ProductoViewModel
import com.example.fila_virtual.features.admin.EstablecimientoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    establecimientoId: String,
    ownerUid: String,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Producto) -> Unit = {},
    viewModel: ProductoViewModel = viewModel(),
    establecimientoViewModel: EstablecimientoViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val categorias = ProductMenuCategories.filters
    var categoriaSeleccionada by remember { mutableStateOf(categorias[0]) }
    
    var currentEstablecimientoId by remember { mutableStateOf(establecimientoId) }
    var showSucursalSelector by remember { mutableStateOf(false) }

    var selectedProducto by remember { mutableStateOf<Producto?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Sincronizar el establecimientoId con el ViewModel para cargar los productos reales
    LaunchedEffect(currentEstablecimientoId, ownerUid) {
        viewModel.setOwnerUid(ownerUid)
        viewModel.setEstablecimientoId(currentEstablecimientoId)
        establecimientoViewModel.setOwnerUid(ownerUid)
    }

    // Observamos los productos y establecimientos
    val productos by viewModel.productos.collectAsState()
    val establecimientos by establecimientoViewModel.establecimientos.collectAsState()
    
    val sucursalActual = if (currentEstablecimientoId.isEmpty()) {
        "Todas las Sucursales"
    } else {
        establecimientos.find { it.id == currentEstablecimientoId }?.nombre ?: "Sucursal desconocida"
    }

    // Filtrado dinámico por categoría y búsqueda
    val productosFiltrados = productos.filter { producto ->
        val matchesCategory = categoriaSeleccionada == "Todos" || producto.categoria == categoriaSeleccionada
        val matchesSearch = producto.nombre.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        containerColor = LightBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = PrimaryOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Platillo")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Título de la sección
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gestión de Menú",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkGray,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                
                Box {
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.clickable { showSucursalSelector = true },
                        border = BorderStroke(1.dp, ExtraLightGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocationOn,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = sucursalActual,
                                color = PrimaryOrange,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSucursalSelector,
                        onDismissRequest = { showSucursalSelector = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas las Sucursales") },
                            onClick = {
                                currentEstablecimientoId = ""
                                showSucursalSelector = false
                            }
                        )
                        establecimientos.forEach { sucursal ->
                            DropdownMenuItem(
                                text = { Text(sucursal.nombre) },
                                onClick = {
                                    currentEstablecimientoId = sucursal.id
                                    showSucursalSelector = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Buscador funcional
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Buscar platillos...",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Chips de categorías horizontal
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(categorias) { categoria ->
                    val isSelected = categoria == categoriaSeleccionada
                    Surface(
                        color = if (isSelected) PrimaryOrange else Color.White,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { categoriaSeleccionada = categoria },
                        border = if (!isSelected) BorderStroke(1.dp, BorderGray) else null
                    ) {
                        Text(
                            text = categoria,
                            color = if (isSelected) Color.White else DarkGray,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Lista de Platillos o Estado Vacío
            if (productosFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (ownerUid.isEmpty())
                            "Inicia sesión para ver tu menú" 
                        else if (searchQuery.isEmpty()) 
                            "No hay platillos registrados aún" 
                        else 
                            "No se encontraron resultados para '$searchQuery'",
                        color = MediumGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(productosFiltrados) { producto ->
                        val sucursalNombre = establecimientos.find { it.id == producto.establecimientoId }?.nombre ?: "Sin Sucursal"
                        CardMenuItem(
                            producto = producto,
                            sucursalNombre = sucursalNombre,
                            onClick = { selectedProducto = producto }
                        )
                    }
                }
            }
        }
    }

    if (selectedProducto != null) {
        val prod = selectedProducto!!
        ModalBottomSheet(
            onDismissRequest = { selectedProducto = null },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Image placeholder with badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BorderGray)
                ) {
                    RemoteImage(
                        url = prod.imagenUrl,
                        contentDescription = prod.nombre,
                        modifier = Modifier.fillMaxSize(),
                        fallback = {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = MediumGray,
                                modifier = Modifier.size(64.dp).align(Alignment.Center)
                            )
                        }
                    )
                    
                    // Category badge
                    Surface(
                        color = Color(0xFF00ACC1),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = prod.categoria,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title & Price
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prod.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$${prod.precio}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC62828)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = prod.descripcion.ifEmpty { "Sin descripción disponible." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Activo en Menú Card
                Surface(
                    color = ExtraLightGray,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = PrimaryOrange.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Activo en Menú",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkGray
                            )
                            Text(
                                text = "Visible para clientes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MediumGray
                            )
                        }
                        
                        Switch(
                            checked = prod.disponible,
                            onCheckedChange = { disp -> 
                                viewModel.actualizarDisponibilidad(prod.id, disp) 
                                selectedProducto = prod.copy(disponible = disp)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryOrange,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = MediumGray,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Editar
                Button(
                    onClick = { 
                        selectedProducto = null
                        onNavigateToEdit(prod) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar Platillo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Eliminar
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD32F2F).copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Platillo", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text("Eliminar platillo", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("¿Estás seguro de que deseas eliminar este platillo? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            viewModel.eliminarProducto(prod.id)
                            showDeleteDialog = false
                            selectedProducto = null
                        }
                    ) {
                        Text("Eliminar", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false }
                    ) {
                        Text("Cancelar", color = DarkGray)
                    }
                }
            )
        }
    }
}

@Composable
private fun CardMenuItem(
    producto: Producto,
    sucursalNombre: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ExtraLightGray),
                contentAlignment = Alignment.Center
            ) {
                RemoteImage(
                    url = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    fallback = {
                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray)
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info del platillo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = ExtraLightGray,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = sucursalNombre.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkGray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$${producto.precio}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = PrimaryOrange
                )
            }

            // Acciones (Derecha)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.height(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = MediumGray
                )
            }
        }
    }
}
