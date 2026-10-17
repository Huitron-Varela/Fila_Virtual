package com.example.fila_virtual.features.user.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.cards.RecommendationProductCard
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.data.Producto

@Composable
fun RecommendationsList(
    productosConLocal: List<Pair<Producto, String>>,
    padding: Dp,
    onQuickAdd: (Producto) -> Unit,
    onProductClick: (Producto, String) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = padding)) {
        if (productosConLocal.isEmpty()) {
            Text(
                text = "No se encontraron resultados...",
                color = MediumGray,
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            productosConLocal.forEach { (producto, nombreLocal) ->
                RecommendationProductCard(
                    producto = producto,
                    storeName = nombreLocal,
                    onClick = { onProductClick(producto, nombreLocal) },
                    onAddToCartClick = { onQuickAdd(producto) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}