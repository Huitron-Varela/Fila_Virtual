import re

file_path = "composeApp/src/commonMain/kotlin/com/example/fila_virtual/features/admin/inicio/AdminDashboardScreen.kt"

with open(file_path, 'r') as f:
    content = f.read()

replacements = [
    ('"Panel de Control 👋"', 'stringResource(Res.string.dashboard_title)'),
    ('"ADMIN DASHBOARD"', 'stringResource(Res.string.dashboard_subtitle)'),
    ('"Gestionar Establecimientos"', 'stringResource(Res.string.dashboard_manage_establishments)'),
    ('"ADMINISTRAR SUCURSALES Y LOCALES"', 'stringResource(Res.string.dashboard_manage_desc)'),
    ('"TENDENCIA DE VENTAS"', 'stringResource(Res.string.dashboard_sales_trend)'),
    ('"Rendimiento Semanal"', 'stringResource(Res.string.dashboard_weekly_performance)'),
    ('"ESTA SEMANA"', 'stringResource(Res.string.dashboard_this_week)'),
    ('"MÁS VENDIDOS"', 'stringResource(Res.string.dashboard_top_selling)'),
    ('"VENTAS"', 'stringResource(Res.string.dashboard_sales)'),
    ('"ÓRDENES"', 'stringResource(Res.string.dashboard_orders)'),
    ('"TICKET"', 'stringResource(Res.string.dashboard_ticket)')
]

for old, new in replacements:
    content = content.replace(old, new)

if 'import org.jetbrains.compose.resources.stringResource' not in content:
    content = content.replace('import androidx.compose.ui.unit.dp', 
                              'import androidx.compose.ui.unit.dp\nimport org.jetbrains.compose.resources.stringResource\nimport fila_virtual.composeapp.generated.resources.*')

with open(file_path, 'w') as f:
    f.write(content)

print("AdminDashboardScreen updated.")
