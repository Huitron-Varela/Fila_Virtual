package com.example.fila_virtual.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.core.BackHandler
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import com.example.fila_virtual.core.ImageBytesPreview

@Composable
fun FormHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = LightBackground // Fondo gris uniforme
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = "Atrás",
                    tint = DarkGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun BaseFormScreen(
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveButtonText: String = "Guardar",
    saveIcon: ImageVector? = null,
    isSaveEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        containerColor = LightBackground,
        topBar = {
            FormHeader(title = title, onBack = onBack)
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = onSave,
                        enabled = isSaveEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryOrange,
                            disabledContainerColor = BorderGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (saveIcon != null) {
                            Icon(
                                imageVector = saveIcon,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            text = saveButtonText,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            content()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    minHeight: Int = 0,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkGray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MediumGray) },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (minHeight > 0) Modifier.height(minHeight.dp) else Modifier)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            enabled = enabled && onClick == null,
            readOnly = readOnly || onClick != null,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                errorContainerColor = Color.White, // <--- Fijo blanco en error
                unfocusedBorderColor = BorderGray,
                focusedBorderColor = PrimaryOrange,
                errorBorderColor = TrafficRed,
                focusedTextColor = DarkGray,
                unfocusedTextColor = DarkGray,
                disabledBorderColor = BorderGray,
                disabledTextColor = DarkGray,
                disabledContainerColor = Color.White
            )
        )
    }
}

@Composable
fun FormImagePicker(
    label: String,
    onClick: () -> Unit,
    hasImage: Boolean = false,
    imageUrl: String = "",
    previewBytes: ByteArray? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = DarkGray,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, BorderGray, RoundedCornerShape(12.dp))
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (previewBytes != null) {
                ImageBytesPreview(
                    bytes = previewBytes,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (imageUrl.isNotBlank()) {
                KamelImage(
                    resource = asyncPainterResource(imageUrl),
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    onFailure = {
                        ImagePickerPlaceholder(hasImage)
                    }
                )
            } else {
                ImagePickerPlaceholder(hasImage)
            }
        }
    }
}

@Composable
private fun ImagePickerPlaceholder(hasImage: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            tint = PrimaryOrange,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasImage) "Imagen seleccionada" else "Toca para subir imagen",
            style = MaterialTheme.typography.bodySmall,
            color = MediumGray
        )
    }
}

@Composable
fun RemoteImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable () -> Unit
) {
    if (url.isBlank()) {
        fallback()
    } else {
        KamelImage(
            resource = asyncPainterResource(url),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            onFailure = { fallback() }
        )
    }
}
