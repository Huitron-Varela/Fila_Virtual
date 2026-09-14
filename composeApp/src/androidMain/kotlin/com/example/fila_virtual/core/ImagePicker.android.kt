package com.example.fila_virtual.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberImagePicker(onImageSelected: (SelectedImage) -> Unit): () -> Unit {
    val context = LocalContext.current
    val callback = remember(onImageSelected) { onImageSelected }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { selectedUri ->
            val bytes = context.contentResolver.openInputStream(selectedUri)?.use { it.readBytes() }
            if (bytes != null) {
                val compressedBytes = compressImage(bytes)
                callback(SelectedImage(compressedBytes, "image/jpeg"))
            }
        }
    }
    return { launcher.launch("image/*") }
}

private fun compressImage(bytes: ByteArray): ByteArray {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

    val sampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight, 1600)
    val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return bytes

    val scale = minOf(1f, 1600f / maxOf(bitmap.width, bitmap.height).toFloat())
    val resized = if (scale < 1f) {
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt(),
            (bitmap.height * scale).toInt(),
            true
        )
    } else {
        bitmap
    }

    return resized.toJpeg(80).also {
        if (resized !== bitmap) resized.recycle()
        bitmap.recycle()
    }
}

private fun Bitmap.toJpeg(quality: Int): ByteArray {
    return java.io.ByteArrayOutputStream().use { output ->
        compress(Bitmap.CompressFormat.JPEG, quality, output)
        output.toByteArray()
    }
}

private fun calculateSampleSize(width: Int, height: Int, maxDimension: Int): Int {
    var sampleSize = 1
    while (width / (sampleSize * 2) >= maxDimension && height / (sampleSize * 2) >= maxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}