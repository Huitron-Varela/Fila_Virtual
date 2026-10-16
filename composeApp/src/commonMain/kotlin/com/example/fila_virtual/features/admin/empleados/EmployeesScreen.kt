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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Email
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
import com.example.fila_virtual.components.AppSnackbar
import com.example.fila_virtual.components.AppSnackbarHost
import com.example.fila_virtual.components.SearchBar
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.EmpleadoDetalle
import com.example.fila_virtual.features.admin.FormState

import com.example.fila_virtual.features.admin.EstablecimientoViewModel
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    establecimientoId: String,
    ownerUid: String,
    onNavigateToAdd: (String) -> Unit,
    onEditEmpleado: (EmpleadoDetalle, String) -> Unit,
    viewModel: EmpleadoViewModel = viewModel(),
    establecimientoViewModel: EstablecimientoViewModel = viewModel(),
    permitirCambiarEstablecimiento: Boolean = true,
    establecimientoNombreFijo: String = ""
) {
    val windowSize = LocalWindowSize.current
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val horizontalPadding = windowSize.compactDp(24)

    val uiState by viewModel.uiState.collectAsState()
    val empleados by viewModel.empleados.collectAsState()
    val establecimientos by establecimientoViewModel.establecimientos.collectAsState()
    val pedidosEmpleado by viewModel.pedidosEmpleado.collectAsState()
    val cargandoPedidosEmpleado by viewModel.cargandoPedidosEmpleado.collectAsState()
    val errorPedidosEmpleado by viewModel.errorPedidosEmpleado.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    
    var currentEstablecimientoId by remember { mutableStateOf(if (establecimientoId.isEmpty()) "TODOS" else establecimientoId) }
    var showSucursalSelector by remember { mutableStateOf(false) }

    var selectedEmpleado by remember { mutableStateOf<EmpleadoDetalle?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEmployeeOrders by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is FormState.Error) {
            snackbarHostState.showSnackbar((uiState as FormState.Error).message)
        }
    }


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

    val sucursalActual = establecimientoNombreFijo.ifBlank { when (currentEstablecimientoId) {
        "TODOS" -> stringResource(Res.string.est_all_branches)
        "" -> stringResource(Res.string.emp_select_branch)
        else -> establecimientos.find { it.id == currentEstablecimientoId }?.nombre ?: stringResource(Res.string.emp_unknown_branch)
    } }

    Scaffold(
        containerColor = LightBackground,
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
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
                .fillMaxSize()
                .padding()
                .padding(horizontal = horizontalPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = windowSize.compactDp(24), bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.emp_title_manage),
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

            if (currentEstablecimientoId == "TODOS" || currentEstablecimientoId.isNotEmpty()) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = stringResource(Res.string.emp_search),
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
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.People, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(Res.string.est_all_branches), fontWeight = FontWeight.Bold, color = PrimaryOrange)
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
                }

                Spacer(modifier = Modifier.height(20.dp))
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
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No se pudo actualizar la lista de empleados.",
                                color = MediumGray,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            TextButton(
                                onClick = {
                                    if (currentEstablecimientoId == "TODOS") {
                                        viewModel.cargarTodosLosEmpleados(establecimientos.map { it.id })
                                    } else {
                                        viewModel.cargarEmpleados(currentEstablecimientoId)
                                    }
                                }
                            ) {
                                Text(stringResource(Res.string.emp_retry), color = PrimaryOrange, fontWeight = FontWeight.Bold)
                            }
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
                                    onClick = {
                                        showEmployeeOrders = false
                                        selectedEmpleado = empleado
                                    }
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
                onDismissRequest = {
                    selectedEmpleado = null
                    showEmployeeOrders = false
                },
                sheetState = sheetState,
                containerColor = LightSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .heightIn(max = windowSize.compactDp(640))
                        .padding(horizontal = horizontalPadding)
                        .padding(bottom = 32.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(84.dp)) {
                        EmployeeAvatar(
                            fotoUrl = emp.fotoUrl,
                            modifier = Modifier.fillMaxSize(),
                            iconSize = 42.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = emp.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    val rolesText = emp.roles.map { role ->
                        when (role.lowercase()) {
                            "cajero" -> stringResource(Res.string.emp_role_cashier)
                            "cocina" -> stringResource(Res.string.emp_role_kitchen)
                            "entrega" -> stringResource(Res.string.emp_role_delivery)
                            "gerente" -> stringResource(Res.string.emp_role_gerente)
                            "supervisor" -> stringResource(Res.string.emp_role_supervisor)
                            "admin" -> stringResource(Res.string.emp_role_admin_local)
                            else -> role
                        }
                    }.joinToString(" • ")

                    Surface(
                        color = if (emp.activo) Color(0xFFE8F5E9) else ExtraLightGray,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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

                    if (rolesText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = SoftOrangeBg, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                rolesText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryOrange,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = BorderGray.copy(alpha = 0.7f))
                    Text(
                        "Información del empleado",
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                    
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

                    OutlinedButton(
                        onClick = {
                            showEmployeeOrders = true
                            val establecimientoIds = if (currentEstablecimientoId == "TODOS") {
                                establecimientos.map { it.id }
                            } else {
                                listOf(currentEstablecimientoId)
                            }
                            viewModel.cargarPedidosEntregadosEmpleado(emp.uid, establecimientoIds)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, PrimaryOrange.copy(alpha = 0.55f))
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver pedidos entregados", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { 
                            selectedEmpleado = null
                            onEditEmpleado(
                                emp,
                                if (currentEstablecimientoId == "TODOS") "" else currentEstablecimientoId
                            ) 
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

            if (showEmployeeOrders) {
                AlertDialog(
                    onDismissRequest = { showEmployeeOrders = false },
                    title = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Pedidos entregados", fontWeight = FontWeight.Bold, color = DarkGray)
                            Text(emp.nombre, style = MaterialTheme.typography.bodySmall, color = MediumGray)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.widthIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "${pedidosEmpleado.size} pedidos entregados",
                                style = MaterialTheme.typography.labelLarge,
                                color = PrimaryOrange,
                                fontWeight = FontWeight.Bold
                            )
                            when {
                                cargandoPedidosEmpleado -> Box(
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PrimaryOrange)
                                }
                                errorPedidosEmpleado != null -> AppSnackbar(
                                    message = errorPedidosEmpleado.orEmpty(),
                                    containerColor = Color(0xFFFFEBEE),
                                    contentColor = TrafficRed
                                )
                                pedidosEmpleado.isEmpty() -> Text(
                                    "Este empleado aún no tiene pedidos entregados.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MediumGray
                                )
                                else -> LazyColumn(
                                    modifier = Modifier.heightIn(max = 340.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(pedidosEmpleado, key = { it.id }) { pedido ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            color = ExtraLightGray,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    val identificador = pedido.turno.takeIf { it > 0 }
                                                        ?.toString() ?: pedido.id.takeLast(6)
                                                    Text(
                                                        "Pedido #$identificador",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DarkGray
                                                    )
                                                    val fecha = pedido.actualizadoAt.takeIf { it > 0L }
                                                        ?: pedido.createdAt
                                                    Text(
                                                        formatPedidoFecha(fecha),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MediumGray
                                                    )
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text(
                                                        "Entregado",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = TrafficGreen,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        "MXN ${pedido.total}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = DarkGray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showEmployeeOrders = false }) {
                            Text("Cerrar", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = LightSurface,
                    shape = RoundedCornerShape(16.dp)
                )
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
}

private fun formatPedidoFecha(timestamp: Long): String {
    if (timestamp <= 0L) return "Fecha no disponible"
    val fecha = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val dia = fecha.dayOfMonth.toString().padStart(2, '0')
    val mes = fecha.monthNumber.toString().padStart(2, '0')
    val hora = fecha.hour.toString().padStart(2, '0')
    val minuto = fecha.minute.toString().padStart(2, '0')
    return "$dia/$mes/${fecha.year} · $hora:$minuto"
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(56.dp)) {
                EmployeeAvatar(
                    fotoUrl = empleado.fotoUrl,
                    modifier = Modifier.size(56.dp),
                    iconSize = 28.dp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = empleado.nombre,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (empleado.activo) Color(0xFFE8F5E9) else ExtraLightGray,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (empleado.activo) stringResource(Res.string.emp_status_active) else stringResource(Res.string.emp_status_inactive),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (empleado.activo) TrafficGreen else MediumGray,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
                Text(
                    text = empleado.correo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MediumGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                
                // Traducción de roles
                val rolesText = empleado.roles.map { role ->
                    when (role.lowercase()) {
                        "cajero" -> stringResource(Res.string.emp_role_cashier)
                        "cocina" -> stringResource(Res.string.emp_role_kitchen)
                        "entrega" -> stringResource(Res.string.emp_role_delivery)
                        "gerente" -> stringResource(Res.string.emp_role_gerente)
                        "supervisor" -> stringResource(Res.string.emp_role_supervisor)
                        "admin" -> stringResource(Res.string.emp_role_admin_local)
                        else -> role
                    }
                }.joinToString(" • ").uppercase()

                if (rolesText.isNotBlank()) {
                    Surface(color = SoftOrangeBg, shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = rolesText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryOrange,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = PrimaryOrange,
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