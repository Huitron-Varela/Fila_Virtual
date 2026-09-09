package com.example.fila_virtual.features.admin.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*

// Datos de demostración para el dashboard — en producción vendrían del ViewModel
private val DEMO_DAYS = listOf("L", "M", "M", "J", "V", "S", "D")
private val DEMO_VALUES = listOf(0.4f, 0.3f, 0.6f, 0.8f, 1.0f, 0.7f, 0.5f)
private val DEMO_TOP_PRODUCTS = listOf(
    TopProduct("Hamburguesa Clásica", "84 unidades vendidas", "$1,250", "+ 5% hoy", TrafficGreen),
    TopProduct("Pizza Familiar", "22 unidades vendidas", "$2,100", "Sin cambios", MediumGray)
)

@Composable
fun AdminDashboardScreen(onNavigateToManage: () -> Unit = {}) {
    val windowSize = LocalWindowSize.current
    val horizontalPadding = windowSize.adaptiveDp(24).value.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        // --- SECCIÓN FIJA: Header (Consistente con home.kt) ---
        Spacer(modifier = Modifier.height(16.dp))
        AdminHeader(padding = horizontalPadding)
        Spacer(modifier = Modifier.height(16.dp))

        // --- SECCIÓN DESPLAZABLE: Contenido ---
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 8.dp)
        ) {
            // 1. MÉTRICAS (Fila de 3 tarjetas)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricSmallCard(Modifier.weight(1f), "VENTAS", "$45,230", "+ 12%", TrafficGreen)
                    MetricSmallCard(Modifier.weight(1f), "ÓRDENES", "128", "- 8.4%", TrafficRed)
                    MetricSmallCard(Modifier.weight(1f), "TICKET", "$350", "- 0.0%", MediumGray)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. BANNER: Gestionar Establecimientos
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToManage() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PrimaryOrange, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Gestionar Establecimientos",
                                fontWeight = FontWeight.Bold,
                                color = DarkGray,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "ADMINISTRAR SUCURSALES Y LOCALES",
                                style = MaterialTheme.typography.labelSmall,
                                color = MediumGray
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediumGray)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // 3. GRÁFICA DE RENDIMIENTO
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text("TENDENCIA DE VENTAS", style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
                            Text("Rendimiento Semanal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = DarkGray)
                        }
                        Surface(color = SoftOrangeBg, shape = RoundedCornerShape(8.dp)) {
                            Text("ESTA SEMANA", color = PrimaryOrange, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Representación de Barras (Días de la semana)
                    Row(
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        DEMO_DAYS.forEachIndexed { index, day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (day == "V") {
                                    Surface(color = PrimaryOrange, shape = RoundedCornerShape(4.dp)) {
                                        Text("$4.5k", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .width(30.dp)
                                        .fillMaxHeight(DEMO_VALUES[index])
                                        .background(
                                            if (day == "V") PrimaryOrange else ExtraLightGray,
                                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(day, fontSize = 12.sp, color = MediumGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // 4. SECCIÓN: MÁS VENDIDOS
            item {
                Text("MÁS VENDIDOS", style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            }

            val topProducts = DEMO_TOP_PRODUCTS

            items(topProducts) { product ->
                TopProductCard(product)
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun AdminHeader(padding: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Panel de Control 👋",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )
            Text(
                text = "ADMIN DASHBOARD",
                style = MaterialTheme.typography.labelSmall,
                color = MediumGray,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .clickable { /* Notificaciones */ },
            color = BorderGray.copy(alpha = 0.4f)
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = "Notificaciones",
                modifier = Modifier.padding(10.dp),
                tint = DarkGray
            )
        }
    }
}

@Composable
fun MetricSmallCard(modifier: Modifier, title: String, value: String, trend: String, trendColor: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MediumGray, fontWeight = FontWeight.Bold)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = DarkGray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (trend.contains("+")) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(trend, style = MaterialTheme.typography.labelSmall, color = trendColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

data class TopProduct(val name: String, val sold: String, val price: String, val status: String, val statusColor: Color)

@Composable
fun TopProductCard(product: TopProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(ExtraLightGray), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, color = DarkGray, style = MaterialTheme.typography.bodyLarge)
                Text(product.sold, style = MaterialTheme.typography.bodySmall, color = MediumGray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(product.price, fontWeight = FontWeight.ExtraBold, color = DarkGray, style = MaterialTheme.typography.bodyLarge)
                Text(product.status, style = MaterialTheme.typography.labelSmall, color = product.statusColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}
