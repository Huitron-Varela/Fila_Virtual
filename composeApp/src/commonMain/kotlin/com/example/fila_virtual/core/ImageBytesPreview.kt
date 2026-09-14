package com.example.fila_virtual.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun ImageBytesPreview(
    bytes: ByteArray,
    contentDescription: String?,
    modifier: Modifier
)