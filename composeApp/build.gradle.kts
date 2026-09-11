import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    // --- LÍNEA NUEVA PARA FIREBASE ---
    id("com.google.gms.google-services")
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.core.ktx)

            // Firebase Nativo Android
            implementation("com.google.android.gms:play-services-auth:21.0.0")
            implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
            implementation("com.google.firebase:firebase-firestore-ktx:24.10.1")
            implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

            // Motor Ktor para Android (Asegúrate de que esté en libs.versions.toml)
            implementation(libs.ktor.client.okhttp)

            // 1. Esto soluciona el error "(Kotlin reflection is not available)"
            implementation(kotlin("reflect"))
        }
        commonMain.dependencies {
            // 1. Interfaz Multiplataforma (Usamos compose.* en lugar de libs.compose.*)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)
            // Core de Compose usando el plugin nativo
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3) // <-- ¡Esto soluciona el error rojo de Material 3!
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)

            // ViewModels y Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.compose.uiToolingPreview)

            // 2. Lifecycle adaptado para KMP
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

            // 3. Firebase KMP (Incluyendo la nueva librería de functions)
            implementation("dev.gitlive:firebase-auth:1.11.1")
            implementation("dev.gitlive:firebase-firestore:1.11.1")
            implementation("dev.gitlive:firebase-functions:1.11.1")
            implementation("dev.gitlive:firebase-storage:1.11.1")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.cio) // Motor de reserva
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation("io.ktor:ktor-client-logging:2.3.12")
            implementation(libs.ktor.client.cio)

            // 5. Imagenes
            implementation("io.ktor:ktor-client-logging:2.3.12")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

            // Imagenes
            implementation(libs.kamel.image)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.fila_virtual"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.fila_virtual"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        // Eliminamos la línea de ktor que estaba causando el error aquí
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}