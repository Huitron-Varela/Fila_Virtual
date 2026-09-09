package com.example.fila_virtual.features.admin.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.BaseFormScreen
import com.example.fila_virtual.components.FormImagePicker
import com.example.fila_virtual.components.FormTextField
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.features.admin.FormState
import com.example.fila_virtual.features.admin.ProductoViewModel
import com.example.fila_virtual.features.admin.EstablecimientoViewModel
import com.example.fila_virtual.core.BackHandler
import com.example.fila_virtual.core.SelectedImage
import com.example.fila_virtual.core.PermissionType
import com.example.fila_virtual.core.rememberPermissionsManager
import com.example.fila_virtual.core.rememberImagePicker
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddDishScreen(
    establecimientoId: String,
    ownerUid: String,
    productoToEdit: Producto? = null,
    onBack: () -> Unit,
    viewModel: ProductoViewModel = viewModel(),
    establecimientoViewModel: EstablecimientoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedEstablecimientoId by remember { mutableStateOf(productoToEdit?.establecimientoId?.takeIf { it.isNotBlank() } ?: establecimientoId) }
    var showSucursalSelector by remember { mutableStateOf(false) }

    LaunchedEffect(ownerUid) {
        establecimientoViewModel.setOwnerUid(ownerUid)
    }

    val establecimientos by establecimientoViewModel.establecimientos.collectAsState()
    val sucursalActual = if (selectedEstablecimientoId.isEmpty()) {
        "Seleccionar Sucursal"
    } else {
        establecimientos.find { it.id == selectedEstablecimientoId }?.nombre ?: "Seleccionar Sucursal"
    }

    var nombre by remember { mutableStateOf(productoToEdit?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(productoToEdit?.descripcion ?: "") }
    var precio by remember { mutableStateOf(productoToEdit?.precio?.toString() ?: "") }
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

    val categorias = ProductMenuCategories.options
    var categoriaSeleccionada by remember { mutableStateOf(productoToEdit?.categoria ?: "") }
    
    var showSuccessSheet by remember { mutableStateOf(false) }
    var showNoEstablecimientosAlert by remember { mutableStateOf(false) }

    val haptic = LocalHapticFeedback.current
    BackHandler(onBack = onBack)

    val isFormValid = nombre.trim().isNotBlank() && precio.trim().isNotBlank() && selectedEstablecimientoId.isNotBlank() && categoriaSeleccionada.isNotBlank()

    // Modal de éxito (Bottom Sheet)
    if (showSuccessSheet) {
        ModalBottomSheet(
            onDismissRequest = { 
                showSuccessSheet = false
                viewModel.resetState()
                onBack() 
            },
            containerColor = Color.White
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
                
                Text(
                    text = if (productoToEdit == null) "¡Platillo Guardado!" else "¡Platillo Actualizado!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = if (productoToEdit == null) "El platillo se ha agregado correctamente a tu menú y está disponible para tus clientes." else "Los datos del platillo se han actualizado correctamente.",
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
                    Text(
                        "Entendido",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showNoEstablecimientosAlert) {
        ModalBottomSheet(
            onDismissRequest = { showNoEstablecimientosAlert = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = PrimaryOrange,
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Atención",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "No tienes ningún establecimiento creado. Debes crear un establecimiento primero antes de poder agregar platillos a un menú.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { showNoEstablecimientosAlert = false },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Entendido",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    BaseFormScreen(
        title = if (productoToEdit == null) "Agregar Platillo" else "Editar Platillo",
        onBack = onBack,
        isSaveEnabled = isFormValid,
        onSave = {
            if (isFormValid) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val precioDouble = precio.trim().toDoubleOrNull() ?: 0.0
                viewModel.guardarProducto(
                    id = productoToEdit?.id ?: "",
                    establecimientoId = selectedEstablecimientoId,
                    ownerUid = ownerUid,
                    nombre = nombre.trim(),
                    descripcion = descripcion.trim(),
                    precio = precioDouble,
                    categoria = categoriaSeleccionada,
                    imagenUrl = productoToEdit?.imagenUrl ?: "",
                    imageBytes = selectedImage?.bytes,
                    imageMimeType = selectedImage?.mimeType ?: "image/jpeg",
                    onSuccess = {
                        showSuccessSheet = true
                    }
                )
            }
        },
        saveButtonText = if (uiState is FormState.Loading) "Guardando..." else if (productoToEdit == null) "Guardar Platillo" else "Guardar Cambios"
    ) {
        FormImagePicker(
            label = "IMAGEN DEL PLATILLO",
            onClick = requestImage,
            hasImage = selectedImage != null || !productoToEdit?.imagenUrl.isNullOrBlank(),
            imageUrl = productoToEdit?.imagenUrl ?: "",
            previewBytes = selectedImage?.bytes
        )

        Spacer(modifier = Modifier.height(24.dp))

        FormTextField(
            label = "Nombre del Platillo",
            value = nombre,
            onValueChange = { nombre = it },
            placeholder = "Ej. Hamburguesa Especial AlToque"
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormTextField(
            label = "Descripción",
            value = descripcion,
            onValueChange = { descripcion = it },
            placeholder = "Describe los ingredientes, alérgenos y detalles especiales...",
            singleLine = false,
            minHeight = 120
        )

        Spacer(modifier = Modifier.height(16.dp))

        FormTextField(
            label = "Precio",
            value = precio,
            onValueChange = { precio = it },
            placeholder = "0.00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            leadingIcon = {
                Text(
                    "$",
                    fontWeight = FontWeight.Bold,
                    color = DarkGray,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 5. SELECCIONAR SUCURSAL
        Text(
            text = "Seleccionar Sucursal",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MediumGray
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (establecimientos.isEmpty()) {
                            showNoEstablecimientosAlert = true
                        } else {
                            showSucursalSelector = true 
                        }
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
                        color = if (selectedEstablecimientoId.isEmpty()) MediumGray else DarkGray,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = DarkGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showSucursalSelector,
                onDismissRequest = { showSucursalSelector = false },
                modifier = Modifier.background(Color.White)
            ) {
                establecimientos.forEach { sucursal ->
                    DropdownMenuItem(
                        text = { Text(sucursal.nombre) },
                        onClick = {
                            selectedEstablecimientoId = sucursal.id
                            showSucursalSelector = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. CATEGORÍA
        Text(
            text = "Categoría",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MediumGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorias.forEach { categoria ->
                val isSelected = categoria == categoriaSeleccionada
                Surface(
                    color = if (isSelected) PrimaryOrange else Color.White,
                    shape = RoundedCornerShape(20.dp),
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

        if (uiState is FormState.Error) {
            Text(
                text = (uiState as FormState.Error).message,
                color = TrafficRed,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}