package com.example.fila_virtual.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VisorCodigoQr(
    modifier: Modifier = Modifier,
    onCodigo: (String) -> Unit,
    onError: (String) -> Unit = {}
)
