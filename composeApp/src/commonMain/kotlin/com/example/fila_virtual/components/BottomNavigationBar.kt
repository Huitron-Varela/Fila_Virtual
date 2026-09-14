package com.example.fila_virtual.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

// Importaciones de Recursos
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.*

// Importaciones del Tema Estandarizado
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray

data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

@Composable
fun BottomNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(LightSurface, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding() // Protege la barra de los gestos del sistema (iOS/Android)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = selectedIndex == item.index

            // Animación de color suave usando nuestros colores estandarizados
            val color by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MediumGray,
                animationSpec = tween(300),
                label = "color_anim"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // Evita el efecto "ripple" gris por defecto al tocar
                    ) { onItemSelected(item.index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall, // Usa la tipografía de tu Theme.kt
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = color
                )
            }
        }
    }
}

object NavigationDefaults {
    @Composable
    fun userItems() = listOf(
        NavigationItem(stringResource(Res.string.nav_home), Icons.Default.Home, 0),
        NavigationItem(stringResource(Res.string.nav_orders), Icons.AutoMirrored.Filled.Assignment, 1),
        NavigationItem(stringResource(Res.string.nav_wallet), Icons.Default.AccountBalanceWallet, 2),
        NavigationItem(stringResource(Res.string.nav_profile), Icons.Default.Person, 3)
    )

    @Composable
    fun adminItems() = listOf(
        NavigationItem(stringResource(Res.string.admin_nav_dashboard), Icons.Default.Dashboard, 0),
        NavigationItem(stringResource(Res.string.admin_nav_employees), Icons.Default.People, 1),
        NavigationItem(stringResource(Res.string.admin_nav_menu), Icons.Default.RestaurantMenu, 2),
        NavigationItem(stringResource(Res.string.admin_nav_profile), Icons.Default.Person, 3)
    )
}
