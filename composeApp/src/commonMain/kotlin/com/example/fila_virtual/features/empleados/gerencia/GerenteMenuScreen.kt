package com.example.fila_virtual.features.empleados.gerencia

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightBackground
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.BorderGray
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.EmpleadoDetalle
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.features.admin.EstablecimientoViewModel
import com.example.fila_virtual.features.admin.ProductoViewModel
import com.example.fila_virtual.features.admin.empleados.AddEmployeeScreen
import com.example.fila_virtual.features.admin.empleados.EmpleadoViewModel
import com.example.fila_virtual.features.admin.empleados.EmployeesScreen
import com.example.fila_virtual.features.admin.menu.AddDishScreen
import com.example.fila_virtual.features.admin.menu.MenuScreen
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_back
import fila_virtual.composeapp.generated.resources.emp_count_listo
import fila_virtual.composeapp.generated.resources.emp_count_preparacion
import fila_virtual.composeapp.generated.resources.emp_count_recibido
import fila_virtual.composeapp.generated.resources.emp_gerente_order_turn
import fila_virtual.composeapp.generated.resources.emp_gerente_queue_empty
import fila_virtual.composeapp.generated.resources.emp_gerente_queue_help
import fila_virtual.composeapp.generated.resources.emp_gerente_tab_queue
import fila_virtual.composeapp.generated.resources.emp_gerente_title
import fila_virtual.composeapp.generated.resources.admin_nav_employees
import fila_virtual.composeapp.generated.resources.admin_nav_menu
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@Composable
fun GerenteMenuScreen(
    establecimientoId: String,
    establecimientoNombre: String,
    onBack: () -> Unit,
    mostrarEmpleadosInicial: Boolean = false,
    mostrarSoloSeccion: Boolean = false,
    viewModel: GerenteViewModel = viewModel()
) {
    LaunchedEffect(establecimientoId) {
        viewModel.escuchar(establecimientoId)
    }

    var seccion by rememberSaveable(establecimientoId, mostrarEmpleadosInicial) {
        mutableStateOf(if (mostrarEmpleadosInicial) Seccion.EMPLEADOS else Seccion.GESTION_MENU)
    }
    var mostrarFormularioPlatillo by rememberSaveable { mutableStateOf(false) }
    var productoAEditar by remember { mutableStateOf<Producto?>(null) }
    var mostrarFormularioEmpleado by rememberSaveable { mutableStateOf(false) }
    var empleadoAEditar by remember { mutableStateOf<EmpleadoDetalle?>(null) }
    val productoViewModel: ProductoViewModel = viewModel()
    val empleadoViewModel: EmpleadoViewModel = viewModel()
    val establecimientoViewModel: EstablecimientoViewModel = viewModel()

    if (mostrarFormularioPlatillo && viewModel.ownerUid.isNotBlank()) {
        AddDishScreen(
            establecimientoId = establecimientoId,
            ownerUid = viewModel.ownerUid,
            productoToEdit = productoAEditar,
            onBack = {
                mostrarFormularioPlatillo = false
                productoAEditar = null
            },
            viewModel = productoViewModel,
            establecimientoViewModel = establecimientoViewModel,
            permitirCambiarEstablecimiento = false,
            establecimientoNombreFijo = establecimientoNombre
        )
        return
    }
    if (mostrarFormularioEmpleado && viewModel.ownerUid.isNotBlank()) {
        AddEmployeeScreen(
            empleado = empleadoAEditar,
            establecimientoId = establecimientoId,
            ownerUid = viewModel.ownerUid,
            viewModel = empleadoViewModel,
            onNavigateBack = {
                mostrarFormularioEmpleado = false
                empleadoAEditar = null
            },
            permitirCambiarEstablecimiento = false,
            establecimientoNombreFijo = establecimientoNombre
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        if (!mostrarSoloSeccion) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.emp_back),
                    tint = DarkGray,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onBack)
                        .padding(12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.emp_gerente_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray
                    )
                    if (establecimientoNombre.isNotBlank()) {
                        Text(establecimientoNombre, color = MediumGray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        when {
            viewModel.cargandoOwner -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                AppListSkeleton(Modifier.fillMaxSize().padding(horizontal = 24.dp), itemCount = 4)
            }
            viewModel.ownerUid.isBlank() -> Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    viewModel.errorOwner.ifBlank { "No se pudo verificar el dueño del local." },
                    color = TrafficRed,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
            else -> {
                Spacer(Modifier.height(12.dp))
                if (!mostrarSoloSeccion) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SeccionGerente(
                            texto = stringResource(Res.string.admin_nav_menu),
                            activa = seccion == Seccion.GESTION_MENU,
                            modifier = Modifier.weight(1f),
                            onClick = { seccion = Seccion.GESTION_MENU }
                        )
                        SeccionGerente(
                            texto = stringResource(Res.string.admin_nav_employees),
                            activa = seccion == Seccion.EMPLEADOS,
                            modifier = Modifier.weight(1f),
                            onClick = { seccion = Seccion.EMPLEADOS }
                        )
                        SeccionGerente(
                            texto = stringResource(Res.string.emp_gerente_tab_queue),
                            activa = seccion == Seccion.PEDIDOS,
                            modifier = Modifier.weight(1f),
                            onClick = { seccion = Seccion.PEDIDOS }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                }
                when (seccion) {
                    Seccion.GESTION_MENU -> MenuScreen(
                        establecimientoId = establecimientoId,
                        ownerUid = viewModel.ownerUid,
                        onNavigateToAdd = {
                            productoAEditar = null
                            mostrarFormularioPlatillo = true
                        },
                        onNavigateToEdit = { producto ->
                            productoAEditar = producto
                            mostrarFormularioPlatillo = true
                        },
                        viewModel = productoViewModel,
                        establecimientoViewModel = establecimientoViewModel,
                        permitirCambiarEstablecimiento = false,
                        establecimientoNombreFijo = establecimientoNombre
                    )
                    Seccion.EMPLEADOS -> EmployeesScreen(
                        establecimientoId = establecimientoId,
                        ownerUid = viewModel.ownerUid,
                        onNavigateToAdd = {
                            empleadoAEditar = null
                            mostrarFormularioEmpleado = true
                        },
                        onEditEmpleado = { empleado, _ ->
                            empleadoAEditar = empleado
                            mostrarFormularioEmpleado = true
                        },
                        viewModel = empleadoViewModel,
                        establecimientoViewModel = establecimientoViewModel,
                        permitirCambiarEstablecimiento = false,
                        establecimientoNombreFijo = establecimientoNombre
                    )
                    Seccion.PEDIDOS -> AppPullToRefreshBox(
                        isRefreshing = viewModel.cargandoCola,
                        onRefresh = { viewModel.escuchar(establecimientoId, forceRefresh = true) },
                        modifier = Modifier.weight(1f)
                    ) {
                        if (viewModel.cargandoCola && viewModel.pedidos.isEmpty()) {
                            AppListSkeleton(Modifier.fillMaxSize().padding(horizontal = 24.dp), itemCount = 4)
                        } else {
                            ColaTurnosGerente(
                                pedidos = viewModel.pedidos,
                                error = viewModel.errorCola,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class Seccion {
    GESTION_MENU,
    EMPLEADOS,
    PEDIDOS
}

@Composable
private fun SeccionGerente(
    texto: String,
    activa: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = texto,
        color = if (activa) LightSurface else DarkGray,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (activa) PrimaryOrange else LightSurface)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}

@Composable
private fun ColaTurnosGerente(
    pedidos: List<Pedido>,
    error: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(Res.string.emp_gerente_queue_help),
            color = MediumGray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        if (error.isNotBlank()) {
            Text(
                error,
                color = TrafficRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
        if (pedidos.isEmpty()) {
            Text(
                stringResource(Res.string.emp_gerente_queue_empty),
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                textAlign = TextAlign.Center,
                color = MediumGray
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pedidos, key = { it.id }) { pedido ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightSurface),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                stringResource(Res.string.emp_gerente_order_turn, pedido.turno),
                                fontWeight = FontWeight.Bold,
                                color = DarkGray
                            )
                            Text(etiquetaEstado(pedido.estado), color = PrimaryOrange, style = MaterialTheme.typography.labelMedium)
                            if (pedido.productos.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    pedido.productos.joinToString { "${it.cantidad} ${it.nombre}" },
                                    color = MediumGray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(dineroPlatillo(pedido.total), color = DarkGray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun etiquetaEstado(estado: EstadoPedido): String {
    return when (estado) {
        EstadoPedido.PENDIENTE, EstadoPedido.RECIBIDO -> stringResource(Res.string.emp_count_recibido)
        EstadoPedido.EN_PREPARACION -> stringResource(Res.string.emp_count_preparacion)
        EstadoPedido.LISTO -> stringResource(Res.string.emp_count_listo)
        else -> estado.name
    }
}

private fun dineroPlatillo(total: Double): String {
    val centavos = round(total * 100).toInt()
    val pesos = centavos / 100
    val cents = abs(centavos % 100).toString().padStart(2, '0')
    return "$$pesos.$cents"
}

