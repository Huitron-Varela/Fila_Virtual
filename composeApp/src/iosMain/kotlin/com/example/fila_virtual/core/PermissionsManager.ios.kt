package com.example.fila_virtual.core

import androidx.compose.runtime.*
import platform.Photos.PHAccessLevelReadWrite
import platform.Photos.PHAuthorizationStatusAuthorized
import platform.Photos.PHAuthorizationStatusLimited
import platform.Photos.PHAuthorizationStatusNotDetermined
import platform.Photos.PHPhotoLibrary

class IosPermissionsManager : PermissionsManager {
    override fun askPermission(permission: PermissionType, callback: (Boolean) -> Unit) {
        if (permission != PermissionType.GALLERY) {
            callback(true)
            return
        }

        PHPhotoLibrary.requestAuthorizationForAccessLevel(PHAccessLevelReadWrite) { status ->
            callback(status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited)
        }
    }

    override fun isPermissionGranted(permission: PermissionType): Boolean {
        if (permission != PermissionType.GALLERY) return true
        val status = PHPhotoLibrary.authorizationStatusForAccessLevel(PHAccessLevelReadWrite)
        return status == PHAuthorizationStatusAuthorized || status == PHAuthorizationStatusLimited
    }
}

@Composable
actual fun rememberPermissionsManager(): PermissionsManager {
    return remember { IosPermissionsManager() }
}
