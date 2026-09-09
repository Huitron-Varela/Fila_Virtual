package com.example.fila_virtual.features.admin.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.FormHeader
import com.example.fila_virtual.components.SearchBar
import com.example.fila_virtual.components.RemoteImage
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.features.admin.EstablecimientoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstablishmentsScreen(
    currentAdminUid: String,
    onBack: () -> Unit,
    onSelectEstablecimiento: (String) -> Unit,
    onRegisterNew: () -> Unit,
    onEditEstablecimiento: (Establecimiento) -> Unit,
    viewModel: EstablecimientoViewModel = viewModel()
) {
    val windowSize = LocalWindowSize.current
    val horizontalPadding = windowSize.adaptiveDp(24).value.dp
    var searchQuery by remember { mutableStateOf("") }
    
    var selectedEstablecimiento by remember { mutableStateOf<Establecimiento?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentAdminUid) {
        viewModel.setOwnerUid(currentAdminUid)
    }
    // Obtenemos la lista real desde el ViewModel
    val establecimientos by viewModel.establecimientos.collectAsState()

    // Filtramos por búsqueda
    val listaFiltrada = establecimientos.filter { 
        it.nombre.contains(searchQuery, ignoreCase = true) 
    }

    Scaffold(
        containerColor = LightBackground,
        topBar = {
            FormHeader(
                title = "Mis Establecimientos",
                onBack = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRegisterNew,
                containerColor = PrimaryOrange,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar Nuevo Local")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Buscar sucursal...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sucursales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )

                Surface(
                    color = BorderGray,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${listaFiltrada.size} TOTAL",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (listaFiltrada.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "No hay establecimientos registrados" else "No se encontraron resultados",
                        color = MediumGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        start = horizontalPadding,
                        top = 8.dp,
                        end = horizontalPadding,
                        bottom = 100.dp
                    )
                ) {
                    items(listaFiltrada) { local ->
                        EstablecimientoCard(
                            establecimiento = local,
                            onClick = { selectedEstablecimiento = local }
                        )
                    }
                }
            }
        }
    }

    if (selectedEstablecimiento != null) {
        val est = selectedEstablecimiento!!
        ModalBottomSheet(
            onDismissRequest = { selectedEstablecimiento = null },
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
                // Top part: Image and Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BorderGray)
                    ) {
                        RemoteImage(
                            url = est.logoUrl,
                            contentDescription = est.nombre,
                            modifier = Modifier.fillMaxSize(),
                            fallback = {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = MediumGray,
                                    modifier = Modifier.align(Alignment.Center).size(36.dp)
                                )
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = est.nombre.ifEmpty { "Sin Nombre" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MediumGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = est.ubicacion.direccion.ifEmpty { "Sin dirección" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MediumGray,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Estado del Local Card
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Estado del Local",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkGray
                            )
                            Text(
                                text = "Visibilidad en la app para clientes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MediumGray
                            )
                        }
                        
                        Text(
                            text = if (est.activo) "ABIERTO" else "CERRADO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (est.activo) PrimaryOrange else MediumGray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = est.activo,
                            onCheckedChange = { disp -> 
                                viewModel.actualizarEstado(est.id, disp)
                                selectedEstablecimiento = est.copy(activo = disp)
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

                // Two small cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        color = ExtraLightGray,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryOrange.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.padding(6.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("CATEGORÍA", style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
                            Text(
                                est.categorias.firstOrNull() ?: "Sin categoría",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Surface(
                        color = ExtraLightGray,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = PrimaryOrange.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Outlined.Timer, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.padding(6.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("HORARIO", style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
                            val horario = est.horario["todos"]
                            Text(
                                if (horario == null) "Sin horario" else "${horario.apertura} - ${horario.cierre}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                OutlinedButton(
                    onClick = { 
                        selectedEstablecimiento = null
                        onSelectEstablecimiento(est.id) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkGray),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Administrar Establecimiento", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { 
                        selectedEstablecimiento = null
                        onEditEstablecimiento(est) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar Establecimiento", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

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
                    Text("Eliminar Establecimiento", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text("Eliminar establecimiento", fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("¿Estás seguro de que deseas eliminar este establecimiento? Esta acción no se puede deshacer.")
                },
                confirmButton = {
                    TextButton(
                        onClick = { 
                            viewModel.eliminarEstablecimiento(est.id)
                            showDeleteDialog = false
                            selectedEstablecimiento = null
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
fun EstablecimientoCard(
    establecimiento: Establecimiento,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Imagen y Badge (Izquierda)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BorderGray) // Placeholder de la imagen
            ) {
                RemoteImage(
                    url = establecimiento.logoUrl,
                    contentDescription = establecimiento.nombre,
                    modifier = Modifier.fillMaxSize(),
                    fallback = {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MediumGray,
                            modifier = Modifier.align(Alignment.Center).size(36.dp)
                        )
                    }
                )

                // Etiqueta "ABIERTO" / "CERRADO"
                val badgeBgColor = if (establecimiento.activo) TrafficGreen.copy(alpha = 0.15f) else ExtraLightGray
                val badgeTextColor = if (establecimiento.activo) TrafficGreen else MediumGray

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = badgeBgColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = badgeTextColor,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (establecimiento.activo) "ABIERTO" else "CERRADO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Información Principal (Centro)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = establecimiento.nombre.ifEmpty { "Sin Nombre" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MediumGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = establecimiento.ubicacion.direccion.ifEmpty { "Sin dirección" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGray,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Acciones (Derecha)
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
