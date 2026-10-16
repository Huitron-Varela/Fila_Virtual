package com.example.fila_virtual.features.admin.inicio

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.AppListSkeleton
import com.example.fila_virtual.components.AppPullToRefreshBox
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.RolesEmpleado
import fila_virtual.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun AdminDashboardScreen(
    ownerUid: String,
    onNavigateToManage: () -> Unit = {},
    onNavigateToEmployees: () -> Unit = {},
    viewModel: AdminDashboardViewModel = viewModel()
) {
    val windowSize = LocalWindowSize.current
    val horizontalPadding = windowSize.compactDp(20)
    val isCompact = windowSize.isSmallScreen
    val haptic = LocalHapticFeedback.current
    val dashboard by viewModel.state.collectAsState()

    LaunchedEffect(ownerUid) {
        viewModel.load(ownerUid)
    }

    AppPullToRefreshBox(
        isRefreshing = dashboard.isLoading,
        onRefresh = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.load(ownerUid)
        },
        modifier = Modifier.fillMaxSize().background(LightBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- SECCIÓN FIJA: Header (Consistente con home.kt) ---
            AdminHeader(
                padding = horizontalPadding,
                isRefreshing = dashboard.isLoading,
                onRefresh = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.load(ownerUid)
                }
            )

            Crossfade(
                targetState = when {
                    dashboard.isLoading -> "loading"
                    dashboard.errorMessage != null -> "error"
                    !dashboard.hasEstablishments -> "empty"
                    else -> "content"
                },
                modifier = Modifier.weight(1f),
                label = "admin_dashboard_state"
            ) {
                when (it) {
                    "loading" -> Box(Modifier.fillMaxSize().padding(horizontal = horizontalPadding, vertical = 16.dp)) {
                        AppListSkeleton(itemCount = 5)
                    }
                    "error" -> DashboardMessage(
                        icon = Icons.Default.ErrorOutline,
                        title = "No se pudieron cargar las métricas",
                        description = dashboard.errorMessage.orEmpty(),
                        actionLabel = "Reintentar",
                        onAction = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.load(ownerUid)
                        }
                    )
                    "empty" -> DashboardMessage(
                        icon = Icons.Default.Storefront,
                        title = "Aún no tienes establecimientos",
                        description = "Crea un establecimiento para empezar a revisar sus pedidos y actividad.",
                        actionLabel = "Gestionar establecimientos",
                        onAction = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToManage()
                        }
                    )
                    else -> DashboardContent(
                        state = dashboard,
                        padding = horizontalPadding,
                        isCompact = isCompact,
                        onManageEstablishments = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateToManage()
                        },
                        onEmployeesClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onNavigateToEmployees()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminHeader(padding: Dp, isRefreshing: Boolean = false, onRefresh: () -> Unit = {}) {
    val windowSize = LocalWindowSize.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = padding)
            .padding(top = windowSize.compactDp(24), bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.admin_dashboard_title),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = windowSize.adaptiveSp(20),
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(Res.string.admin_dashboard_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = MediumGray,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: AdminDashboardState,
    padding: Dp,
    isCompact: Boolean,
    onManageEstablishments: () -> Unit,
    onEmployeesClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = padding, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                onClick = onManageEstablishments,
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).background(PrimaryOrange, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Gestionar establecimientos", fontWeight = FontWeight.Bold, color = DarkGray)
                        Text("Revisar locales y menús", style = MaterialTheme.typography.bodySmall, color = MediumGray)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MediumGray)
                }
            }
        }

        item {
            SectionTitle("Resumen de hoy", subtitle = "Datos registrados en tus establecimientos")
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Ventas hoy",
                        value = state.salesToday.asCurrency(),
                        detail = "${state.ordersToday} pedidos no cancelados",
                        icon = Icons.Default.AttachMoney
                    )
                    DashboardMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Ticket promedio",
                        value = state.averageTicket.asCurrency(),
                        detail = "Por pedido de hoy",
                        icon = Icons.Default.Receipt
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DashboardMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Personal activo",
                        value = state.activeEmployees.toString(),
                        detail = "${state.employeesOnShift} en turno",
                        icon = Icons.Default.People
                    )
                    DashboardMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Entregados hoy",
                        value = state.deliveredToday.toString(),
                        detail = "Preparados: ${state.preparedToday}",
                        icon = Icons.Default.TaskAlt
                    )
                }
            }
        }

        item {
            EmployeeActivityCard(
                prepared = state.preparedToday,
                delivered = state.deliveredToday,
                abandoned = state.abandonedToday,
                onClick = onEmployeesClick
            )
        }

        item {
            SalesTrendCard(state.salesByDay, isCompact)
        }

        item {
            SectionTitle("Más vendidos", subtitle = "Productos de pedidos no cancelados · 7 días")
        }

        if (state.bestSellers.isEmpty()) {
            item {
                Text(
                    "Aún no hay ventas registradas para este periodo.",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    color = MediumGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            items(state.bestSellers, key = { it.name }) { product ->
                TopProductCard(product)
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkGray)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MediumGray)
    }
}

@Composable
private fun DashboardMetricCard(
    modifier: Modifier,
    title: String,
    value: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(if (LocalWindowSize.current.isSmallScreen) 12.dp else 16.dp)) {
            Icon(icon, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = MediumGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MediumGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun EmployeeActivityCard(prepared: Int, delivered: Int, abandoned: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Groups, contentDescription = null, tint = PrimaryOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Actividad del equipo", fontWeight = FontWeight.Bold, color = DarkGray, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, contentDescription = "Ver empleados", tint = MediumGray)
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityValue(Modifier.weight(1f), "Preparados", prepared)
                ActivityValue(Modifier.weight(1f), "Entregados", delivered)
                ActivityValue(Modifier.weight(1f), "No recogidos", abandoned)
            }
        }
    }
}

@Composable
private fun ActivityValue(modifier: Modifier, label: String, value: Int) {
    Column(modifier = modifier.background(ExtraLightGray, RoundedCornerShape(10.dp)).padding(10.dp)) {
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkGray)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MediumGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SalesTrendCard(salesByDay: List<DashboardDaySales>, isCompact: Boolean) {
    val maxSales = salesByDay.maxOfOrNull { it.amount } ?: 0.0
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(if (isCompact) 14.dp else 18.dp)) {
            Text("Ventas · últimos 7 días", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkGray)
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(136.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                salesByDay.forEach { day ->
                    val fraction = if (maxSales > 0.0) (day.amount / maxSales).toFloat() else 0f
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (day.amount > 0.0) {
                            Text(
                                text = day.amount.toShortCurrency(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MediumGray,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .width(if (isCompact) 18.dp else 28.dp)
                                .height(if (fraction == 0f) 4.dp else (76.dp * fraction).coerceAtLeast(8.dp))
                                .background(if (day.amount == maxSales && maxSales > 0.0) PrimaryOrange else LightOrange, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(day.label, style = MaterialTheme.typography.labelSmall, color = MediumGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardMessage(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(modifier = Modifier.size(76.dp), shape = CircleShape, color = SoftOrangeBg) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(34.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkGray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(6.dp))
        Text(description, style = MaterialTheme.typography.bodySmall, color = MediumGray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(modifier = Modifier.height(18.dp))
        Button(onClick = onAction, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)) {
            Text(actionLabel, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TopProductCard(product: DashboardTopProduct) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderGray.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(ExtraLightGray), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Fastfood, contentDescription = null, tint = MediumGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, color = DarkGray, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${product.units} vendidos", style = MaterialTheme.typography.bodySmall, color = MediumGray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(product.grossSales.asCurrency(), fontWeight = FontWeight.Bold, color = PrimaryOrange, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

private fun Double.asCurrency(): String = "$${toLong().toString().withThousandsSeparator()}"

private fun Double.toShortCurrency(): String = when {
    this >= 1_000_000 -> "$${(this / 1_000_000).toInt()}M"
    this >= 1_000 -> "$${(this / 1_000).toInt()}k"
    else -> "$${toInt()}"
}

private fun String.withThousandsSeparator(): String =
    reversed().chunked(3).joinToString(",").reversed()
