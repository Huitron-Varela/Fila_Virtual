package com.example.fila_virtual.features.empleados

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.data.Empleado
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.data.InvitacionEmpleado
import com.example.fila_virtual.repository.EmpleadoRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.datetime.Clock
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@Composable
fun AceptarInvitacionScreen(
    token: String,
    onAccepted: () -> Unit,
    onCancel: () -> Unit
) {
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser
    var invitation by remember { mutableStateOf<InvitacionEmpleado?>(null) }
    var state by remember { mutableStateOf("Cargando invitación...") }
    var isLoading by remember { mutableStateOf(true) }
    var isAccepting by remember { mutableStateOf(false) }

    LaunchedEffect(token, user?.uid) {
        isLoading = true
        try {
            if (user == null) {
                state = "Debes iniciar sesión con la cuenta que recibió la invitación."
                return@LaunchedEffect
            }

            val document = db.collection("invitaciones_empleado").document(token).get()
            if (!document.exists) {
                state = "La invitación no existe o ya no está disponible."
                return@LaunchedEffect
            }

            val loadedInvitation = document.data<InvitacionEmpleado>()
            val now = Clock.System.now().toEpochMilliseconds()
            when {
                loadedInvitation.status != "pending" -> state = "Esta invitación ya fue utilizada."
                loadedInvitation.expiresAt < now -> state = "Esta invitación ha expirado."
                loadedInvitation.correo != user.email -> state = "Inicia sesión con el correo que recibió la invitación."
                loadedInvitation.ownerUid == user.uid -> state = "El dueño no puede aceptar su propia invitación."
                else -> {
                    val employeeDocument = db.collection("establecimientos")
                        .document(loadedInvitation.establecimientoId)
                        .collection("empleados")
                        .document(user.uid)
                        .get()
                    if (employeeDocument.exists) {
                        state = "Ya perteneces a este establecimiento."
                    } else {
                        invitation = loadedInvitation
                        state = "¿Quieres aceptar esta invitación?"
                    }
                }
            }
        } catch (exception: Exception) {
            state = exception.message ?: "No se pudo cargar la invitación."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(LightBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InvitationTopBar(onBack = onCancel)

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryOrange)
            }
        } else {
            invitation?.let { currentInvitation ->
                InvitationHeader(currentInvitation.ownerNombre)
                InvitationCard(currentInvitation)

                Surface(
                    color = Color(0xFFEFF6FF),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = ActionBlue,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Se vinculará tu cuenta actual:", color = MediumGray, style = MaterialTheme.typography.bodySmall)
                            Text(Firebase.auth.currentUser?.email.orEmpty(), color = DarkGray, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))
                Text(
                    text = if (isAccepting) "Procesando invitación..." else state,
                    color = if (state.startsWith("¿")) MediumGray else TrafficRed,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Button(
                    onClick = {
                        isAccepting = true
                        acceptInvitation(token, currentInvitation, onAccepted) {
                            isAccepting = false
                            state = it
                        }
                    },
                    enabled = !isAccepting,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    androidx.compose.material3.Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Aceptar e ingresar", fontWeight = FontWeight.Bold)
                }
                Text(
                    "Rechazar invitación",
                    color = MediumGray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(vertical = 18.dp)
                )
            } ?: ErrorInvitationState(state)
        }
    }
}

@Composable
private fun InvitationTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(64.dp).background(LightSurface).padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.IconButton(onClick = onBack) {
            androidx.compose.material3.Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = DarkGray)
        }
        Text(
            "Invitación de equipo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun InvitationHeader(ownerName: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 18.dp, bottom = 16.dp)) {
        Box(
            modifier = Modifier.size(58.dp).background(SoftOrangeBg, CircleShape).border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(Icons.Default.Groups, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(32.dp))
        }
        Text(
            ownerName.uppercase(),
            color = PrimaryOrange,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 12.dp).background(SoftOrangeBg, RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 5.dp)
        )
        Text("¡Te invitaron al equipo!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        Text("Has recibido una invitación para unirte como colaborador en AlToque.", color = MediumGray, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 48.dp, vertical = 4.dp))
    }
}

@Composable
private fun InvitationCard(invitation: InvitacionEmpleado) {
    Surface(
        color = LightSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).background(PrimaryOrange, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                    Text(invitation.establecimientoNombre.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(invitation.establecimientoNombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(Icons.Default.LocationOn, contentDescription = null, tint = MediumGray, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("Sucursal Principal", color = MediumGray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(color = ExtraLightGray, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Invitado por ${invitation.ownerNombre}", color = DarkGray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.height(18.dp))
            Text("TE UNES COMO", color = MediumGray, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Surface(color = SoftOrangeBg, shape = RoundedCornerShape(16.dp), modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)) {
                Text(
                    invitation.roles.ifEmpty { invitation.rol.split(",").map { it.trim() } }
                        .joinToString(" • ") { it.replaceFirstChar { character -> character.uppercase() } },
                    color = PrimaryOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorInvitationState(message: String) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message, color = TrafficRed, textAlign = TextAlign.Center)
    }
}

private fun acceptInvitation(
    token: String,
    invitation: InvitacionEmpleado,
    onAccepted: () -> Unit,
    onError: (String) -> Unit
) {
    val user = Firebase.auth.currentUser ?: run {
        onError("Debes iniciar sesión para aceptar la invitación.")
        return
    }

    MainScope().launch {
        try {
            val now = Clock.System.now().toEpochMilliseconds()
            val employee = Empleado(
                uid = user.uid,
                rol = invitation.roles.ifEmpty { invitation.rol.split(",").map { it.trim() } }.joinToString(","),
                roles = invitation.roles.ifEmpty { invitation.rol.split(",").map { it.trim() } },
                activo = true,
                invitacionToken = token,
                joinedAt = now,
                updatedAt = now
            )
            EmpleadoRepository().registrarEmpleado(invitation.establecimientoId, employee)
                .getOrThrow()
            Firebase.firestore.collection("invitaciones_empleado")
                .document(token)
                .update(
                    "status" to "accepted",
                    "acceptedBy" to user.uid,
                    "acceptedAt" to now
                )
            Firebase.firestore.collection("usuarios")
                .document(user.uid)
                .update("rolGlobal" to "empleado", "updatedAt" to now)
            onAccepted()
        } catch (exception: Exception) {
            onError(exception.message ?: "No se pudo aceptar la invitación.")
        }
    }
}
