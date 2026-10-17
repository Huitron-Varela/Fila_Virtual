package com.example.fila_virtual.features.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateColorAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fila_virtual.components.RemoteImage
import com.example.fila_virtual.components.cards.FeaturedEstablishmentCard
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.data.Establecimiento
import fila_virtual.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeHeader(padding: Dp, cartCount: Int, onCartClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.client_welcome),
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = DarkGray,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        BadgedBox(
            badge = {
                if (cartCount > 0) {
                    Badge(containerColor = PrimaryOrange, contentColor = Color.White) {
                        Text(cartCount.toString())
                    }
                }
            }
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCartClick()
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(BorderGray.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = stringResource(Res.string.user_cart_open),
                    modifier = Modifier.padding(10.dp),
                    tint = DarkGray
                )
            }
        }
    }
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

fun getIconForCategory(categoria: String): ImageVector {
    return when (categoria.lowercase()) {
        "comida", "comidas" -> Icons.Default.Restaurant
        "bebidas", "bebida", "café", "cafe" -> Icons.Default.LocalCafe
        "snacks", "botanas" -> Icons.Default.Fastfood
        "postres", "postre" -> Icons.Default.Icecream
        "todos" -> Icons.Default.Menu
        else -> Icons.Default.Category
    }
}

@Composable
fun CategoryRow(
    categorias: List<String>,
    seleccion: String,
    onSelect: (String) -> Unit,
    padding: Dp
) {
    val haptic = LocalHapticFeedback.current
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = padding),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categorias) { name ->
            val isSelected = name == seleccion
            val chipColor by animateColorAsState(
                targetValue = if (isSelected) PrimaryOrange else LightSurface,
                label = "category_chip_color"
            )

            Surface(
                modifier = Modifier.clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect(name)
                },
                shape = RoundedCornerShape(12.dp),
                color = chipColor,
                border = if (isSelected) null else BorderStroke(1.dp, BorderGray)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = getIconForCategory(name),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) Color.White else PrimaryOrange
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else DarkGray,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
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
