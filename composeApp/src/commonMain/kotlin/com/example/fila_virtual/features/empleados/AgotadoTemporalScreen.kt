package com.example.fila_virtual.features.empleados

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightBackground
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.data.Producto
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_back
import fila_virtual.composeapp.generated.resources.emp_stock_empty
import fila_virtual.composeapp.generated.resources.emp_stock_help
import fila_virtual.composeapp.generated.resources.emp_stock_mark
import fila_virtual.composeapp.generated.resources.emp_stock_restore
import fila_virtual.composeapp.generated.resources.emp_stock_title
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.math.round

@Composable
fun AgotadoTemporalScreen(
    establecimientoId: String,
    onBack: () -> Unit,
    viewModel: AgotadoViewModel = viewModel()
) {
    LaunchedEffect(establecimientoId) {
        viewModel.escuchar(establecimientoId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.emp_back),
                tint = DarkGray,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onBack)
                    .padding(12.dp)
            )
            Text(
                stringResource(Res.string.emp_stock_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )
        }
        Text(
            stringResource(Res.string.emp_stock_help),
            color = MediumGray,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        if (viewModel.error.isNotBlank()) {
            Text(
                viewModel.error,
                color = TrafficRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                viewModel.cargando && viewModel.productos.isEmpty() -> {
                    CircularProgressIndicator(color = PrimaryOrange, modifier = Modifier.align(Alignment.Center))
                }
                viewModel.productos.isEmpty() -> {
                    Text(
                        stringResource(Res.string.emp_stock_empty),
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MediumGray
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(viewModel.productos, key = { it.id }) { producto ->
                            ProductoAgotadoCard(
                                producto = producto,
                                enCambio = viewModel.productoEnCambio == producto.id,
                                onCambiar = { viewModel.cambiarDisponibilidad(producto) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductoAgotadoCard(
    producto: Producto,
    enCambio: Boolean,
    onCambiar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(producto.nombre, fontWeight = FontWeight.Bold, color = DarkGray, style = MaterialTheme.typography.titleMedium)
            if (producto.descripcion.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(producto.descripcion, color = MediumGray, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Text(dineroProducto(producto.precio), color = DarkGray, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            if (producto.disponible) {
                Button(
                    onClick = onCambiar,
                    enabled = !enCambio,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text(stringResource(Res.string.emp_stock_mark), fontWeight = FontWeight.SemiBold)
                }
            } else {
                OutlinedButton(
                    onClick = onCambiar,
                    enabled = !enCambio,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(Res.string.emp_stock_restore), color = TrafficRed)
                }
            }
        }
    }
}

private fun dineroProducto(total: Double): String {
    val centavos = round(total * 100).toInt()
    val pesos = centavos / 100
    val cents = abs(centavos % 100).toString().padStart(2, '0')
    return "$$pesos.$cents"
}
