package com.example.fila_virtual.features.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.CategoryFilterRow
import com.example.fila_virtual.components.Heders.StoreHeader
import com.example.fila_virtual.components.cards.FeaturedEstablishmentCard
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.data.Establecimiento
import fila_virtual.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeHeader(cartCount: Int, onCartClick: () -> Unit) {
    StoreHeader(
        title = stringResource(Res.string.client_welcome),
        cartCount = cartCount,
        onCartClick = onCartClick,
        containerColor = MaterialTheme.colorScheme.background,
        titleStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        centerTitle = false,
        includeStatusBarPadding = false,
        minHeight = 72.dp,
        horizontalPadding = 12.dp,
        verticalPadding = 8.dp
    )
}

@Composable
fun SectionHeader(title: String, padding: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 20.dp)
                .background(PrimaryOrange, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CategoryRow(
    categorias: List<String>,
    seleccion: String,
    onSelect: (String) -> Unit,
    padding: Dp
) {
    CategoryFilterRow(
        categories = categorias,
        selectedCategory = seleccion,
        onCategorySelected = onSelect,
        contentPadding = PaddingValues(horizontal = padding)
    )
}

@Composable
fun CafeteriasList(
    establecimientos: List<Establecimiento>,
    padding: Dp,
    onEstablecimientoClick: (Establecimiento) -> Unit
) {
    val windowSize = LocalWindowSize.current
    val cardWidth = (windowSize.width - padding * 2).coerceIn(260.dp, 420.dp)
    if (establecimientos.isEmpty()) {
        Text(
            text = "Buscando establecimientos...",
            modifier = Modifier.padding(horizontal = padding),
            color = MediumGray
        )
    } else {
        LazyRow(
            contentPadding = PaddingValues(horizontal = padding),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(establecimientos) { establecimiento ->
                FeaturedEstablishmentCard(
                    establecimiento = establecimiento,
                    onClick = { onEstablecimientoClick(establecimiento) },
                    modifier = Modifier.width(cardWidth)
                )
            }
        }
    }
}
