package com.example.fila_virtual.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.fila_virtual.data.Roles

import com.example.fila_virtual.features.empleados.EmpleadoMainScreen
import com.example.fila_virtual.features.user.ClienteMainScreen
import com.example.fila_virtual.features.user.UserViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.fila_virtual.features.admin.AdminMainScreen

@Composable
fun MainScreen(
    viewModel: UserViewModel = remember { UserViewModel() },
    onLogout: () -> Unit
) {
    val usuario = viewModel.usuario

    LaunchedEffect(Unit) {
        if (viewModel.usuario == null) viewModel.loadUserData()
    }

    if (usuario == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.errorMessage.isNotBlank()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(viewModel.errorMessage, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = { viewModel.loadUserData() }) {
                        Text("Reintentar")
                    }
                }
            } else {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        // --- DEPURACIÓN ---
        val rolDetectado = Roles.normalizar(usuario.rolGlobal)
        println("DEBUG_ROLE: El usuario ${usuario.email} tiene el rol: '$rolDetectado'")

        // Enrutador basado en el rol del usuario
        when (rolDetectado) {
            Roles.ADMIN -> {
                AdminMainScreen(viewModel = viewModel, onLogout = onLogout)
            }
            Roles.EMPLEADO -> {
                EmpleadoMainScreen(viewModel = viewModel, onLogout = onLogout)
            }
            else -> {
                ClienteMainScreen(viewModel = viewModel, onLogout = onLogout)
            }
        }
    }
}
