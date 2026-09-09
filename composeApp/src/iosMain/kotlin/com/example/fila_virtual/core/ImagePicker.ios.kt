package com.example.fila_virtual.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberImagePicker(onImageSelected: (SelectedImage) -> Unit): () -> Unit {
    val delegate = remember(onImageSelected) { ImagePickerDelegate(onImageSelected) }
    return {
        val picker = UIImagePickerController()
        picker.sourceType = platform.UIKit.UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
        picker.delegate = delegate
        UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
            picker,
            animated = true,
            completion = null
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ImagePickerDelegate(
    private val onImageSelected: (SelectedImage) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        val data: NSData? = image?.let { compressImage(it) }
        data?.let { onImageSelected(SelectedImage(it.toByteArray(), "image/jpeg")) }
        picker.dismissViewControllerAnimated(true, completion = null)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun compressImage(image: UIImage): NSData? {
    var quality = 0.8
    var data = platform.UIKit.UIImageJPEGRepresentation(image, quality)
    while (data != null && data.length.toLong() > 4 * 1024 * 1024 && quality > 0.4) {
        quality -= 0.1
        data = platform.UIKit.UIImageJPEGRepresentation(image, quality)
    }
    return data
}