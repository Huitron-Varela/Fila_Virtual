package com.example.fila_virtual.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.example.fila_virtual.core.checkPasswordRequirements
// Importaciones de recursos
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.*

// Importaciones de tu arquitectura y tema
import com.example.fila_virtual.core.theme.*

@Composable
fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MediumGray, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = isError,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            textStyle = MaterialTheme.typography.bodyMedium,
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = BorderGray,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                errorContainerColor = Color.White, // <--- Esto evita la transición al gris en error
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MediumGray,
                errorLeadingIconColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MediumGray,
                disabledBorderColor = BorderGray
            )
        )
        AnimatedVisibility(visible = isError && errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onVisibilityChange: (Boolean) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MediumGray, style = MaterialTheme.typography.bodyMedium) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            isError = isError,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            textStyle = MaterialTheme.typography.bodyMedium,
            leadingIcon = leadingIcon?.let {
                { Icon(imageVector = it, contentDescription = null) }
            },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                IconButton(onClick = { onVisibilityChange(!passwordVisible) }, enabled = enabled) {
                    Icon(
                        imageVector = image,
                        contentDescription = stringResource(
                            if (passwordVisible) Res.string.password_hide else Res.string.password_show
                        )
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = BorderGray,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                errorContainerColor = Color.White, // <--- Esto evita la transición al gris en error
                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedLeadingIconColor = MediumGray,
                errorLeadingIconColor = MaterialTheme.colorScheme.error,
                focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
                unfocusedTrailingIconColor = MediumGray,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MediumGray,
                disabledBorderColor = BorderGray
            )
        )
        AnimatedVisibility(visible = isError && errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun TermsCheckbox(
    termsAccepted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = termsAccepted,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = BorderGray
            )
        )

        val primaryColor = MaterialTheme.colorScheme.primary
        val textColor = MaterialTheme.colorScheme.onBackground

        val annotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(color = textColor)) {
                append(stringResource(Res.string.terms_accept))
                append(" ")
            }

            pushStringAnnotation(tag = "TERMS", annotation = "terms")
            withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) {
                append(stringResource(Res.string.terms_user_agreement))
            }
            pop()

            withStyle(style = SpanStyle(color = textColor)) {
                append(" ")
                append(stringResource(Res.string.terms_and))
                append(" ")
            }

            pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
            withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.SemiBold)) {
                append(stringResource(Res.string.terms_privacy_policy))
            }
            pop()
        }

        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 4.dp),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                    .firstOrNull()?.let { onTermsClick() }

                annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                    .firstOrNull()?.let { onPrivacyClick() }
            }
        )
    }
}

@Composable
fun ActionButton(
    text: String,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = BorderGray
        ),
        shape = RoundedCornerShape(16.dp),
        enabled = enabled && !isLoading,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            if (icon != null) {
                Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { snackbarData ->
        AppSnackbar(
            message = snackbarData.visuals.message,
            modifier = Modifier.padding(horizontal = 16.dp),
            action = snackbarData.visuals.actionLabel?.let { label ->
                {
                    TextButton(onClick = snackbarData::performAction) {
                        Text(label)
                    }
                }
            },
            dismissAction = if (snackbarData.visuals.withDismissAction) {
                {
                    IconButton(onClick = snackbarData::dismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.snackbar_dismiss)
                        )
                    }
                }
            } else {
                null
            }
        )
    }
}

@Composable
fun AppSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    containerColor: Color = DarkGray,
    contentColor: Color = Color.White,
    action: (@Composable () -> Unit)? = null,
    dismissAction: (@Composable () -> Unit)? = null
) {
    Snackbar(
        modifier = modifier,
        action = action,
        dismissAction = dismissAction,
        shape = RoundedCornerShape(16.dp),
        containerColor = containerColor,
        contentColor = contentColor,
        actionContentColor = SoftOrangeBg
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.logot),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = contentColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun SocialLoginBlock(onGoogleClick: () -> Unit = {}, onAppleClick: () -> Unit = {}) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGray)
            Text(
                text = stringResource(Res.string.social_login_label),
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MediumGray,
                style = MaterialTheme.typography.labelSmall
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = BorderGray)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, BorderGray, CircleShape)
                    .clickable { onGoogleClick() }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_google_logo),
                    contentDescription = "Google"
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, BorderGray, CircleShape)
                    .clickable { onAppleClick() }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_appel_logo),
                    contentDescription = "Apple",
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onBackground)
                )
            }
        }
    }
}

@Composable
fun NavigationLink(textMain: String, textLink: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = textMain,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = textLink,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable(onClick = onClick).padding(start = 4.dp)
        )
    }
}
@Composable
fun PasswordStrengthBar(password: String) {
    val requirements = checkPasswordRequirements(password)

    var score = 0
    if (requirements.hasMinLength) score++
    if (requirements.hasUpperCase) score++
    if (requirements.hasDigit) score++
    if (requirements.hasSpecialChar) score++

    val TrafficYellow = Color(0xFFFFC107)

    val barColor = when (score) {
        1 -> TrafficRed
        2 -> TrafficYellow
        3 -> TrafficYellow
        4 -> TrafficGreen
        else -> BorderGray
    }

    val statusText = when (score) {
        0 -> "Introduce una contraseña"
        1 -> "Débil"
        2 -> "Regular"
        3 -> "Buena"
        4 -> "Segura"
        else -> ""
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (i <= score) barColor else BorderGray,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = if (score == 0) MediumGray else barColor,
            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
        )

        Text(
            text = "Tu contraseña debe contener:\n• Mínimo 9 caracteres\n• Al menos una letra mayúscula\n• Al menos un número\n• Un carácter especial (ej. @, #, $, !)",
            style = MaterialTheme.typography.labelSmall,
            color = MediumGray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "¿Qué se te antoja hoy?"
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                color = MediumGray,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = BorderGray,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        )
    )
}