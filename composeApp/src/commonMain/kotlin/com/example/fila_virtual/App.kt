package com.example.fila_virtual

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

// Importaciones de tu estructura
import com.example.fila_virtual.core.theme.FilaVirtualTheme
import com.example.fila_virtual.navigation.Screens
import com.example.fila_virtual.auth.animacion.AuthContainer
import com.example.fila_virtual.features.MainScreen
import com.example.fila_virtual.features.empleados.AceptarInvitacionScreen
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.rememberResponsiveSize

// Firebase Auth
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun App(
    onGoogleSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    invitationToken: String? = null
) {
    BoxWithConstraints {
        // OPTIMIZACIÓN: Utilizamos el 'remember' que definiste en tu ResponsiveUtils
        // Esto evita instancias innecesarias en cada recomposición.
        val windowSize = rememberResponsiveSize(maxWidth, maxHeight)

        CompositionLocalProvider(LocalWindowSize provides windowSize) {
            FilaVirtualTheme {
                // Mantenemos tu lógica de inicio persistente
                val startScreen = remember { if (Firebase.auth.currentUser != null) Screens.Home else Screens.Login }
                var currentScreen by remember { mutableStateOf(startScreen) }

                val scope = rememberCoroutineScope()
                val mainUserViewModel = remember { UserViewModel() }
                var pendingInvitationToken by remember { mutableStateOf(invitationToken) }

                // FIX PARA GOOGLE: Escuchamos el cambio de sesión
                LaunchedEffect(Unit) {
                    Firebase.auth.authStateChanged.collectLatest { user ->
                        if (user != null && currentScreen == Screens.Login) {
                            currentScreen = Screens.Home
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    // El fondo principal de toda tu app, reaccionando a tu Theme estandarizado
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen) {
                        Screens.Login, Screens.Register -> {
                            AuthContainer(
                                currentScreen = currentScreen,
                                onNavigate = { newScreen -> currentScreen = newScreen },
                                onGoogleSignIn = onGoogleSignIn
                            )
                        }
                        Screens.Home -> {
                            if (pendingInvitationToken != null) {
                                AceptarInvitacionScreen(
                                    token = pendingInvitationToken!!,
                                    onAccepted = {
                                        mainUserViewModel.loadUserData()
                                        pendingInvitationToken = null
                                        currentScreen = Screens.Home
                                    },
                                    onCancel = {
                                        pendingInvitationToken = null
                                        currentScreen = Screens.Home
                                    }
                                )
                            } else {
                                MainScreen(
                                    viewModel = mainUserViewModel,
                                    onLogout = {
                                        scope.launch {
                                            onSignOut()
                                            currentScreen = Screens.Login
                                        }
                                    }
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}