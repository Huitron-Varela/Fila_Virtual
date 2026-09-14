package com.example.fila_virtual.features.admin.empleados

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*
import com.example.fila_virtual.components.BaseFormScreen
import com.example.fila_virtual.components.InputField
import com.example.fila_virtual.data.EmpleadoDetalle
import com.example.fila_virtual.data.RolesEmpleado
import com.example.fila_virtual.features.admin.EstablecimientoViewModel
import com.example.fila_virtual.core.*
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.features.admin.FormState
import com.example.fila_virtual.core.BackHandler
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEmployeeScreen(
    empleado: EmpleadoDetalle? = null,
    establecimientoId: String,
    ownerUid: String,
    viewModel: EmpleadoViewModel,
    onNavigateBack: () -> Unit
) {
    val establecimientoViewModel: EstablecimientoViewModel = viewModel()
    val windowSize = LocalWindowSize.current
    val formPadding = windowSize.compactDp(16)
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = empleado != null
    val focusManager = LocalFocusManager.current

    var correo by remember { mutableStateOf(empleado?.correo ?: "") }
    val rolesDisponibles = RolesEmpleado.disponibles
    var rolesSeleccionados by remember {
        mutableStateOf(
            empleado?.roles?.ifEmpty {
                empleado.rol.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }?.ifEmpty { listOf("cajero") } ?: listOf("cajero")
        )
    }
    var localError by remember { mutableStateOf("") }
    var invitationToken by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    
    val establecimientos by establecimientoViewModel.establecimientos.collectAsState()
    
    LaunchedEffect(ownerUid) {
        establecimientoViewModel.setOwnerUid(ownerUid)
    }

    var selectedEstablecimientoId by remember { mutableStateOf(establecimientoId) }
    var expandedEstablecimiento by remember { mutableStateOf(false) }

    val sucursalActual = if (selectedEstablecimientoId.isEmpty()) {
        stringResource(Res.string.emp_select_branch)
    } else {
        establecimientos.find { it.id == selectedEstablecimientoId }?.nombre ?: stringResource(Res.string.emp_unknown_branch)
    }

    LaunchedEffect(uiState) {
        if (uiState is FormState.Success && isEditing) {
            viewModel.resetState()
            onNavigateBack()
        }
    }

    BaseFormScreen(
        title = if (isEditing) stringResource(Res.string.emp_title_edit) else stringResource(Res.string.emp_add_employee),
        onBack = onNavigateBack,
        //isLoading = uiState is FormState.Loading,
        saveButtonText = if (isEditing) stringResource(Res.string.est_save_changes) else stringResource(Res.string.emp_bind_employee),
        onSave = {
            if (selectedEstablecimientoId.isEmpty()) {
                localError = "emp_error_no_branch" // we resolve via stringResource or keep key logic
            } else {
                localError = ""
                focusManager.clearFocus()
                if (isEditing) {
                    viewModel.guardarEmpleadoPorCorreo(
                        correoBusqueda = correo,
                        roles = rolesSeleccionados,
                        establecimientoId = selectedEstablecimientoId,
                        onSuccess = onNavigateBack
                    )
                } else {
                    viewModel.enviarInvitacionPorCorreo(
                        correo = correo,
                        roles = rolesSeleccionados,
                        establecimientoId = selectedEstablecimientoId,
                        onSent = { invitationToken = it }
                    )
                }
            }
        }
    ) {
        // Resolve error message properly
        val errorTextToShow = if (localError == "emp_error_no_branch") stringResource(Res.string.emp_error_no_branch) else localError

        if (!isEditing) {
            Text(
                text = stringResource(Res.string.emp_warning_registered_account),
                color = PrimaryOrange,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .background(SoftOrangeBg, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            )
            if (invitationToken.isNotEmpty()) {
                Surface(
                    color = SoftOrangeBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(formPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(Res.string.emp_invitation_sent), fontWeight = FontWeight.Bold, color = DarkGray)
                        Text(
                            text = invitationToken,
                            color = PrimaryOrange,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        OutlinedButton(
                            onClick = { clipboardManager.setText(AnnotatedString(invitationToken)) }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(Res.string.emp_copy_code))
                        }
                        Text(
                            stringResource(Res.string.emp_invitation_expiry),
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        } else {
            Text(
                text = stringResource(Res.string.emp_editing_role, empleado?.nombre ?: ""),
                color = DarkGray,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        InputField(
            label = stringResource(Res.string.emp_user_email_label),
            value = correo,
            onValueChange = { correo = it },
            placeholder = "ejemplo@correo.com",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            //enabled = !isEditing
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(Res.string.emp_branch),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkGray
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Box {
            Surface(
                color = LightSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (!isEditing) expandedEstablecimiento = true },
                border = BorderStroke(1.dp, ExtraLightGray)
            ) {
                Row(
                    modifier = Modifier.padding(formPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = PrimaryOrange,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sucursalActual,
                        color = if (selectedEstablecimientoId.isEmpty()) MediumGray else DarkGray,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!isEditing) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MediumGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = expandedEstablecimiento,
                onDismissRequest = { expandedEstablecimiento = false },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .background(LightSurface)
            ) {
                establecimientos.forEach { sucursal ->
                    DropdownMenuItem(
                        text = { Text(sucursal.nombre) },
                        onClick = {
                            selectedEstablecimientoId = sucursal.id
                            expandedEstablecimiento = false
                            if (localError == "emp_error_no_branch") localError = ""
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(Res.string.emp_roles_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkGray
        )
        Spacer(modifier = Modifier.height(12.dp))

        RoleRow(
            roles = listOf("cajero", "cocina"),
            selectedRoles = rolesSeleccionados,
            onRoleClick = { role ->
                rolesSeleccionados = toggleRole(rolesSeleccionados, role)
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        RoleRow(
            roles = listOf("supervisor", "admin"),
            selectedRoles = rolesSeleccionados,
            onRoleClick = { role ->
                rolesSeleccionados = toggleRole(rolesSeleccionados, role)
            }
        )
        Spacer(modifier = Modifier.height(12.dp))
        val allRolesText = stringResource(Res.string.est_all)
        RoleRow(
            roles = listOf("entrega", allRolesText),
            selectedRoles = rolesSeleccionados,
            onRoleClick = { role ->
                rolesSeleccionados = if (role == allRolesText) {
                    if (rolesSeleccionados.containsAll(rolesDisponibles)) emptyList() else rolesDisponibles
                } else {
                    toggleRole(rolesSeleccionados, role)
                }
            }
        )
        Text(
            text = if (rolesSeleccionados.isEmpty()) stringResource(Res.string.emp_error_no_role) else stringResource(Res.string.emp_info_multiple_roles),
            color = if (rolesSeleccionados.isEmpty()) TrafficRed else MediumGray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (errorTextToShow.isNotEmpty() || uiState is FormState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            val errorMessage = if (errorTextToShow.isNotEmpty()) errorTextToShow else (uiState as FormState.Error).message
            Text(
                text = errorMessage,
                color = TrafficRed,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RoleRow(
    roles: List<String>,
    selectedRoles: List<String>,
    onRoleClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        roles.forEach { role ->
            RoleChip(
                label = when (role) {
                    "cajero" -> stringResource(Res.string.emp_role_cashier)
                    "cocina" -> stringResource(Res.string.emp_role_kitchen)
                    "entrega" -> stringResource(Res.string.emp_role_delivery)
                    "supervisor" -> stringResource(Res.string.emp_role_supervisor)
                    stringResource(Res.string.est_all) -> stringResource(Res.string.emp_role_all)
                    else -> stringResource(Res.string.emp_role_admin_local)
                },
                isSelected = if (role == stringResource(Res.string.est_all)) selectedRoles.containsAll(listOf("cajero", "cocina", "entrega", "supervisor", "admin")) else role in selectedRoles,
                onClick = { onRoleClick(role) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun toggleRole(selectedRoles: List<String>, role: String): List<String> {
    return if (role in selectedRoles) selectedRoles - role else selectedRoles + role
}

@Composable
private fun RoleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimaryOrange else LightSurface)
            .border(1.dp, if (isSelected) Color.Transparent else BorderGray, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) LightSurface else MediumGray,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}