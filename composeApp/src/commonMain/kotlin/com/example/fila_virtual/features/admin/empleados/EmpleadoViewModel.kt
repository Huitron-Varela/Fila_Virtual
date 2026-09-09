package com.example.fila_virtual.features.admin.empleados

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fila_virtual.data.Empleado
import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.data.EmpleadoDetalle
import com.example.fila_virtual.data.InvitacionEmpleado
import com.example.fila_virtual.data.Usuario
import com.example.fila_virtual.features.admin.FormState
import com.example.fila_virtual.repository.EmpleadoRepository
import com.example.fila_virtual.repository.EmployeeInvitationEmailTemplate
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.random.Random

class EmpleadoViewModel : ViewModel() {

    private val repository = EmpleadoRepository()
    private val db = Firebase.firestore

    private val _uiState = MutableStateFlow<FormState>(FormState.Idle)
    val uiState: StateFlow<FormState> = _uiState

    private val _empleados = MutableStateFlow<List<EmpleadoDetalle>>(emptyList())
    val empleados: StateFlow<List<EmpleadoDetalle>> = _empleados

    fun enviarInvitacionPorCorreo(
        correo: String,
        roles: List<String>,
        establecimientoId: String,
        onSent: (String) -> Unit
    ) {
        if (correo.isBlank() || roles.isEmpty()) {
            _uiState.value = FormState.Error("Por favor ingresa un correo válido")
            return
        }

        viewModelScope.launch {
            _uiState.value = FormState.Loading
            try {
                val email = correo.trim()
                val userQuery = db.collection("usuarios")
                    .where("email", equalTo = email)
                    .get()

                if (userQuery.documents.isEmpty()) {
                    _uiState.value = FormState.Error("No existe ningún usuario registrado con este correo en la app.")
                    return@launch
                }

                val userDocument = userQuery.documents.first()
                val usuarioInvitado = userDocument.data<Usuario>()
                val uidInvitado = usuarioInvitado.uid.ifEmpty { userDocument.id }
                val establecimiento = db.collection("establecimientos")
                    .document(establecimientoId)
                    .get()
                    .data<Establecimiento>()

                if (establecimiento.ownerUid == uidInvitado) {
                    _uiState.value = FormState.Error("No puedes invitar al dueño del establecimiento.")
                    return@launch
                }

                val empleadoActual = db.collection("establecimientos")
                    .document(establecimientoId)
                    .collection("empleados")
                    .document(uidInvitado)
                    .get()

                if (empleadoActual.exists) {
                    _uiState.value = FormState.Error("Este usuario ya pertenece a este establecimiento.")
                    return@launch
                }

                val establecimientosDelUsuario = db.collection("establecimientos")
                    .where("ownerUid", equalTo = uidInvitado)
                    .get()

                if (establecimientosDelUsuario.documents.isNotEmpty()) {
                    _uiState.value = FormState.Error("No puedes invitar a un usuario que ya es dueño de un establecimiento.")
                    return@launch
                }

                val owner = db.collection("usuarios")
                    .document(establecimiento.ownerUid)
                    .get()
                    .data<Usuario>()

                val now = Clock.System.now().toEpochMilliseconds()
                val token = Random.nextInt(100000, 1000000).toString()
                val invitation = InvitacionEmpleado(
                    token = token,
                    correo = email,
                    establecimientoId = establecimientoId,
                    rol = roles.joinToString(","),
                    roles = roles,
                    ownerUid = establecimiento.ownerUid,
                    ownerNombre = owner.nombre.ifEmpty { owner.email },
                    establecimientoNombre = establecimiento.nombre,
                    createdAt = now,
                    expiresAt = now + 24 * 60 * 60 * 1000L
                )

                db.collection("invitaciones_empleado")
                    .document(token)
                    .set(invitation)

                db.collection("mail").add(
                    EmployeeInvitationEmailTemplate.create(
                        token = token,
                        email = email,
                        ownerNombre = owner.nombre.ifEmpty { owner.email },
                        establecimientoNombre = establecimiento.nombre,
                        rol = roles.joinToString(", ")
                    )
                )

                _uiState.value = FormState.Success
                onSent(token)
            } catch (e: Exception) {
                _uiState.value = FormState.Error(e.message ?: "Error enviando la invitación")
            }
        }
    }

    fun cargarEmpleados(establecimientoId: String) {
        viewModelScope.launch {
            _uiState.value = FormState.Loading
            val result = repository.obtenerEmpleados(establecimientoId)

            if (result.isSuccess) {
                val listaEmpleados = result.getOrDefault(emptyList())
                val empleadosDetalle = listaEmpleados.map { empleado ->
                    async { construirDetalleEmpleado(empleado) }
                }.awaitAll()

                _empleados.value = empleadosDetalle
                _uiState.value = FormState.Idle
            } else {
                _uiState.value = FormState.Error(
                    result.exceptionOrNull()?.message ?: "Error al cargar empleados"
                )
            }
        }
    }

    fun guardarEmpleadoPorCorreo(
        correoBusqueda: String,
        roles: List<String>,
        establecimientoId: String,
        onSuccess: () -> Unit
    ) {
        if (correoBusqueda.isBlank()) {
            _uiState.value = FormState.Error("Por favor ingresa un correo válido")
            return
        }

        viewModelScope.launch {
            _uiState.value = FormState.Loading

            try {
                val userQuery = db.collection("usuarios")
                    .where("email", equalTo = correoBusqueda.trim())
                    .get()

                if (userQuery.documents.isEmpty()) {
                    _uiState.value = FormState.Error("No existe ningún usuario registrado con este correo en la app.")
                    return@launch
                }

                val firstDoc = userQuery.documents.first()
                val usuarioEncontrado = firstDoc.data<Usuario>()
                
                val uidFinal = usuarioEncontrado.uid.ifEmpty { firstDoc.id }

                val nuevoEmpleado = Empleado(
                    uid = uidFinal,
                    rol = roles.joinToString(","),
                    roles = roles,
                    activo = true
                )

                val result = repository.registrarEmpleado(establecimientoId, nuevoEmpleado)

                if (result.isSuccess) {
                    _uiState.value = FormState.Success
                    cargarEmpleados(establecimientoId)
                    onSuccess()
                } else {
                    _uiState.value = FormState.Error(
                        result.exceptionOrNull()?.message ?: "Error al vincular empleado"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = FormState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun cargarTodosLosEmpleados(establecimientoIds: List<String>) {
        viewModelScope.launch {
            _uiState.value = FormState.Loading
            val todosList = mutableListOf<EmpleadoDetalle>()

            for (estId in establecimientoIds) {
                val result = repository.obtenerEmpleados(estId)
                if (result.isSuccess) {
                    val detalles = result.getOrDefault(emptyList()).map { empleado ->
                        async { construirDetalleEmpleado(empleado) }
                    }.awaitAll()
                    todosList.addAll(detalles)
                }
            }

            // Remove duplicates by uid (employee in multiple establishments)
            _empleados.value = todosList.distinctBy { it.uid }
            _uiState.value = FormState.Idle
        }
    }

    fun eliminarEmpleado(establecimientoId: String, uid: String) {
        viewModelScope.launch {
            _uiState.value = FormState.Loading
            val result = repository.eliminarEmpleado(establecimientoId, uid)
            if (result.isSuccess) {
                _uiState.value = FormState.Success
                cargarEmpleados(establecimientoId)
            } else {
                _uiState.value = FormState.Error(result.exceptionOrNull()?.message ?: "Error al eliminar")
            }
        }
    }

    fun resetState() {
        _uiState.value = FormState.Idle
    }

    private suspend fun construirDetalleEmpleado(empleado: Empleado): EmpleadoDetalle {
        return try {
            val usuario = db.collection("usuarios").document(empleado.uid).get().data<Usuario>()
            EmpleadoDetalle(
                uid = empleado.uid,
                nombre = usuario.nombre.ifEmpty { "Usuario sin nombre" },
                correo = usuario.email,
                fotoUrl = usuario.fotoUrl,
                rol = empleado.rol,
                roles = empleado.roles.normalizarRoles(empleado.rol),
                activo = empleado.activo,
                joinedAt = empleado.joinedAt
            )
        } catch (exception: Exception) {
            EmpleadoDetalle(
                uid = empleado.uid,
                nombre = "Usuario Desconocido",
                rol = empleado.rol,
                roles = empleado.roles.normalizarRoles(empleado.rol),
                activo = empleado.activo,
                joinedAt = empleado.joinedAt
            )
        }
    }

    private fun List<String>.normalizarRoles(rol: String): List<String> {
        return if (isNotEmpty()) this else rol.split(",").map(String::trim).filter(String::isNotEmpty)
    }
}