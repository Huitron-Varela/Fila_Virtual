package com.example.fila_virtual.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.BaseFormScreen
import com.example.fila_virtual.components.FormTextField
import com.example.fila_virtual.core.ImageBytesPreview
import com.example.fila_virtual.core.PhoneVisualTransformation
import com.example.fila_virtual.core.isValidName
import com.example.fila_virtual.core.isValidPhone
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.data.Usuario
import com.example.fila_virtual.core.SelectedImage
import com.example.fila_virtual.core.rememberImagePicker
import com.example.fila_virtual.features.user.UserViewModel
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    usuario: Usuario?,
    viewModel: UserViewModel,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val windowSize = LocalWindowSize.current
    val haptic = LocalHapticFeedback.current

    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var telefono by remember { mutableStateOf(if (usuario?.telefono == "Sin registrar") "" else (usuario?.telefono ?: "")) }
    var selectedImage by remember { mutableStateOf<SelectedImage?>(null) }
    val imagePicker = rememberImagePicker { selectedImage = it }
    val email = usuario?.email ?: ""

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isSaving by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar cambios") },
            text = { Text("¿Estás seguro de que deseas guardar los cambios realizados?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isSaving = true
                        viewModel.updateProfile(
                            nombre = nombre,
                            telefono = telefono,
                            fotoUrl = usuario?.fotoUrl,
                            fotoBytes = selectedImage?.bytes,
                            fotoMimeType = selectedImage?.mimeType ?: "image/jpeg"
                        ) { success ->
                            isSaving = false
                            if (success) {
                                showSuccessDialog = true
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("No se pudieron guardar los cambios.") }
                            }
                        }
                    },
                    enabled = !isSaving
                ) {
                    Text("Confirmar y guardar", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar", color = MediumGray)
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBack()
            },
            title = { Text("Perfil actualizado") },
            text = { Text("Tus cambios se guardaron correctamente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onBack()
                    }
                ) {
                    Text("Aceptar", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    BaseFormScreen(
        title = "Editar Perfil",
        onBack = onBack,
        onSave = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            showConfirmDialog = true
        },
        saveButtonText = if (isSaving) "Guardando..." else "Guardar Cambios",
        isSaveEnabled = !isSaving,
        snackbarHostState = snackbarHostState
    ) {
        Spacer(modifier = Modifier.height(windowSize.adaptiveDp(8)))

        // FOTO CON BOTÓN EDITAR
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                if (selectedImage != null) {
                    ImageBytesPreview(
                        bytes = selectedImage!!.bytes,
                        contentDescription = "Vista previa de foto de perfil",
                        modifier = Modifier
                            .size(windowSize.adaptiveDp(100))
                            .clip(CircleShape)
                    )
                } else {
                    ProfileHeader(usuario)
                }
                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        imagePicker()
                    },
                    modifier = Modifier.size(windowSize.adaptiveDp(36)).offset(x = windowSize.adaptiveDp(4), y = windowSize.adaptiveDp(4)),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(windowSize.adaptiveDp(2), MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Cambiar foto de perfil",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(windowSize.adaptiveDp(8))
                    )
                }
            }
        }

        Text(
            text = if (selectedImage == null) "Toca el lápiz para cambiar tu foto" else "Nueva foto lista para guardar",
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(windowSize.adaptiveDp(32)))

        // INPUTS
        FormTextField(
            label = "Nombre completo",
            value = nombre,
            onValueChange = { if (isValidName(it) || it.isEmpty()) nombre = it },
            placeholder = stringResource(Res.string.placeholder_name),
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            isError = nombre.isNotEmpty() && nombre.length < 3
        )

        Spacer(modifier = Modifier.height(windowSize.adaptiveDp(16)))

        FormTextField(
            label = stringResource(Res.string.label_phone),
            value = telefono,
            onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) telefono = it },
            placeholder = stringResource(Res.string.placeholder_phone),
            leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            visualTransformation = PhoneVisualTransformation(),
            isError = telefono.isNotEmpty() && !isValidPhone(telefono)
        )
        Spacer(modifier = Modifier.height(windowSize.adaptiveDp(16)))

        // EMAIL BLOQUEADO (Solo ver)
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Correo electrónico", style = MaterialTheme.typography.labelMedium, color = MediumGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(windowSize.adaptiveDp(8)))
            OutlinedTextField(
                value = email, onValueChange = {}, readOnly = true, enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = BorderGray,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledTextColor = MediumGray
                ),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MediumGray) }
            )
        }
    }
}
