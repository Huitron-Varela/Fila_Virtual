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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
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
                viewModel.numeroTarjeta,
            onValueChange = { valor ->
                // 🔥 FILTRO: Solo números, máximo 16 dígitos
                val filtrado = valor.filter { it.isDigit() }.take(16)
                viewModel.onNumeroTarjetaChange(filtrado)
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
            visualTransformation =
                CardNumberVisualTransformation(),
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
                // 🔥 FILTRO: Máximo 50 caracteres para el nombre
                viewModel.onNombreTitularChange(valor.take(50))
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
                        viewModel.fechaExpiracion,
                    onValueChange = { valor ->
                        // 🔥 FILTRO: Solo números, máximo 4 dígitos (MMAA)
                        val filtrado = valor.filter { it.isDigit() }.take(4)
                        viewModel.onFechaExpiracionChange(filtrado)
                    },
                    modifier =
                        Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("MM/AA")
                    },
                    singleLine =
                        true,
                    visualTransformation =
                        ExpiryDateVisualTransformation(),
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
                        // 🔥 FILTRO: Solo números, máximo 4 dígitos
                        val filtrado = valor.filter { it.isDigit() }.take(4)
                        viewModel.onCvvChange(filtrado)
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

/*
 * ====================================================================
 * TRANSFORMACIONES VISUALES (A PRUEBA DE CRASHES)
 * ====================================================================
 */

class CardNumberVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text
        val formatted = buildString {
            for (i in input.indices) {
                append(input[i])
                // Solo agrega espacio si NO es el último digito tecleado
                if ((i + 1) % 4 == 0 && i != 15 && i != input.lastIndex) {
                    append(" ")
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 8) return offset + 1
                if (offset <= 12) return offset + 2
                if (offset <= 16) return offset + 3
                return 19
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 4) return offset
                if (offset <= 9) return offset - 1
                if (offset <= 14) return offset - 2
                if (offset <= 19) return offset - 3
                return 16
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

class ExpiryDateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val input = text.text
        val formatted = buildString {
            for (i in input.indices) {
                append(input[i])
                // Solo agrega la diagonal si ya pasaste del mes y NO es el último digito tecleado
                if (i == 1 && i != input.lastIndex) {
                    append("/")
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 4) return offset + 1
                return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}