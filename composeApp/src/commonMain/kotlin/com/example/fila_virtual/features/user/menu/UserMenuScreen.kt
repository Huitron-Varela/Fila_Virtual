package com.example.fila_virtual.features.user.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.core.BackHandler
import com.example.fila_virtual.features.user.UserViewModel

import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.components.RemoteImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMenuScreen(
    establecimientoId: String,
    nombreEstablecimiento: String,
    onBack: () -> Unit,
    userViewModel: UserViewModel // <--- Recibimos el UserViewModel para acceder al carrito
) {
    val menuViewModel = remember { UserMenuViewModel() }
    val productos by menuViewModel.productos.collectAsState()

    // Estado para mostrar una confirmación rápida cuando agregas algo
    var showSnackbar by remember { mutableStateOf(false) }
    var lastAddedProduct by remember { mutableStateOf("") }

    BackHandler(onBack = onBack)

    // En cuanto se abre la pantalla, le decimos al cerebro que cargue el menú de este local
    LaunchedEffect(establecimientoId) {
        menuViewModel.cargarMenu(establecimientoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(nombreEstablecimiento, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        // Agregamos un Snackbar para darle feedback al usuario
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showSnackbar = false }) {
                            Text("OK", color = PrimaryOrange)
                        }
                    }
                ) {
                    Text("$lastAddedProduct agregado al carrito")
                }
                LaunchedEffect(showSnackbar) {
                    kotlinx.coroutines.delay(2000)
                    showSnackbar = false
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (productos.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Cargando menú o no hay platillos disponibles...", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(productos.size) { index ->
                        val producto = productos[index]
                        ProductoClienteCard(
                            producto = producto,
                            onAddClick = {
                                // AQUÍ ESTÁ LA MAGIA: Guardamos en el carrito de verdad
                                userViewModel.agregarAlCarrito(
                                    idProducto = producto.id,
                                    nombre = producto.nombre,
                                    precio = producto.precio
                                )
                                // Mostramos el mensajito de éxito
                                lastAddedProduct = producto.nombre
                                showSnackbar = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoClienteCard(producto: Producto, onAddClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color(0xFF1E1E24)),
                contentAlignment = Alignment.Center
            ) {
                RemoteImage(
                    url = producto.imagenUrl,
                    contentDescription = producto.nombre,
                    modifier = Modifier.fillMaxSize(),
                    fallback = {
                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = producto.nombre, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                    Text(
                        text = producto.descripcion.ifEmpty { "Sin descripción" },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$${producto.precio}", color = PrimaryOrange, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onAddClick() },
                    color = PrimaryOrange
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}