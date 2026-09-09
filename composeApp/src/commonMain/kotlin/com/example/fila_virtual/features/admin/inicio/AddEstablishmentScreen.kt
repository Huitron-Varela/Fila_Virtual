package com.example.fila_virtual.features.admin.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.*
import com.example.fila_virtual.data.*
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.features.admin.EstablecimientoViewModel
import com.example.fila_virtual.features.admin.FormState
import com.example.fila_virtual.core.BackHandler
import com.example.fila_virtual.core.SelectedImage
import com.example.fila_virtual.core.PermissionType
import com.example.fila_virtual.core.rememberPermissionsManager
import com.example.fila_virtual.core.rememberImagePicker
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEstablishmentScreen(
    ownerUid: String,
    onBack: () -> Unit,
    establecimientoToEdit: Establecimiento? = null,
    viewModel: EstablecimientoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    BackHandler(onBack = onBack)

    // Estados de los campos
    var nombre by remember { mutableStateOf(establecimientoToEdit?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(establecimientoToEdit?.descripcion ?: "") }
    var direccion by remember { mutableStateOf(establecimientoToEdit?.ubicacion?.direccion ?: "") }
    var selectedImage by remember { mutableStateOf<SelectedImage?>(null) }
    val imagePicker = rememberImagePicker { selectedImage = it }
    val permissions = rememberPermissionsManager()
    val requestImage = {
        if (permissions.isPermissionGranted(PermissionType.GALLERY)) {
            imagePicker()
        } else {
            permissions.askPermission(PermissionType.GALLERY) { granted ->
                if (granted) imagePicker()
            }
        }
    }
    
    // Horario
    var apertura by remember { mutableStateOf(establecimientoToEdit?.horario?.get("todos")?.apertura ?: "09:00 AM") }
    var cierre by remember { mutableStateOf(establecimientoToEdit?.horario?.get("todos")?.cierre ?: "10:00 PM") }
    
    // Estados para diálogos y selectores
    var showAperturaPicker by remember { mutableStateOf(false) }
    var showCierrePicker by remember { mutableStateOf(false) }
    var showSuccessSheet by remember { mutableStateOf(false) }

    val timePickerStateApertura = rememberTimePickerState(initialHour = 9, initialMinute = 0)
    val timePickerStateCierre = rememberTimePickerState(initialHour = 22, initialMinute = 0)

    val categoriasDisponibles = listOf("Cafetería", "Restaurante", "Comida Rápida", "Bar")
    var categoriasSeleccionadas by remember { mutableStateOf(establecimientoToEdit?.categorias ?: listOf<String>()) }

    // Validación: Nombre, dirección y al menos una categoría
    val isFormValid = nombre.trim().isNotBlank() && direccion.trim().isNotBlank() && categoriasSeleccionadas.isNotEmpty()

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
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { 
                        showSuccessSheet = false
                        viewModel.resetState()
                        onBack() 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // Diálogos de selección de hora
    if (showAperturaPicker) {
        TimePickerDialog(
            onDismissRequest = { showAperturaPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    apertura = formatTime(timePickerStateApertura.hour, timePickerStateApertura.minute)
                    showAperturaPicker = false
                }) { Text("Confirmar", color = PrimaryOrange) }
            },
            dismissButton = {
                TextButton(onClick = { showAperturaPicker = false }) { Text("Cancelar", color = MediumGray) }
            }
        ) { TimePicker(state = timePickerStateApertura) }
    }

    if (showCierrePicker) {
        TimePickerDialog(
            onDismissRequest = { showCierrePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    cierre = formatTime(timePickerStateCierre.hour, timePickerStateCierre.minute)
                    showCierrePicker = false
                }) { Text("Confirmar", color = PrimaryOrange) }
            },
            dismissButton = {
                TextButton(onClick = { showCierrePicker = false }) { Text("Cancelar", color = MediumGray) }
            }
        ) { TimePicker(state = timePickerStateCierre) }
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
                horario = mapOf("todos" to HorarioDia(apertura = apertura, cierre = cierre)),
                logoUrl = establecimientoToEdit?.logoUrl ?: "",
                createdAt = establecimientoToEdit?.createdAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            viewModel.guardarEstablecimiento(
                establecimiento = nuevo,
                imageBytes = selectedImage?.bytes,
                imageMimeType = selectedImage?.mimeType ?: "image/jpeg"
            ) { showSuccessSheet = true }
        },
        saveButtonText = if (uiState is FormState.Loading) "Guardando..." else if (establecimientoToEdit == null) "Registrar Establecimiento" else "Guardar Cambios",
        saveIcon = Icons.Default.Store
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
            label = "Nombre del Establecimiento",
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
        Text(
            "Categorías",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkGray,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoriasDisponibles.forEach { cat ->
                val isSelected = cat in categoriasSeleccionadas
                Box(
                    modifier = Modifier
                        .background(if (isSelected) PrimaryOrange else Color.White, RoundedCornerShape(20.dp))
                        .border(1.dp, if (isSelected) Color.Transparent else BorderGray, RoundedCornerShape(20.dp))
                        .clickable {
                            categoriasSeleccionadas = if (isSelected) {
                                categoriasSeleccionadas - cat
                            } else {
                                categoriasSeleccionadas + cat
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else MediumGray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Horarios
        Text(
            text = "Horario de Atención",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkGray,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FormTextField(
                    label = "Apertura",
                    value = apertura,
                    onValueChange = {},
                    readOnly = true,
                    onClick = { showAperturaPicker = true },
                    trailingIcon = { Icon(Icons.Default.Schedule, null, tint = MediumGray, modifier = Modifier.size(20.dp)) }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FormTextField(
                    label = "Cierre",
                    value = cierre,
                    onValueChange = {},
                    readOnly = true,
                    onClick = { showCierrePicker = true },
                    trailingIcon = { Icon(Icons.Default.Schedule, null, tint = MediumGray, modifier = Modifier.size(20.dp)) }
                )
            }
        }

        if (uiState is FormState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (uiState as FormState.Error).message,
                color = TrafficRed,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
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

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour < 12) "AM" else "PM"
    val h = if (hour % 12 == 0) 12 else hour % 12
    return "${h.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String = "Seleccionar hora",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        title = { Text(text = title, style = MaterialTheme.typography.titleMedium, color = DarkGray) },
        text = { content() },
        containerColor = Color.White,
    )
}
