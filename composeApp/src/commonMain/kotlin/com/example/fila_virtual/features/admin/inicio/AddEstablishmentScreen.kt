package com.example.fila_virtual.features.admin.inicio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.*
import com.example.fila_virtual.data.*
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.features.admin.EstablecimientoViewModel
import com.example.fila_virtual.features.admin.FormState
import com.example.fila_virtual.core.BackHandler
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.SelectedImage
import com.example.fila_virtual.core.PermissionType
import com.example.fila_virtual.core.rememberPermissionsManager
import com.example.fila_virtual.core.rememberImagePicker
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.datetime.Clock
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEstablishmentScreen(
    ownerUid: String,
    onBack: () -> Unit,
    establecimientoToEdit: Establecimiento? = null,
    viewModel: EstablecimientoViewModel = viewModel()
) {
    val windowSize = LocalWindowSize.current
    val formPadding = windowSize.compactDp(16)
    val allText = stringResource(Res.string.est_all)
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val snackbarHostState = remember { SnackbarHostState() }
    BackHandler(onBack = onBack)

    // Estados de los campos
    var nombre by remember { mutableStateOf(establecimientoToEdit?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(establecimientoToEdit?.descripcion ?: "") }
    var direccion by remember { mutableStateOf(establecimientoToEdit?.ubicacion?.direccion ?: "") }
    var selectedImage by remember { mutableStateOf<SelectedImage?>(null) }
    val imagePicker = rememberImagePicker { selectedImage = it }
    val permissions = rememberPermissionsManager()
    val requestImage = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (permissions.isPermissionGranted(PermissionType.GALLERY)) {
            imagePicker()
        } else {
            permissions.askPermission(PermissionType.GALLERY) { granted ->
                if (granted) imagePicker()
            }
        }
    }
    
    // Horario
    var apertura by remember { mutableStateOf(establecimientoToEdit?.horario?.get(allText)?.apertura ?: "09:00 AM") }
    var cierre by remember { mutableStateOf(establecimientoToEdit?.horario?.get(allText)?.cierre ?: "10:00 PM") }

    var showAperturaPicker by remember { mutableStateOf(false) }
    var showCierrePicker by remember { mutableStateOf(false) }
    var showSuccessSheet by remember { mutableStateOf(false) }

    val categoriasDisponibles = listOf("Cafetería", "Restaurante", "Comida Rápida", "Bar")
    var categoriasSeleccionadas by remember { mutableStateOf(establecimientoToEdit?.categorias ?: listOf<String>()) }

    // Validación: Nombre, dirección y al menos una categoría
    val isFormValid = nombre.trim().isNotBlank() && direccion.trim().isNotBlank() && categoriasSeleccionadas.isNotEmpty()

    LaunchedEffect(uiState) {
        if (uiState is FormState.Error) {
            snackbarHostState.showSnackbar((uiState as FormState.Error).message)
        }
    }

    // Modal de éxito (Bottom Sheet)
    if (showSuccessSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSuccessSheet = false
                viewModel.resetState()
                onBack() 
            },
            containerColor = LightBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryOrange,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = if (establecimientoToEdit == null) "¡Establecimiento Registrado!" else "¡Establecimiento Actualizado!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = if (establecimientoToEdit == null) "El establecimiento se ha guardado correctamente. Ahora puedes empezar a gestionar su menú y empleados." else "Los datos del establecimiento se han actualizado correctamente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = formPadding)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showSuccessSheet = false
                        viewModel.resetState()
                        onBack() 
                    },
                    modifier = Modifier.fillMaxWidth().height(windowSize.compactDp(56)),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold, color = LightSurface)
                }
            }
        }
    }

    // Diálogos de selección de hora
    if (showAperturaPicker) {
        ScheduleTimeDialog(
            title = "Hora de apertura",
            selectedTime = apertura,
            onDismissRequest = { showAperturaPicker = false },
            onConfirm = { hour, minute ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                apertura = formatTime(hour, minute)
                showAperturaPicker = false
            }
        )
    }

    if (showCierrePicker) {
        ScheduleTimeDialog(
            title = "Hora de cierre",
            selectedTime = cierre,
            onDismissRequest = { showCierrePicker = false },
            onConfirm = { hour, minute ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                cierre = formatTime(hour, minute)
                showCierrePicker = false
            }
        )
    }

    BaseFormScreen(
        title = if (establecimientoToEdit == null) "Nuevo Establecimiento" else "Editar Establecimiento",
        onBack = onBack,
        isSaveEnabled = isFormValid,
        onSave = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            val nuevo = Establecimiento(
                id = establecimientoToEdit?.id ?: "",
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                ubicacion = Ubicacion(direccion = direccion.trim()),
                activo = establecimientoToEdit?.activo ?: false,
                ownerUid = ownerUid,
                categorias = categoriasSeleccionadas,
                horario = mapOf(allText to HorarioDia(apertura = apertura, cierre = cierre)),
                logoUrl = establecimientoToEdit?.logoUrl ?: "",
                createdAt = establecimientoToEdit?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
                updatedAt = Clock.System.now().toEpochMilliseconds()
            )
            viewModel.guardarEstablecimiento(
                establecimiento = nuevo,
                imageBytes = selectedImage?.bytes,
                imageMimeType = selectedImage?.mimeType ?: "image/jpeg"
            ) { showSuccessSheet = true }
        },
        saveButtonText = if (uiState is FormState.Loading) stringResource(Res.string.est_saving) else if (establecimientoToEdit == null) "Registrar Establecimiento" else stringResource(Res.string.est_save_changes),
        saveIcon = Icons.Default.Store,
        snackbarHostState = snackbarHostState
    ) {
        // Imagen de portada
        FormImagePicker(
            label = "Imagen de Portada / Logotipo",
            onClick = requestImage,
            hasImage = selectedImage != null || !establecimientoToEdit?.logoUrl.isNullOrBlank(),
            imageUrl = establecimientoToEdit?.logoUrl ?: "",
            previewBytes = selectedImage?.bytes
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Información General
        Text(
            text = "Información General",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        FormTextField(
            label = stringResource(Res.string.est_name),
            value = nombre,
            onValueChange = { nombre = it },
            placeholder = "Ej. El Naranjo Cafetería",
            leadingIcon = { Icon(Icons.Default.Store, null, tint = MediumGray, modifier = Modifier.size(20.dp)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormTextField(
            label = "Dirección",
            value = direccion,
            onValueChange = { direccion = it },
            placeholder = "Calle, Número y Colonia",
            leadingIcon = { Icon(Icons.Default.LocationOn, null, tint = MediumGray, modifier = Modifier.size(20.dp)) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormTextField(
            label = "Descripción (Opcional)",
            value = descripcion,
            onValueChange = { descripcion = it },
            placeholder = "¿Qué hace especial a este lugar?",
            singleLine = false,
            minHeight = 100,
            leadingIcon = { Icon(Icons.Default.Description, null, tint = MediumGray, modifier = Modifier.size(20.dp)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Selección de Categorías
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Categorías", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, color = DarkGray)
            Text("${categoriasSeleccionadas.size} seleccionadas", style = MaterialTheme.typography.labelSmall, color = MediumGray)
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoriasDisponibles.forEach { cat ->
                val isSelected = cat in categoriasSeleccionadas
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            categoriasSeleccionadas = if (isSelected) {
                                categoriasSeleccionadas - cat
                            } else {
                                categoriasSeleccionadas + cat
                            }
                    },
                    label = { Text(cat, maxLines = 1) },
                    leadingIcon = if (isSelected) {{ Icon(Icons.Default.Check, contentDescription = null) }} else null,
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryOrange,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = BorderGray,
                        selectedBorderColor = PrimaryOrange
                    )
                )
            }
        }
        if (categoriasSeleccionadas.isEmpty()) {
            Text(
                "Selecciona al menos una categoría para continuar.",
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MediumGray
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LightSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, BorderGray),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Horario de atención", fontWeight = FontWeight.Bold, color = DarkGray)
                        Text("Define cuándo estará abierto el local", style = MaterialTheme.typography.bodySmall, color = MediumGray)
                    }
                }

                if (windowSize.isSmallScreen) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ScheduleTimeOption(
                            label = "Apertura",
                            time = apertura,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAperturaPicker = true
                            }
                        )
                        ScheduleTimeOption(
                            label = "Cierre",
                            time = cierre,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showCierrePicker = true
                            }
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ScheduleTimeOption(
                            modifier = Modifier.weight(1f),
                            label = "Apertura",
                            time = apertura,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showAperturaPicker = true
                            }
                        )
                        ScheduleTimeOption(
                            modifier = Modifier.weight(1f),
                            label = "Cierre",
                            time = cierre,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showCierrePicker = true
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "El establecimiento se registrará como inactivo por defecto.",
            color = MediumGray,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ScheduleTimeOption(
    label: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        color = ExtraLightGray,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
                Text(time, style = MaterialTheme.typography.titleMedium, color = DarkGray, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.Default.Edit, contentDescription = "Cambiar hora de $label", tint = PrimaryOrange, modifier = Modifier.size(18.dp))
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    return "${h.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
}

private fun parseStoredTime(value: String?, fallbackHour: Int): Pair<Int, Int> {
    if (value.isNullOrBlank()) return fallbackHour to 0

    val parts = value.trim().split(Regex("\\s+"))
    val timeParts = parts.firstOrNull()?.split(":") ?: return fallbackHour to 0
    val displayedHour = timeParts.getOrNull(0)?.toIntOrNull() ?: return fallbackHour to 0
    val minute = timeParts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
    val meridiem = parts.getOrNull(1)?.uppercase()
    val hour = when {
        meridiem == "PM" && displayedHour in 1..11 -> displayedHour + 12
        meridiem == "AM" && displayedHour == 12 -> 0
        else -> displayedHour.coerceIn(0, 23)
    }
    return hour to minute
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ScheduleTimeDialog(
    title: String,
    selectedTime: String,
    onDismissRequest: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val initialTime = remember(selectedTime) { parseStoredTime(selectedTime, 9) }
    val initialHour = when (val hour = initialTime.first % 12) {
        0 -> 12
        else -> hour
    }
    var selectedHour by remember(selectedTime) { mutableIntStateOf(initialHour - 1) }
    var selectedMinute by remember(selectedTime) { mutableIntStateOf(initialTime.second) }
    var isPm by remember(selectedTime) { mutableStateOf(initialTime.first >= 12) }
    val hourValues = remember { (1..12).map { it.toString().padStart(2, '0') } }
    val minuteValues = remember { (0..59).map { it.toString().padStart(2, '0') } }
    val periodValues = remember { listOf("a.m.", "p.m.") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, color = DarkGray, fontWeight = FontWeight.Bold) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth().height(132.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                WheelTimeColumn(
                    values = hourValues,
                    selectedIndex = selectedHour,
                    modifier = Modifier.width(64.dp),
                    onSelected = { selectedHour = it }
                )
                Text(":", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = DarkGray)
                WheelTimeColumn(
                    values = minuteValues,
                    selectedIndex = selectedMinute,
                    modifier = Modifier.width(72.dp),
                    onSelected = { selectedMinute = it }
                )
                WheelTimeColumn(
                    values = periodValues,
                    selectedIndex = if (isPm) 1 else 0,
                    modifier = Modifier.width(64.dp),
                    onSelected = { isPm = it == 1 }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hour = selectedHour + 1
                    val hour24 = when {
                        isPm && hour != 12 -> hour + 12
                        !isPm && hour == 12 -> 0
                        else -> hour
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onConfirm(hour24, selectedMinute)
                }
            ) { Text("Confirmar", color = PrimaryOrange, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onDismissRequest()
            }) { Text("Cancelar", color = MediumGray) }
        },
        containerColor = Color.White.copy(alpha = 0.88f),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(16.dp)
    )
}

private const val WHEEL_BASE_INDEX = 1_000_080
private const val WHEEL_ITEM_COUNT = Int.MAX_VALUE

@Composable
private fun WheelTimeColumn(
    values: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = WHEEL_BASE_INDEX + selectedIndex
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(listState, values) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { itemIndex ->
                val offset = (itemIndex - WHEEL_BASE_INDEX) % values.size
                onSelected((offset + values.size) % values.size)
            }
    }

    LazyColumn(
        state = listState,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(vertical = 44.dp),
        modifier = modifier.height(132.dp)
    ) {
        items(count = WHEEL_ITEM_COUNT, key = { it }) { itemIndex ->
            val distance = kotlin.math.abs(itemIndex - listState.firstVisibleItemIndex)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        color = if (distance == 0) DarkGray.copy(alpha = 0.08f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val valueIndex = ((itemIndex - WHEEL_BASE_INDEX) % values.size + values.size) % values.size
                Text(
                    text = values[valueIndex],
                    color = if (distance == 0) DarkGray else MediumGray.copy(alpha = 0.55f),
                    style = if (distance == 0) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                    fontWeight = if (distance == 0) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}
