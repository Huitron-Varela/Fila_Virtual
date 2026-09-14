package com.example.fila_virtual.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.File
import dev.gitlive.firebase.storage.storage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.NSTemporaryDirectory

@OptIn(ExperimentalForeignApi::class)
actual suspend fun uploadImage(path: String, bytes: ByteArray, mimeType: String): Result<String> {
    if (bytes.size > 5 * 1024 * 1024) {
        return Result.failure(IllegalArgumentException("La imagen supera el límite de 5 MB"))
    }
    return try {
        val temporaryPath = NSTemporaryDirectory() + "upload-${bytes.hashCode()}.jpg"
        val data = bytes.usePinned { pinned ->
            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
        }
        check(data.writeToFile(temporaryPath, atomically = true)) { "No se pudo preparar la imagen" }
        val reference = Firebase.storage.reference.child(path)
        reference.putFile(File(NSURL.fileURLWithPath(temporaryPath)))
        Result.success(reference.getDownloadUrl())
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}