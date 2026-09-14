package com.example.fila_virtual.features.user.billetera

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.core.theme.DarkGray
import com.example.fila_virtual.core.theme.MediumGray
import com.example.fila_virtual.core.theme.PrimaryOrange
import com.example.fila_virtual.features.user.UserViewModel

@Composable
fun FormularioTarjetaScreen(
    viewModel: UserViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.clearWalletMessage()
    }

    val numeroVisible =
        formatearNumeroTarjeta(
            viewModel.numeroTarjeta
        )

    val fechaVisible =
        formatearFecha(
            viewModel.fechaExpiracion
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 4.dp,
                bottom = 40.dp
            )
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.clearWalletMessage()
                    onBack()
                }
            ) {
                Icon(
                    imageVector =
                        Icons.Default.ArrowBack,
                    contentDescription =
                        "Regresar"
                )
            }

            Spacer(
                modifier =
                    Modifier.width(4.dp)
            )

            Column {
                Text(
                    text =
                        "Agregar tarjeta",
                    style =
                        MaterialTheme.typography.titleLarge,
                    fontWeight =
                        FontWeight.Bold
                )

                Text(
                    text =
                        "Ingresa los datos tal como aparecen en tu tarjeta.",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        MediumGray
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Surface(
            modifier =
                Modifier.fillMaxWidth(),
            shape =
                RoundedCornerShape(14.dp),
            color =
                Color(0xFFF4F8FF)
        ) {
            Row(
                modifier =
                    Modifier.padding(14.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Lock,
                    contentDescription =
                        null,
                    tint =
                        Color(0xFF1976D2),
                    modifier =
                        Modifier.size(20.dp)
                )

                Spacer(
                    modifier =
                        Modifier.width(10.dp)
                )

                Text(
                    text =
                        "Validamos primero el formato localmente y después intentamos tokenizar la tarjeta con Mercado Pago.",
                    style =
                        MaterialTheme.typography.bodySmall,
                    color =
                        DarkGray
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Text(
            text =
                "Número de tarjeta",
            fontWeight =
                FontWeight.SemiBold,
            color =
                DarkGray
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        OutlinedTextField(
            value =
                numeroVisible,
            onValueChange = { valor ->
                viewModel.onNumeroTarjetaChange(
                    valor
                )
            },
            modifier =
                Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "1234 5678 9012 3456"
                )
            },
            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Default.CreditCard,
                    contentDescription =
                        null
                )
            },
            singleLine =
                true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Number,
                    imeAction =
                        ImeAction.Next
                ),
            shape =
                RoundedCornerShape(14.dp)
        )

        Text(
            text =
                "Si el número es inválido, te explicaremos exactamente por qué.",
            style =
                MaterialTheme.typography.labelSmall,
            color =
                MediumGray,
            modifier =
                Modifier.padding(
                    start = 4.dp,
                    top = 5.dp
                )
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Text(
            text =
                "Nombre del titular",
            fontWeight =
                FontWeight.SemiBold,
            color =
                DarkGray
        )

        Spacer(
            modifier =
                Modifier.height(8.dp)
        )

        OutlinedTextField(
            value =
                viewModel.nombreTitular,
            onValueChange = { valor ->
                viewModel.onNombreTitularChange(
                    valor
                )
            },
            modifier =
                Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "Ej. Juan Pérez"
                )
            },
            leadingIcon = {
                Icon(
                    imageVector =
                        Icons.Default.Person,
                    contentDescription =
                        null
                )
            },
            singleLine =
                true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Text,
                    imeAction =
                        ImeAction.Next
                ),
            shape =
                RoundedCornerShape(14.dp)
        )

        Text(
            text =
                "Solo letras, espacios, apóstrofes y guiones.",
            style =
                MaterialTheme.typography.labelSmall,
            color =
                MediumGray,
            modifier =
                Modifier.padding(
                    start = 4.dp,
                    top = 5.dp
                )
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        "Vencimiento",
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        DarkGray
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value =
                        fechaVisible,
                    onValueChange = { valor ->
                        viewModel.onFechaExpiracionChange(
                            valor
                        )
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("MM/AA")
                    },
                    singleLine =
                        true,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number,
                            imeAction =
                                ImeAction.Next
                        ),
                    shape =
                        RoundedCornerShape(14.dp)
                )
            }

            Column(
                modifier =
                    Modifier.weight(1f)
            ) {
                Text(
                    text =
                        "CVV",
                    fontWeight =
                        FontWeight.SemiBold,
                    color =
                        DarkGray
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                OutlinedTextField(
                    value =
                        viewModel.cvv,
                    onValueChange = { valor ->
                        viewModel.onCvvChange(
                            valor
                        )
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("•••")
                    },
                    singleLine =
                        true,
                    visualTransformation =
                        PasswordVisualTransformation(),
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number,
                            imeAction =
                                ImeAction.Done
                        ),
                    shape =
                        RoundedCornerShape(14.dp)
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(22.dp)
        )

        viewModel.walletMessage
            ?.let { mensaje ->

                val esError =
                    viewModel.walletMessageIsError

                Surface(
                    modifier =
                        Modifier.fillMaxWidth(),
                    shape =
                        RoundedCornerShape(14.dp),
                    color =
                        if (esError) {
                            MaterialTheme
                                .colorScheme
                                .errorContainer
                        } else {
                            Color(0xFFE8F5E9)
                        }
                ) {
                    Row(
                        modifier =
                            Modifier.padding(14.dp),
                        verticalAlignment =
                            Alignment.Top
                    ) {
                        Icon(
                            imageVector =
                                if (esError) {
                                    Icons.Default.Warning
                                } else {
                                    Icons.Default.CheckCircle
                                },
                            contentDescription =
                                null,
                            tint =
                                if (esError) {
                                    MaterialTheme
                                        .colorScheme
                                        .error
                                } else {
                                    Color(0xFF2E7D32)
                                }
                        )

                        Spacer(
                            modifier =
                                Modifier.width(10.dp)
                        )

                        Column {
                            Text(
                                text =
                                    if (esError) {
                                        "Revisa los datos"
                                    } else {
                                        "Validación correcta"
                                    },
                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(3.dp)
                            )

                            Text(
                                text =
                                    mensaje,
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(18.dp)
                )
            }

        Button(
            onClick = {
                viewModel.procesarPagoSeguro(
                    onSuccess = {
                        onSuccess()
                    }
                )
            },
            enabled =
                !viewModel.isLoading,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape =
                RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        PrimaryOrange
                )
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    color =
                        Color.White,
                    strokeWidth =
                        2.dp,
                    modifier =
                        Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector =
                        Icons.Default.Lock,
                    contentDescription =
                        null,
                    tint =
                        Color.White
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    text =
                        "Validar y vincular tarjeta",
                    color =
                        Color.White,
                    fontWeight =
                        FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Text(
            text =
                "Esta acción valida y tokeniza el método de pago. No realiza ningún cobro.",
            style =
                MaterialTheme.typography.labelSmall,
            color =
                MediumGray
        )

        Spacer(
            modifier =
                Modifier.height(20.dp)
        )
    }
}

private fun formatearNumeroTarjeta(
    numero: String
): String {
    return numero
        .chunked(4)
        .joinToString(" ")
}

private fun formatearFecha(
    fecha: String
): String {
    return if (fecha.length <= 2) {
        fecha
    } else {
        fecha.take(2) +
                "/" +
                fecha.drop(2)
    }
}