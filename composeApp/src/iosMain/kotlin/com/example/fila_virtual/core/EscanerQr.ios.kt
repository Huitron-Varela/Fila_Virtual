package com.example.fila_virtual.core

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun VisorCodigoQr(
    modifier: Modifier,
    onCodigo: (String) -> Unit,
    onError: (String) -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text("El escaneo con cámara está disponible en Android.")
    }
}
