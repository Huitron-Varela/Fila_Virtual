package com.example.fila_virtual.features.admin.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.SearchBar
import com.example.fila_virtual.components.RemoteImage
import com.example.fila_virtual.core.LocalWindowSize
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
    establecimientoViewModel: EstablecimientoViewModel = viewModel(),
    permitirCambiarEstablecimiento: Boolean = true,
    establecimientoNombreFijo: String = ""
) {
    val windowSize = LocalWindowSize.current
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val horizontalPadding = windowSize.compactDp(24)
    val isCompact = windowSize.isSmallScreen
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
    
    val sucursalActual = establecimientoNombreFijo.ifBlank { if (currentEstablecimientoId.isEmpty()) {
        stringResource(Res.string.est_all_branches)
    } else {
        establecimientos.find { it.id == currentEstablecimientoId }?.nombre ?: "Sucursal desconocida"
    } }

    // Filtrado dinámico por categoría y búsqueda
    val productosFiltrados = productos.filter { producto ->
        val matchesCategory = categoriaSeleccionada == "Todos" || producto.categoria == categoriaSeleccionada
        val matchesSearch = producto.nombre.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        containerColor = colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = PrimaryOrange,
                contentColor = LightSurface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.menu_title_add))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 960.dp)
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = windowSize.compactDp(24), bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(Res.string.menu_title_manage),
                        style = typography.titleLarge.copy(
                            fontSize = windowSize.adaptiveSp(20),
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = stringResource(Res.string.menu_search),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = stringResource(Res.string.emp_branch),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MediumGray
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    Surface(
                        color = LightSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = permitirCambiarEstablecimiento) {
                                showSucursalSelector = true
                            },
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = DarkGray,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = sucursalActual,
                                color = if (currentEstablecimientoId.isEmpty()) MediumGray else DarkGray,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (permitirCambiarEstablecimiento) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = DarkGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    if (permitirCambiarEstablecimiento) {
                        DropdownMenu(
                            expanded = showSucursalSelector,
                            onDismissRequest = { showSucursalSelector = false },
                            modifier = Modifier.fillMaxWidth(0.9f).background(LightSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.est_all_branches)) },
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
                }

                Spacer(modifier = Modifier.height(16.dp))

            // Chips de categorías horizontal
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(categorias) { categoria ->
                    val isSelected = categoria == categoriaSeleccionada
                    Surface(
                        color = if (isSelected) PrimaryOrange else LightSurface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { categoriaSeleccionada = categoria },
                        border = if (!isSelected) BorderStroke(1.dp, BorderGray) else null
                    ) {
                        Text(
                            text = categoria,
                            color = if (isSelected) LightSurface else DarkGray,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Lista de Platillos o Estado Vacío
            if (productosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (ownerUid.isEmpty())
                            "Inicia sesión para ver tu menú" 
                        else if (searchQuery.isEmpty()) 
                            stringResource(Res.string.menu_empty) 
                        else 
                            "No se encontraron resultados para '$searchQuery'",
                        color = MediumGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = horizontalPadding),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
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
    }

    if (selectedProducto != null) {
        val prod = selectedProducto!!
        ModalBottomSheet(
            onDismissRequest = { selectedProducto = null },
            sheetState = sheetState,
            containerColor = LightSurface
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .heightIn(max = windowSize.compactDp(720))
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.CenterHorizontally),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Image placeholder with badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isCompact) 160.dp else 200.dp)
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
                        color = SoftOrangeBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.Restaurant, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = prod.categoria,
                                color = PrimaryOrange,
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
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$${prod.precio}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryOrange,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                Text(
                    text = prod.descripcion.ifEmpty { "Sin descripción disponible." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
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
                                text = stringResource(Res.string.menu_dish_active),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkGray
                            )
                            Text(
                                text = stringResource(Res.string.menu_dish_active_desc),
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
                                checkedThumbColor = LightSurface,
                                checkedTrackColor = PrimaryOrange,
                                uncheckedThumbColor = LightSurface,
                                uncheckedTrackColor = MediumGray,
                                uncheckedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    Text(stringResource(Res.string.menu_title_edit), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Eliminar
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TrafficRed),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TrafficRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.menu_delete_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
                    Text(stringResource(Res.string.menu_delete_confirm))
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            viewModel.eliminarProducto(prod.id)
                            showDeleteDialog = false
                            selectedProducto = null
                        }
                    ) {
                        Text(stringResource(Res.string.menu_delete_btn), fontWeight = FontWeight.Bold, color = TrafficRed)
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    .clip(RoundedCornerShape(10.dp))
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
                    color = DarkGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(color = SoftOrangeBg, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = producto.categoria,
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryOrange,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = ExtraLightGray,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = sucursalNombre,
                            style = MaterialTheme.typography.labelSmall,
                            color = DarkGray,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${producto.precio}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryOrange
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(
                        color = if (producto.disponible) Color(0xFFE8F5E9) else ExtraLightGray,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = stringResource(
                                if (producto.disponible) Res.string.menu_dish_available
                                else Res.string.menu_dish_unavailable
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (producto.disponible) TrafficGreen else MediumGray,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = PrimaryOrange,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
