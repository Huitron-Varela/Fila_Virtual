package com.example.fila_virtual.features.empleados

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.LightSurface
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.core.theme.TrafficRed
import com.example.fila_virtual.repository.EmpleadoRepository
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.emp_code_button
import fila_virtual.composeapp.generated.resources.emp_code_help
import fila_virtual.composeapp.generated.resources.emp_code_hint
import fila_virtual.composeapp.generated.resources.emp_code_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun CodigoInvitacionCard(
    onAceptada: () -> Unit,
    modifier: Modifier = Modifier
) {
    var codigo by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LightSurface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                stringResource(Res.string.emp_code_title),
                fontWeight = FontWeight.Bold,
                color = DarkGray,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(Res.string.emp_code_help),
                color = MediumGray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it.filter { caracter -> caracter.isDigit() }.take(6) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text(stringResource(Res.string.emp_code_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            if (error.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(error, color = TrafficRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        enviando = true
                        error = ""
                        val resultado = EmpleadoRepository().aceptarInvitacion(codigo)
                        enviando = false
                        if (resultado.isSuccess) onAceptada()
                        else error = resultado.exceptionOrNull()?.message ?: "No se pudo aceptar la invitación."
                    }
                },
                enabled = !enviando && codigo.length == 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                Text(stringResource(Res.string.emp_code_button), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
