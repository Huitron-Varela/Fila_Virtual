package com.example.fila_virtual.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
actual fun ImageBytesPreview(
    bytes: ByteArray,
    contentDescription: String?,
    modifier: Modifier
) {
    KamelImage(
        resource = asyncPainterResource(bytes),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}