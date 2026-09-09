package com.example.fila_virtual.features.admin.empleados

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.SearchBar
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.EmpleadoDetalle
import com.example.fila_virtual.features.admin.FormState

import com.example.fila_virtual.features.admin.EstablecimientoViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    establecimientoId: String,
    ownerUid: String,
    onNavigateToAdd: (String) -> Unit,
    onEditEmpleado: (EmpleadoDetalle) -> Unit,
    viewModel: EmpleadoViewModel = viewModel(),
    establecimientoViewModel: EstablecimientoViewModel = viewModel()
) {
    val windowSize = LocalWindowSize.current
    val horizontalPadding = windowSize.adaptiveDp(24)

    val uiState by viewModel.uiState.collectAsState()
    val empleados by viewModel.empleados.collectAsState()
    val establecimientos by establecimientoViewModel.establecimientos.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    
    var currentEstablecimientoId by remember { mutableStateOf(if (establecimientoId.isEmpty()) "TODOS" else establecimientoId) }
    var showSucursalSelector by remember { mutableStateOf(false) }

    var selectedEmpleado by remember { mutableStateOf<EmpleadoDetalle?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Cargar establecimientos y empleados al montar la pantalla
    LaunchedEffect(currentEstablecimientoId, ownerUid, establecimientos.size) {
        establecimientoViewModel.setOwnerUid(ownerUid)
        when {
            currentEstablecimientoId == "TODOS" && establecimientos.isNotEmpty() -> {
                viewModel.cargarTodosLosEmpleados(establecimientos.map { it.id })
            }
            currentEstablecimientoId != "TODOS" && currentEstablecimientoId.isNotEmpty() -> {
                viewModel.cargarEmpleados(currentEstablecimientoId)
            }
        }
    }

    val listaFiltrada = empleados.filter {
        it.nombre.contains(searchQuery, ignoreCase = true) ||
                it.roles.any { role -> role.contains(searchQuery, ignoreCase = true) } ||
                it.correo.contains(searchQuery, ignoreCase = true)
    }

    val sucursalActual = when (currentEstablecimientoId) {
        "TODOS" -> "Todos los Empleados"
        "" -> "Seleccionar Sucursal"
        else -> establecimientos.find { it.id == currentEstablecimientoId }?.nombre ?: "Sucursal desconocida"
    }

    Scaffold(
        containerColor = LightBackground,
        floatingActionButton = {
            val canAddEmployee = currentEstablecimientoId.isNotEmpty() &&
                (currentEstablecimientoId != "TODOS" || establecimientos.isNotEmpty())
            if (canAddEmployee) {
                FloatingActionButton(
                    onClick = {
                        val targetEstablecimientoId = if (currentEstablecimientoId == "TODOS") {
                            establecimientos.firstOrNull()?.id.orEmpty()
                        } else {
                            currentEstablecimientoId
                        }
                        if (targetEstablecimientoId.isNotEmpty()) {
                            onNavigateToAdd(targetEstablecimientoId)
                        }
                    },
                    containerColor = PrimaryOrange,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Empleado")
                }
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Gestión de Empleados",
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
                        // Opción "Todos"
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.People,
                                        contentDescription = null,
                                        tint = PrimaryOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Todos los Empleados", fontWeight = FontWeight.Bold, color = PrimaryOrange)
                                }
                            },
                            onClick = {
                                currentEstablecimientoId = "TODOS"
                                showSucursalSelector = false
                            }
                        )
                        HorizontalDivider()
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

            if (currentEstablecimientoId == "TODOS" || currentEstablecimientoId.isNotEmpty()) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Buscar empleados...",
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Personal",
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

                when {
                    uiState is FormState.Loading && empleados.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryOrange)
                        }
                    }

                    uiState is FormState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (uiState as FormState.Error).message,
                                color = TrafficRed,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }

                    listaFiltrada.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty())
                                    "No hay empleados registrados aún"
                                else
                                    "No se encontraron resultados para \"$searchQuery\"",
                                color = MediumGray,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 88.dp)
                        ) {
                            items(listaFiltrada, key = { it.uid }) { empleado ->
                                CardEmpleado(
                                    empleado = empleado,
                                    onClick = { selectedEmpleado = empleado }
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selecciona una sucursal para ver los empleados",
                        color = MediumGray,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        }
        
        // Modal de Opciones
        if (selectedEmpleado != null) {
            val emp = selectedEmpleado!!
            ModalBottomSheet(
                onDismissRequest = { selectedEmpleado = null },
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
                    // Profile avatar
                    Box(modifier = Modifier.size(100.dp)) {
                        EmployeeAvatar(
                            fotoUrl = emp.fotoUrl,
                            modifier = Modifier.fillMaxSize(),
                            iconSize = 50.dp
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(20.dp)
                                .background(if (emp.activo) TrafficGreen else MediumGray, CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = emp.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                    Text(
                        text = emp.roles.joinToString(" • ") { role ->
                            role.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MediumGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        color = if (emp.activo) Color(0xFFE8F5E9) else ExtraLightGray,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (emp.activo) TrafficGreen else MediumGray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (emp.activo) "ACTIVO" else "INACTIVO",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (emp.activo) TrafficGreen else MediumGray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val dateStr = if (emp.joinedAt > 0) "Registrado" else "N/A"
                    
                    // Mail card
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = ExtraLightGray, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = DarkGray, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("CORREO ELECTRÓNICO", style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
                            Text(emp.correo, style = MaterialTheme.typography.bodyMedium, color = DarkGray, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    // Date & Shift
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = ExtraLightGray, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Outlined.DateRange, contentDescription = null, tint = DarkGray, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("CONTRATACIÓN", style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
                            Text(dateStr, style = MaterialTheme.typography.bodyMedium, color = DarkGray, fontWeight = FontWeight.Medium)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            selectedEmpleado = null
                            onEditEmpleado(emp) 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar Perfil", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
                        Text("Eliminar Empleado", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Eliminar Empleado", fontWeight = FontWeight.Bold) },
                    text = { Text("¿Estás seguro de que deseas eliminar a este empleado del establecimiento?") },
                    confirmButton = {
                        TextButton(onClick = { 
                            viewModel.eliminarEmpleado(currentEstablecimientoId, emp.uid)
                            showDeleteDialog = false
                            selectedEmpleado = null
                        }) {
                            Text("Eliminar", fontWeight = FontWeight.Bold, color = TrafficRed)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancelar", color = DarkGray)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CardEmpleado(
    empleado: EmpleadoDetalle,
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
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(56.dp)) {
                EmployeeAvatar(
                    fotoUrl = empleado.fotoUrl,
                    modifier = Modifier.size(56.dp),
                    iconSize = 28.dp
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(16.dp)
                        .background(
                            color = if (empleado.activo) TrafficGreen else MediumGray,
                            shape = CircleShape
                        )
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = empleado.nombre,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray
                )
                Text(
                    text = empleado.correo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MediumGray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = empleado.roles.joinToString(" • ") { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryOrange,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                val badgeBg = if (empleado.activo) Color(0xFFE8F5E9) else ExtraLightGray
                val badgeColor = if (empleado.activo) TrafficGreen else MediumGray
                Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = if (empleado.activo) "ACTIVO" else "INACTIVO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                Icons.Default.MoreVert,
                contentDescription = "Opciones",
                tint = MediumGray,
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}

@Composable
private fun EmployeeAvatar(
    fotoUrl: String,
    modifier: Modifier,
    iconSize: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(LightGray),
        contentAlignment = Alignment.Center
    ) {
        if (fotoUrl.isNotBlank()) {
            KamelImage(
                resource = asyncPainterResource(fotoUrl),
                contentDescription = "Foto de perfil",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onFailure = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MediumGray, modifier = Modifier.size(iconSize))
                }
            )
        } else {
            Icon(Icons.Default.Person, contentDescription = null, tint = MediumGray, modifier = Modifier.size(iconSize))
        }
    }
}