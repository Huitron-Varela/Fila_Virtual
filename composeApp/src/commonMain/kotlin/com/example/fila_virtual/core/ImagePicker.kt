package com.example.fila_virtual.core

import androidx.compose.runtime.Composable

data class SelectedImage(
    val bytes: ByteArray,
    val mimeType: String = "image/jpeg"
)

@Composable
expect fun rememberImagePicker(onImageSelected: (SelectedImage) -> Unit): () -> Unit