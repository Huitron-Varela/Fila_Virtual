package com.example.fila_virtual.auth.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fila_virtual.components.ActionButton
import com.example.fila_virtual.components.InputField
import com.example.fila_virtual.components.NavigationLink
import com.example.fila_virtual.components.PasswordInputField
import com.example.fila_virtual.components.SocialLoginBlock
import com.example.fila_virtual.core.ErrorMessages
import com.example.fila_virtual.core.LocalWindowSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

// Importaciones de Firebase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

// Importaciones de tu Logo y recursos
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.*

// Importaciones de tu arquitectura y tema
import com.example.fila_virtual.core.mapFirebaseError
import com.example.fila_virtual.core.isValidEmail
import com.example.fila_virtual.navigation.Screens
import com.example.fila_virtual.core.theme.* 

// Importación de tu Repositorio
import com.example.fila_virtual.repository.AuthRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigate: (Screens) -> Unit,
    onGoogleSignIn: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val windowSize = LocalWindowSize.current
    val authRepository = remember { AuthRepository() }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var recoveryEmail by remember { mutableStateOf("") }
    var recoveryMessage by remember { mutableStateOf("") }
    var isRecovering by remember { mutableStateOf(false) }

    var recoveryStep by remember { mutableStateOf(1) }
    var inputOtp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var newPasswordVisible by remember { mutableStateOf(false) }
    val isLoginEnabled = email.isNotBlank() && isValidEmail(email) && password.isNotBlank()
    val isRecoveryEnabled = recoveryEmail.isNotBlank() && isValidEmail(recoveryEmail)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding() 
            .verticalScroll(rememberScrollState()) 
            .padding(horizontal = 24.dp, vertical = windowSize.compactDp(8).value.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(windowSize.compactDp(16).value.dp))

        Text(
            text = stringResource(Res.string.btn_login),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(Res.string.login_welcome),
            style = MaterialTheme.typography.bodyLarge,
            color = MediumGray,
            modifier = Modifier.padding(top = 4.dp, bottom = windowSize.compactDp(24).value.dp)
        )

        InputField(
            label = stringResource(Res.string.label_email),
            value = email,
            onValueChange = { email = it; errorMessage = "" },
            placeholder = stringResource(Res.string.placeholder_email),
            leadingIcon = Icons.Filled.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            isError = email.isNotEmpty() && !isValidEmail(email)
        )

        Spacer(modifier = Modifier.height(windowSize.compactDp(16).value.dp))

        PasswordInputField(
            label = stringResource(Res.string.label_password),
            value = password,
            onValueChange = { password = it; errorMessage = "" },
            passwordVisible = passwordVisible,
            onVisibilityChange = { passwordVisible = it },
            placeholder = stringResource(Res.string.placeholder_password),
            leadingIcon = Icons.Filled.Lock,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        Text(
            text = stringResource(Res.string.forgot_password),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = 8.dp)
                .clickable { showBottomSheet = true }
        )

        Spacer(modifier = Modifier.height(windowSize.compactDp(24).value.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        ActionButton(
            text = stringResource(Res.string.btn_login),
            isLoading = isLoading,
            enabled = isLoginEnabled,
            icon = Icons.Default.Login
        ) {
            scope.launch {
                isLoading = true
                try {
                    Firebase.auth.signInWithEmailAndPassword(email.trim(), password.trim())
                    onNavigate(Screens.Home)
                } catch (e: Exception) {
                    errorMessage = mapFirebaseError(e.message)
                } finally {
                    isLoading = false
                }
            }
        }

        Spacer(modifier = Modifier.height(windowSize.compactDp(32).value.dp))
        SocialLoginBlock(onGoogleClick = onGoogleSignIn)

        Spacer(modifier = Modifier.height(32.dp))

        NavigationLink(
            textMain = stringResource(Res.string.no_account),
            textLink = stringResource(Res.string.btn_register)
        ) { onNavigate(Screens.Register) }

        Spacer(modifier = Modifier.height(windowSize.compactDp(32).value.dp))
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
                recoveryMessage = ""
                recoveryStep = 1
                inputOtp = ""
                newPassword = ""
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background // Fondo Gris
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // PASO 1: PEDIR CORREO
                AnimatedVisibility(visible = recoveryStep == 1) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Recuperar contraseña", style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = "Te enviaremos un código de 6 dígitos.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MediumGray,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        InputField(
                            label = "Correo electrónico",
                            value = recoveryEmail,
                            onValueChange = { recoveryEmail = it; recoveryMessage = "" },
                            placeholder = "ejemplo@correo.com",
                            leadingIcon = Icons.Filled.Email
                        )

                        if (recoveryMessage.isNotEmpty()) {
                            Text(recoveryMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        ActionButton(
                            text = "Enviar Código",
                            isLoading = isRecovering,
                            enabled = isRecoveryEnabled
                        ) {
                            scope.launch {
                                isRecovering = true
                                if (authRepository.sendPasswordResetOtp(recoveryEmail.trim())) {
                                    recoveryStep = 2
                                } else {
                                    recoveryMessage = ErrorMessages.OTP_SEND_ERROR
                                }
                                isRecovering = false
                            }
                        }
                    }
                }

                // PASO 2: OTP
                AnimatedVisibility(visible = recoveryStep == 2) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Verifica tu identidad", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = inputOtp,
                            onValueChange = { if (it.length <= 6) inputOtp = it },
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 24.sp, textAlign = TextAlign.Center, letterSpacing = 8.sp),
                            modifier = Modifier.fillMaxWidth(0.8f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White, // Input Blanco
                                focusedContainerColor = Color.White,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = BorderGray
                            )
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        ActionButton(text = "Verificar", isLoading = isRecovering) {
                            scope.launch {
                                isRecovering = true
                                if (authRepository.verifyOtpCode(recoveryEmail.trim(), inputOtp)) {
                                    recoveryStep = 3
                                } else {
                                    recoveryMessage = "Código incorrecto"
                                }
                                isRecovering = false
                            }
                        }
                    }
                }

                // PASO 3: NUEVA CLAVE
                AnimatedVisibility(visible = recoveryStep == 3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nueva contraseña", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        PasswordInputField(
                            label = "Contraseña",
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            passwordVisible = newPasswordVisible,
                            onVisibilityChange = { newPasswordVisible = it },
                            placeholder = "Mínimo 8 caracteres"
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        ActionButton(text = "Actualizar", isLoading = isRecovering) {
                            if (newPassword.length < 8) {
                                recoveryMessage = ErrorMessages.PASSWORD_TOO_SHORT
                                return@ActionButton
                            }
                            scope.launch {
                                isRecovering = true
                                delay(1000)
                                showBottomSheet = false
                                isRecovering = false
                            }
                        }
                    }
                }
            }
        }
    }
}
