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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*
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
    val horizontalPadding = windowSize.compactDp(24)

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
        "TODOS" -> stringResource(Res.string.emp_all_employees)
        "" -> stringResource(Res.string.emp_select_branch)
        else -> establecimientos.find { it.id == currentEstablecimientoId }?.nombre ?: stringResource(Res.string.emp_unknown_branch)
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
                    Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.emp_title_add))
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
                    text = stringResource(Res.string.emp_title_manage),
                    style = MaterialTheme.typography.headlineSmall,
                    color = DarkGray,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = windowSize.compactDp(24), bottom = 8.dp)
                )

                Box {
                    Surface(
                        color = LightSurface,
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
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                    Text(stringResource(Res.string.emp_all_employees), fontWeight = FontWeight.Bold, color = PrimaryOrange)
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
                    placeholder = stringResource(Res.string.emp_search),
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.emp_staff_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                    Surface(
                        color = BorderGray,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${listaFiltrada.size} ${stringResource(Res.string.emp_total_label)}",
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
                                    stringResource(Res.string.emp_empty)
                                else
                                    stringResource(Res.string.emp_no_results_query, searchQuery),
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
                        text = stringResource(Res.string.emp_select_branch_instruction),
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
                containerColor = LightSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
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
                        color = DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Traducción de roles
                    val rolesText = emp.roles.map { role ->
                        when (role.lowercase()) {
                            "cajero" -> stringResource(Res.string.emp_role_cashier)
                            "cocina" -> stringResource(Res.string.emp_role_kitchen)
                            "entrega" -> stringResource(Res.string.emp_role_delivery)
                            "supervisor" -> stringResource(Res.string.emp_role_supervisor)
                            "admin" -> stringResource(Res.string.emp_role_admin_local)
                            else -> role
                        }
                    }.joinToString(" • ")

                    Text(
                        text = rolesText,
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
                                text = if (emp.activo) stringResource(Res.string.emp_status_active) else stringResource(Res.string.emp_status_inactive),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (emp.activo) TrafficGreen else MediumGray
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    val dateStr = if (emp.joinedAt > 0) stringResource(Res.string.emp_registered) else stringResource(Res.string.emp_not_available)
                    
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
                            Text(stringResource(Res.string.emp_email_label), style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
                            Text(emp.correo, style = MaterialTheme.typography.bodyMedium, color = DarkGray, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
                            Text(stringResource(Res.string.emp_joined_label), style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
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
                        Text(stringResource(Res.string.profile_edit_profile), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
                        Text(stringResource(Res.string.emp_delete_title), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(Res.string.emp_delete_title), fontWeight = FontWeight.Bold) },
                    text = { Text(stringResource(Res.string.emp_delete_confirm)) },
                    confirmButton = {
                        TextButton(onClick = { 
                            viewModel.eliminarEmpleado(currentEstablecimientoId, emp.uid)
                            showDeleteDialog = false
                            selectedEmpleado = null
                        }) {
                            Text(stringResource(Res.string.menu_delete_btn), fontWeight = FontWeight.Bold, color = TrafficRed)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(Res.string.btn_cancel), color = DarkGray)
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
        colors = CardDefaults.cardColors(containerColor = LightSurface),
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
                    color = DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = empleado.correo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MediumGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                // Traducción de roles
                val rolesText = empleado.roles.map { role ->
                    when (role.lowercase()) {
                        "cajero" -> stringResource(Res.string.emp_role_cashier)
                        "cocina" -> stringResource(Res.string.emp_role_kitchen)
                        "entrega" -> stringResource(Res.string.emp_role_delivery)
                        "supervisor" -> stringResource(Res.string.emp_role_supervisor)
                        "admin" -> stringResource(Res.string.emp_role_admin_local)
                        else -> role
                    }
                }.joinToString(" • ").uppercase()

                Text(
                    text = rolesText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryOrange,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))

                val badgeBg = if (empleado.activo) Color(0xFFE8F5E9) else ExtraLightGray
                val badgeColor = if (empleado.activo) TrafficGreen else MediumGray
                Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = if (empleado.activo) stringResource(Res.string.emp_status_active) else stringResource(Res.string.emp_status_inactive),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                Icons.Default.MoreVert,
                contentDescription = stringResource(Res.string.est_options),
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
                contentDescription = stringResource(Res.string.emp_profile_photo),
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