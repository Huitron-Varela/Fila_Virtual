package com.example.fila_virtual.features.empleados

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.FormImagePicker
import com.example.fila_virtual.components.FormTextField
import com.example.fila_virtual.core.PermissionType
import com.example.fila_virtual.core.SelectedImage
import com.example.fila_virtual.core.rememberImagePicker
import com.example.fila_virtual.core.rememberPermissionsManager
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightBackground
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.SoftOrangeBg
import com.example.fila_virtual.core.theme.SoftRedBg
import com.example.fila_virtual.core.theme.BorderGray
import com.example.fila_virtual.core.theme.TrafficGreen
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.features.admin.menu.ProductMenuCategories
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_back
import fila_virtual.composeapp.generated.resources.emp_count_listo
import fila_virtual.composeapp.generated.resources.emp_count_preparacion
import fila_virtual.composeapp.generated.resources.emp_count_recibido
import fila_virtual.composeapp.generated.resources.emp_gerente_add
import fila_virtual.composeapp.generated.resources.emp_gerente_available
import fila_virtual.composeapp.generated.resources.emp_gerente_baja
import fila_virtual.composeapp.generated.resources.emp_gerente_baja_body
import fila_virtual.composeapp.generated.resources.emp_gerente_baja_cancel
import fila_virtual.composeapp.generated.resources.emp_gerente_baja_note
import fila_virtual.composeapp.generated.resources.emp_gerente_baja_title
import fila_virtual.composeapp.generated.resources.emp_gerente_stock_note
import fila_virtual.composeapp.generated.resources.emp_gerente_category
import fila_virtual.composeapp.generated.resources.emp_gerente_edit
import fila_virtual.composeapp.generated.resources.emp_gerente_empty
import fila_virtual.composeapp.generated.resources.emp_gerente_help
import fila_virtual.composeapp.generated.resources.emp_gerente_new
import fila_virtual.composeapp.generated.resources.emp_gerente_no_results
import fila_virtual.composeapp.generated.resources.emp_gerente_order_turn
import fila_virtual.composeapp.generated.resources.emp_gerente_queue_empty
import fila_virtual.composeapp.generated.resources.emp_gerente_queue_help
import fila_virtual.composeapp.generated.resources.emp_gerente_save
import fila_virtual.composeapp.generated.resources.emp_gerente_search
import fila_virtual.composeapp.generated.resources.emp_gerente_tab_menu
import fila_virtual.composeapp.generated.resources.emp_gerente_tab_queue
import fila_virtual.composeapp.generated.resources.emp_gerente_title
import fila_virtual.composeapp.generated.resources.emp_gerente_unavailable
import fila_virtual.composeapp.generated.resources.menu_dish_image
import fila_virtual.composeapp.generated.resources.emp_stock_mark
import fila_virtual.composeapp.generated.resources.emp_stock_restore
import fila_virtual.composeapp.generated.resources.menu_dish_desc
import fila_virtual.composeapp.generated.resources.menu_dish_desc_placeholder
import fila_virtual.composeapp.generated.resources.menu_dish_name
import fila_virtual.composeapp.generated.resources.menu_dish_price
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@Composable
fun GerenteMenuScreen(
    establecimientoId: String,
    establecimientoNombre: String,
    onBack: () -> Unit,
    viewModel: GerenteViewModel = viewModel()
) {
    LaunchedEffect(establecimientoId) {
        viewModel.escuchar(establecimientoId)
    }

    var creando by rememberSaveable { mutableStateOf(false) }
    var editandoId by rememberSaveable { mutableStateOf<String?>(null) }
    var verCola by rememberSaveable { mutableStateOf(false) }
    var busqueda by rememberSaveable { mutableStateOf("") }
    val enEdicion = editandoId?.let { id -> viewModel.productos.find { it.id == id } }
    LaunchedEffect(editandoId, creando, viewModel.cargando, viewModel.productos) {
        if (!creando && editandoId != null && !viewModel.cargando && enEdicion == null) {
            editandoId = null
        }
    }

    if (creando || enEdicion != null) {
        EditorPlatillo(
            titulo = stringResource(if (creando) Res.string.emp_gerente_new else Res.string.emp_gerente_edit),
            inicial = if (creando) null else enEdicion,
            guardando = viewModel.guardando,
            error = viewModel.error,
            onBack = {
                creando = false
                editandoId = null
            },
            onGuardar = { nombre, descripcion, precio, categoria, imagen ->
                viewModel.guardar(
                    productoId = if (creando) "" else editandoId.orEmpty(),
                    nombre = nombre,
                    descripcion = descripcion,
                    precioTexto = precio,
                    categoria = categoria,
                    imagenBytes = imagen?.bytes,
                    imagenMime = imagen?.mimeType ?: "image/jpeg",
                    imagenUrlActual = if (creando) "" else enEdicion?.imagenUrl.orEmpty()
                ) {
                    creando = false
                    editandoId = null
                }
            },
            onAgotado = enEdicion?.let { platillo -> { viewModel.cambiarDisponibilidad(platillo) } },
            onQuitar = enEdicion?.let { platillo ->
                {
                    viewModel.darDeBaja(platillo) { listo ->
                        if (listo) editandoId = null
                    }
                }
            },
            cambiandoDisponibilidad = viewModel.productoEnCambio == enEdicion?.id
        )
        return
    }
    if (editandoId != null) {
        Box(modifier = Modifier.fillMaxSize().background(LightBackground), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryOrange)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
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
        Text(
            stringResource(Res.string.emp_gerente_help),
            color = MediumGray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        if (viewModel.aviso.isNotBlank()) {
            Text(
                viewModel.aviso,
                color = TrafficGreen,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
        if (viewModel.error.isNotBlank()) {
            Text(
                viewModel.error,
                color = TrafficRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SeccionGerente(
                texto = stringResource(Res.string.emp_gerente_tab_menu),
                activa = !verCola,
                modifier = Modifier.weight(1f),
                onClick = { verCola = false }
            )
            SeccionGerente(
                texto = stringResource(Res.string.emp_gerente_tab_queue),
                activa = verCola,
                modifier = Modifier.weight(1f),
                onClick = { verCola = true }
            )
        }
        Spacer(Modifier.height(12.dp))
        if (verCola) {
            ColaTurnosGerente(
                pedidos = viewModel.pedidos,
                error = viewModel.errorCola,
                modifier = Modifier.weight(1f)
            )
        } else {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                singleLine = true,
                placeholder = { Text(stringResource(Res.string.emp_gerente_search), color = MediumGray) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryOrange,
                    unfocusedBorderColor = BorderGray,
                    focusedTextColor = DarkGray,
                    unfocusedTextColor = DarkGray,
                    focusedContainerColor = LightSurface,
                    unfocusedContainerColor = LightSurface
                )
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { creando = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                Text(stringResource(Res.string.emp_gerente_add), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            val consulta = busqueda.trim()
            val visibles = if (consulta.isBlank()) {
                viewModel.productos
            } else {
                viewModel.productos.filter { producto ->
                    producto.nombre.contains(consulta, ignoreCase = true) ||
                        producto.categoria.contains(consulta, ignoreCase = true)
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    viewModel.cargando && viewModel.productos.isEmpty() -> {
                        CircularProgressIndicator(color = PrimaryOrange, modifier = Modifier.align(Alignment.Center))
                    }
                    viewModel.productos.isEmpty() -> {
                        Text(
                            stringResource(Res.string.emp_gerente_empty),
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MediumGray
                        )
                    }
                    visibles.isEmpty() -> {
                        Text(
                            stringResource(Res.string.emp_gerente_no_results),
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = MediumGray
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(visibles, key = { it.id }) { producto ->
                                PlatilloGerenteCard(
                                    producto = producto,
                                    onEditar = { editandoId = producto.id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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

@Composable
private fun PlatilloGerenteCard(
    producto: Producto,
    onEditar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditar),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    producto.nombre.ifBlank { "—" },
                    fontWeight = FontWeight.Bold,
                    color = DarkGray,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    listOf(producto.categoria, dineroPlatillo(producto.precio))
                        .filter { it.isNotBlank() }
                        .joinToString(" · "),
                    color = MediumGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = stringResource(
                    if (producto.disponible) Res.string.emp_gerente_available else Res.string.emp_gerente_unavailable
                ),
                color = if (producto.disponible) TrafficGreen else TrafficRed,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (producto.disponible) SoftOrangeBg else SoftRedBg)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(Res.string.emp_gerente_edit),
                tint = MediumGray
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditorPlatillo(
    titulo: String,
    inicial: Producto?,
    guardando: Boolean,
    error: String,
    onBack: () -> Unit,
    onGuardar: (nombre: String, descripcion: String, precio: String, categoria: String, imagen: SelectedImage?) -> Unit,
    onAgotado: (() -> Unit)? = null,
    onQuitar: (() -> Unit)? = null,
    cambiandoDisponibilidad: Boolean = false
) {
    var confirmarBaja by remember { mutableStateOf(false) }
    val clave = inicial?.id ?: "nuevo"
    var imagen by remember(clave) { mutableStateOf<SelectedImage?>(null) }
    val imagePicker = rememberImagePicker { imagen = it }
    val permissions = rememberPermissionsManager()
    val pedirFoto = {
        if (permissions.isPermissionGranted(PermissionType.GALLERY)) {
            imagePicker()
        } else {
            permissions.askPermission(PermissionType.GALLERY) { granted ->
                if (granted) imagePicker()
            }
        }
    }
    var nombre by remember(clave) { mutableStateOf(inicial?.nombre.orEmpty()) }
    var descripcion by remember(clave) { mutableStateOf(inicial?.descripcion.orEmpty()) }
    var precio by remember(clave) {
        mutableStateOf(if (inicial == null || inicial.precio <= 0.0) "" else inicial.precio.textoPrecio())
    }
    val opciones = remember(clave, inicial?.categoria) {
        val base = ProductMenuCategories.options
        val actual = inicial?.categoria.orEmpty()
        if (actual.isNotBlank() && actual !in base) base + actual else base
    }
    var categoria by remember(clave) { mutableStateOf(inicial?.categoria?.ifBlank { opciones.firstOrNull().orEmpty() }.orEmpty()) }

    if (confirmarBaja && onQuitar != null) {
        AlertDialog(
            onDismissRequest = { if (!guardando) confirmarBaja = false },
            title = { Text(stringResource(Res.string.emp_gerente_baja_title)) },
            text = { Text(stringResource(Res.string.emp_gerente_baja_body)) },
            confirmButton = {
                TextButton(
                    enabled = !guardando,
                    onClick = {
                        confirmarBaja = false
                        onQuitar()
                    }
                ) {
                    Text(stringResource(Res.string.emp_gerente_baja), color = TrafficRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarBaja = false }, enabled = !guardando) {
                    Text(stringResource(Res.string.emp_gerente_baja_cancel), color = PrimaryOrange)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .verticalScroll(rememberScrollState())
    ) {
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
                    .clickable(enabled = !guardando, onClick = onBack)
                    .padding(12.dp)
            )
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )
        }
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            FormImagePicker(
                label = stringResource(Res.string.menu_dish_image),
                onClick = pedirFoto,
                hasImage = imagen != null || !inicial?.imagenUrl.isNullOrBlank(),
                imageUrl = inicial?.imagenUrl.orEmpty(),
                previewBytes = imagen?.bytes
            )
            Spacer(Modifier.height(16.dp))
            FormTextField(
                label = stringResource(Res.string.menu_dish_name),
                value = nombre,
                onValueChange = { nombre = it }
            )
            Spacer(Modifier.height(16.dp))
            FormTextField(
                label = stringResource(Res.string.menu_dish_desc),
                value = descripcion,
                onValueChange = { descripcion = it },
                placeholder = stringResource(Res.string.menu_dish_desc_placeholder),
                singleLine = false,
                minHeight = 110
            )
            Spacer(Modifier.height(16.dp))
            FormTextField(
                label = stringResource(Res.string.menu_dish_price),
                value = precio,
                onValueChange = { precio = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(Res.string.emp_gerente_category),
                color = DarkGray,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                opciones.forEach { opcion ->
                    val elegida = opcion == categoria
                    Text(
                        text = opcion,
                        color = if (elegida) LightSurface else DarkGray,
                        fontWeight = if (elegida) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (elegida) PrimaryOrange else LightSurface)
                            .clickable { categoria = opcion }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
            if (error.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = TrafficRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onGuardar(nombre, descripcion, precio, categoria, imagen) },
                enabled = !guardando,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                if (guardando) {
                    CircularProgressIndicator(color = LightSurface, modifier = Modifier.height(22.dp))
                } else {
                    Text(stringResource(Res.string.emp_gerente_save), fontWeight = FontWeight.Bold)
                }
            }
            if (inicial != null && onAgotado != null) {
                Spacer(Modifier.height(28.dp))
                Text(
                    stringResource(Res.string.emp_gerente_stock_note),
                    color = MediumGray,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onAgotado,
                    enabled = !guardando && !cambiandoDisponibilidad,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(
                            if (inicial.disponible) Res.string.emp_stock_mark else Res.string.emp_stock_restore
                        ),
                        color = if (inicial.disponible) PrimaryOrange else TrafficRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (onQuitar != null) {
                Spacer(Modifier.height(20.dp))
                Text(
                    stringResource(Res.string.emp_gerente_baja_note),
                    color = MediumGray,
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(
                    onClick = { confirmarBaja = true },
                    enabled = !guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.emp_gerente_baja), color = TrafficRed, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun dineroPlatillo(total: Double): String {
    val centavos = round(total * 100).toInt()
    val pesos = centavos / 100
    val cents = abs(centavos % 100).toString().padStart(2, '0')
    return "$$pesos.$cents"
}

private fun Double.textoPrecio(): String {
    val centavos = round(this * 100).toInt()
    val pesos = centavos / 100
    val cents = abs(centavos % 100).toString().padStart(2, '0')
    return "$pesos.$cents"
}
