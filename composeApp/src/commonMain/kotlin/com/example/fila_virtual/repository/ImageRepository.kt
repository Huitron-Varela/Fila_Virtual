package com.example.fila_virtual.repository

expect suspend fun uploadImage(path: String, bytes: ByteArray, mimeType: String): Result<String>