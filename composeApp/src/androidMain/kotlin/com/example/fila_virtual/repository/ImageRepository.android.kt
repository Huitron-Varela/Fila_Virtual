package com.example.fila_virtual.repository

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await

private const val STORAGE_BUCKET = "gs://altoque-c1c87.firebasestorage.app"

actual suspend fun uploadImage(path: String, bytes: ByteArray, mimeType: String): Result<String> {
    if (bytes.size > 5 * 1024 * 1024) {
        return Result.failure(IllegalArgumentException("La imagen supera el límite de 5 MB"))
    }
    return try {
        val reference = FirebaseStorage.getInstance(STORAGE_BUCKET).reference.child(path)
        val metadata = StorageMetadata.Builder()
            .setContentType(mimeType)
            .build()
        reference.putBytes(bytes, metadata).await()
        Result.success(reference.downloadUrl.await().toString())
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}