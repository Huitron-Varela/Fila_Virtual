package com.example.fila_virtual.features.admin.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Empleado
import com.example.fila_virtual.data.EstadoPedido
import com.example.fila_virtual.data.Pedido
import com.example.fila_virtual.data.ProductoCarrito
import com.example.fila_virtual.repository.CajeroRepository
import com.example.fila_virtual.repository.EmpleadoRepository
import com.example.fila_virtual.repository.EstablecimientoRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

data class AdminDashboardState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val hasEstablishments: Boolean = true,
    val salesToday: Double = 0.0,
    val ordersToday: Int = 0,
    val averageTicket: Double = 0.0,
    val activeEmployees: Int = 0,
    val employeesOnShift: Int = 0,
    val preparedToday: Int = 0,
    val deliveredToday: Int = 0,
    val abandonedToday: Int = 0,
    val salesByDay: List<DashboardDaySales> = emptyList(),
    val bestSellers: List<DashboardTopProduct> = emptyList()
)

data class DashboardDaySales(val label: String, val amount: Double)

data class DashboardTopProduct(val name: String, val units: Int, val grossSales: Double)

class AdminDashboardViewModel(
    private val establecimientoRepository: EstablecimientoRepository = EstablecimientoRepository(),
    private val empleadoRepository: EmpleadoRepository = EmpleadoRepository(),
    private val cajeroRepository: CajeroRepository = CajeroRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(AdminDashboardState())
    val state: StateFlow<AdminDashboardState> = _state.asStateFlow()

    private var loadJob: Job? = null

    fun load(ownerUid: String) {
        loadJob?.cancel()
        if (ownerUid.isBlank()) {
            _state.value = AdminDashboardState(isLoading = false, errorMessage = "No se pudo identificar al administrador.")
            return
        }

        loadJob = viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val establecimientos = establecimientoRepository.getEstablecimientosByOwner(ownerUid).first()
                if (establecimientos.isEmpty()) {
                    _state.value = AdminDashboardState(isLoading = false, hasEstablishments = false)
                    return@launch
                }

                val storeData = coroutineScope {
                    establecimientos.map { establecimiento ->
                        async {
                            val pedidos = cajeroRepository.escuchar(establecimiento.id).first()
                            val empleados = empleadoRepository.getEmpleadosFlow(establecimiento.id).first()
                            StoreDashboardData(pedidos, empleados)
                        }
                    }.awaitAll()
                }

                val hoy = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date
                val fechasSemana = (6 downTo 0).map { diasAtras -> hoy - DatePeriod(days = diasAtras) }
                val empleadosActivos = storeData
                    .flatMap { it.empleados }
                    .distinctBy(Empleado::uid)
                    .filter(Empleado::activo)

                val metricasEmpleados = coroutineScope {
                    empleadosActivos.map { empleado ->
                        async { leerMetricasEmpleado(empleado.uid, hoy) }
                    }.awaitAll()
                }

                val todosLosPedidos = storeData.flatMap { it.pedidos }
                val pedidosConVenta = todosLosPedidos.filter { it.estado != EstadoPedido.CANCELADO }
                val pedidosHoy = pedidosConVenta.filter { it.createdAt.toLocalDateOrNull() == hoy }
                val pedidosUltimosSieteDias = pedidosConVenta.filter { it.createdAt.toLocalDateOrNull() in fechasSemana }
                val ventasHoy = pedidosHoy.sumOf(Pedido::total)

                val acumuladoProductos = mutableMapOf<String, ProductSalesAccumulator>()
                pedidosUltimosSieteDias.forEach { pedido ->
                    pedido.productos.forEach { producto ->
                        val key = producto.idProducto.ifBlank { producto.nombre }
                        val actual = acumuladoProductos[key]
                        if (actual == null) {
                            acumuladoProductos[key] = ProductSalesAccumulator(
                                name = producto.nombre,
                                units = producto.cantidad,
                                grossSales = producto.precio * producto.cantidad
                            )
                        } else {
                            actual.units += producto.cantidad
                            actual.grossSales += producto.precio * producto.cantidad
                        }
                    }
                }

                val pedidosPorFecha = pedidosConVenta.groupBy { it.createdAt.toLocalDateOrNull() }
                _state.value = AdminDashboardState(
                    isLoading = false,
                    hasEstablishments = true,
                    salesToday = ventasHoy,
                    ordersToday = pedidosHoy.size,
                    averageTicket = if (pedidosHoy.isEmpty()) 0.0 else ventasHoy / pedidosHoy.size,
                    activeEmployees = empleadosActivos.size,
                    employeesOnShift = metricasEmpleados.count { it.onShift },
                    preparedToday = metricasEmpleados.sumOf { it.prepared },
                    deliveredToday = metricasEmpleados.sumOf { it.delivered },
                    abandonedToday = metricasEmpleados.sumOf { it.abandoned },
                    salesByDay = fechasSemana.map { fecha ->
                        DashboardDaySales(
                            label = fecha.weekdayLabel(),
                            amount = pedidosPorFecha[fecha].orEmpty().sumOf(Pedido::total)
                        )
                    },
                    bestSellers = acumuladoProductos.values
                        .sortedByDescending(ProductSalesAccumulator::units)
                        .take(5)
                        .map { DashboardTopProduct(it.name, it.units, it.grossSales) }
                )
            } catch (exception: Exception) {
                _state.value = AdminDashboardState(
                    isLoading = false,
                    errorMessage = exception.message ?: "No se pudieron cargar las métricas."
                )
            }
        }
    }

    private suspend fun leerMetricasEmpleado(uid: String, hoy: LocalDate): EmployeeDayMetrics {
        if (uid.isBlank()) return EmployeeDayMetrics()
        return runCatching {
            val usuario = Firebase.firestore.collection("usuarios").document(uid).get()
            if (!usuario.exists) return@runCatching EmployeeDayMetrics()

            val fechaGuardada = runCatching { usuario.get<String>("metricaFecha") }.getOrNull()
            val esHoy = fechaGuardada == hoy.toString()
            val sesionActiva = runCatching { usuario.get<Boolean>("sesionActiva") }.getOrNull() ?: false
            val inicioSesion = readLong(usuario, "sesionInicioAt").toLocalDateOrNull()
            val sesionDelDia = sesionActiva && inicioSesion == hoy

            EmployeeDayMetrics(
                onShift = sesionActiva,
                prepared = (if (esHoy) readInt(usuario, "metricaDiaPreparados") else 0) +
                    (if (sesionDelDia) readInt(usuario, "sesionPreparados") else 0),
                delivered = (if (esHoy) readInt(usuario, "metricaDiaEntregados") else 0) +
                    (if (sesionDelDia) readInt(usuario, "sesionEntregados") else 0),
                abandoned = (if (esHoy) readInt(usuario, "metricaDiaAbandonados") else 0) +
                    (if (sesionDelDia) readInt(usuario, "sesionAbandonados") else 0)
            )
        }.getOrDefault(EmployeeDayMetrics())
    }

    private fun readInt(document: dev.gitlive.firebase.firestore.DocumentSnapshot, field: String): Int =
        runCatching { document.get<Long>(field).toInt() }.getOrNull()
            ?: runCatching { document.get<Int>(field) }.getOrDefault(0)

    private fun readLong(document: dev.gitlive.firebase.firestore.DocumentSnapshot, field: String): Long =
        runCatching { document.get<Long>(field) }.getOrDefault(0L)
}

private data class StoreDashboardData(
    val pedidos: List<Pedido>,
    val empleados: List<Empleado>
)

private data class EmployeeDayMetrics(
    val onShift: Boolean = false,
    val prepared: Int = 0,
    val delivered: Int = 0,
    val abandoned: Int = 0
)

private data class ProductSalesAccumulator(
    val name: String,
    var units: Int,
    var grossSales: Double
)

private fun Long.toLocalDateOrNull(): LocalDate? =
    takeIf { it > 0L }?.let {
        Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

private fun LocalDate.weekdayLabel(): String =
    listOf("L", "M", "X", "J", "V", "S", "D")[dayOfWeek.isoDayNumber - 1]