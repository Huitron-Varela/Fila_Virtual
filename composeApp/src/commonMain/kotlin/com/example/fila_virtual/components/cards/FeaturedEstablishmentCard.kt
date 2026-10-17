package com.example.fila_virtual.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.RemoteImage
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.ExtraLightGray
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.TrafficGreen
import com.example.fila_virtual.data.Establecimiento
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.est_no_address
import fila_virtual.composeapp.generated.resources.est_no_name
import fila_virtual.composeapp.generated.resources.est_status_active
import fila_virtual.composeapp.generated.resources.est_status_inactive
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.round

@Composable
fun FeaturedEstablishmentCard(
    establecimiento: Establecimiento,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val categorias = establecimiento.categorias.take(2).joinToString(" • ")
    val subtitulo = categorias.ifBlank { establecimiento.descripcion }
    val rating = round(establecimiento.ratingAvg.coerceIn(0.0, 5.0) * 10) / 10
    val estadoColor = if (establecimiento.activo) TrafficGreen else MediumGray

    Card(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier.fillMaxWidth().heightIn(min = 112.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(ExtraLightGray),
                contentAlignment = Alignment.Center
            ) {
                RemoteImage(
                    url = establecimiento.logoUrl,
                    contentDescription = establecimiento.nombre,
                    modifier = Modifier.fillMaxSize(),
                    fallback = {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = MediumGray)
                    }
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopStart).padding(4.dp),
                    color = if (establecimiento.activo) TrafficGreen else ExtraLightGray,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        stringResource(if (establecimiento.activo) Res.string.est_status_active else Res.string.est_status_inactive),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        color = if (establecimiento.activo) Color.White else DarkGray,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        establecimiento.nombre.ifBlank { stringResource(Res.string.est_no_name) },
                        modifier = Modifier.weight(1f),
                        color = DarkGray,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (establecimiento.ratingCount > 0) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(3.dp))
                        Text(
                            rating.toString(),
                            color = DarkGray,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    subtitulo.ifBlank { stringResource(Res.string.est_no_address) },
                    color = PrimaryOrange,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MediumGray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        establecimiento.ubicacion.direccion.ifBlank { stringResource(Res.string.est_no_address) },
                        color = MediumGray,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediumGray)
        }
    }
}
