package com.example.fila_virtual.core

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

@Composable
actual fun VisorCodigoQr(
    modifier: Modifier,
    onCodigo: (String) -> Unit,
    onError: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val alLeer = rememberUpdatedState(onCodigo)
    val alFallar = rememberUpdatedState(onError)
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val yaLeido = remember { AtomicBoolean(false) }

    AndroidView(factory = { previewView }, modifier = modifier)

    DisposableEffect(lifecycleOwner) {
        val futuro = ProcessCameraProvider.getInstance(context)
        val principal = ContextCompat.getMainExecutor(context)
        val analisis = Executors.newSingleThreadExecutor()
        val lector = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
        val alListo = Runnable {
            try {
                val camaras = futuro.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val lectura = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()
                lectura.setAnalyzer(analisis) { imagen ->
                    if (yaLeido.get()) {
                        imagen.close()
                        return@setAnalyzer
                    }
                    val media = imagen.image
                    if (media == null) {
                        imagen.close()
                        return@setAnalyzer
                    }
                    val entrada = InputImage.fromMediaImage(media, imagen.imageInfo.rotationDegrees)
                    lector.process(entrada)
                        .addOnSuccessListener { codigos ->
                            val texto = codigos.firstOrNull()?.rawValue?.trim().orEmpty()
                            if (texto.isNotEmpty() && yaLeido.compareAndSet(false, true)) {
                                principal.execute { alLeer.value(texto) }
                            }
                        }
                        .addOnFailureListener { fallo ->
                            Log.e("EscanerQr", "No se pudo leer el QR", fallo)
                        }
                        .addOnCompleteListener {
                            imagen.close()
                        }
                }
                camaras.unbindAll()
                camaras.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    lectura
                )
            } catch (error: Exception) {
                Log.e("EscanerQr", "No se pudo abrir la cámara", error)
                principal.execute {
                    alFallar.value("No se pudo abrir la cámara. Revisa el permiso y que haya una cámara disponible.")
                }
            }
        }
        futuro.addListener(alListo, principal)
        onDispose {
            if (futuro.isDone) {
                runCatching { futuro.get().unbindAll() }
            }
            lector.close()
            analisis.shutdown()
        }
    }
}
