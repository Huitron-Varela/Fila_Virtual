package com.example.fila_virtual.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.core.BackHandler
import com.example.fila_virtual.components.Heders.StoreHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(title: String, content: String, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            StoreHeader(
                title = title,
                onBack = onBack,
                containerColor = MaterialTheme.colorScheme.surface,
                backButtonContainerColor = Color.Transparent,
                titleStyle = MaterialTheme.typography.titleLarge,
                titleColor = MaterialTheme.colorScheme.onSurface,
                backContentDescription = "Regresar"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entendido")
            }
        }
    }
}