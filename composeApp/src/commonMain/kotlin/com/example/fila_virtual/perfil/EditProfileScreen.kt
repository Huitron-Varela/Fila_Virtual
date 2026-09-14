package com.example.fila_virtual.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.example.fila_virtual.components.BaseFormScreen
import com.example.fila_virtual.components.InputField
import com.example.fila_virtual.core.PhoneVisualTransformation
import com.example.fila_virtual.core.isValidName
import com.example.fila_virtual.core.isValidPhone
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.data.Usuario
import com.example.fila_virtual.core.PermissionType
import com.example.fila_virtual.core.rememberPermissionsManager
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
    val permissions = rememberPermissionsManager()
    val windowSize = LocalWindowSize.current

    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var telefono by remember { mutableStateOf(if (usuario?.telefono == "Sin registrar") "" else (usuario?.telefono ?: "")) }
    val email = usuario?.email ?: ""

    val scope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }
    var showConfirmSheet by remember { mutableStateOf(false) }
    var showPhotoSheet by remember { mutableStateOf(false) }

    // MODAL DE CONFIRMACIÓN
    if (showConfirmSheet) {
        ModalBottomSheet(
            onDismissRequest = { showConfirmSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = windowSize.adaptiveDp(24), topEnd = windowSize.adaptiveDp(24))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(windowSize.adaptiveDp(24)).padding(bottom = windowSize.adaptiveDp(32)), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Confirmar cambios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(windowSize.adaptiveDp(16)))
                Text("¿Estás seguro de que deseas guardar los cambios realizados?", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(windowSize.adaptiveDp(32)))
                Button(
                    onClick = {
                        showConfirmSheet = false
                        scope.launch {
                            isSaving = true
                            viewModel.updateProfile(nombre, telefono, usuario?.fotoUrl) { success ->
                                if (success) onBack()
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(windowSize.adaptiveDp(56)),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(windowSize.adaptiveDp(16))
                ) { Text("Confirmar y Guardar", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
            }
        }
    }

    // MODAL DE FOTO
    if (showPhotoSheet) {
        ModalBottomSheet(onDismissRequest = { showPhotoSheet = false }, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = windowSize.adaptiveDp(32))) {
                ListItem(
                    headlineContent = { Text("Tomar foto", color = MaterialTheme.colorScheme.onSurface) },
                    leadingContent = { Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        permissions.askPermission(PermissionType.CAMERA) { granted ->
                            if (granted) { /* Lógica Cámara */ }
                            showPhotoSheet = false
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
                ListItem(
                    headlineContent = { Text("Elegir de la galería", color = MaterialTheme.colorScheme.onSurface) },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable {
                        permissions.askPermission(PermissionType.GALLERY) { granted ->
                            if (granted) { /* Lógica Galería */ }
                            showPhotoSheet = false
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
    }

    BaseFormScreen(
        title = "Editar Perfil",
        onBack = onBack,
        onSave = { showConfirmSheet = true },
        saveButtonText = if (isSaving) "Guardando..." else "Guardar Cambios"
    ) {
        Spacer(modifier = Modifier.height(windowSize.adaptiveDp(8)))

        // FOTO CON BOTÓN EDITAR
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                ProfileHeader(usuario)
                Surface(
                    onClick = { showPhotoSheet = true },
                    modifier = Modifier.size(windowSize.adaptiveDp(36)).offset(x = windowSize.adaptiveDp(4), y = windowSize.adaptiveDp(4)),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    border = BorderStroke(windowSize.adaptiveDp(2), MaterialTheme.colorScheme.surface)
                ) { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(windowSize.adaptiveDp(8))) }
            }
        }

        Spacer(modifier = Modifier.height(windowSize.adaptiveDp(32)))

        // INPUTS
        InputField(
            label = "Nombre completo",
            value = nombre,
            onValueChange = { if (isValidName(it) || it.isEmpty()) nombre = it },
            placeholder = stringResource(Res.string.placeholder_name),
            leadingIcon = Icons.Filled.Person,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            isError = nombre.isNotEmpty() && nombre.length < 3
        )

        Spacer(modifier = Modifier.height(windowSize.adaptiveDp(16)))

        InputField(
            label = stringResource(Res.string.label_phone),
            value = telefono,
            onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) telefono = it },
            placeholder = stringResource(Res.string.placeholder_phone),
            leadingIcon = Icons.Filled.Phone,
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
                shape = RoundedCornerShape(windowSize.adaptiveDp(12)),
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
