package com.example.fila_virtual.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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

    // CAMBIO AQUÍ: Solo mostramos la carga inicial si no tenemos los datos del usuario aún.
    // Esto evita que la app se destruya cuando un botón use isLoading = true.
    if (usuario == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        // --- DEPURACIÓN ---
        val rolDetectado = usuario.rolGlobal.lowercase().trim()
        println("DEBUG_ROLE: El usuario ${usuario.email} tiene el rol: '$rolDetectado'")

        // Enrutador basado en el rol del usuario
        when (rolDetectado) {
            "admin" -> {
                AdminMainScreen(viewModel = viewModel, onLogout = onLogout)
            }
            "empleado" -> {
                EmpleadoMainScreen(viewModel = viewModel, onLogout = onLogout)
            }
            else -> {
                ClienteMainScreen(viewModel = viewModel, onLogout = onLogout)
            }
        }
    }
}
