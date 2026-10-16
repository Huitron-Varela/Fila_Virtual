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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fila_virtual.components.FormTextField
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.features.user.UserViewModel

@Composable
fun FormularioTarjetaScreen(
    viewModel: UserViewModel,
    onSuccess: () -> Unit
) {
    val windowSize = LocalWindowSize.current
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.clearWalletMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = windowSize.compactDp(24),
                end = windowSize.compactDp(24),
                top = 4.dp,
                bottom = 40.dp
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Agregar tarjeta",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "Ingresa los datos tal como aparecen en tu tarjeta.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MediumGray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = ExtraLightGray
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = PrimaryOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Validamos primero el formato localmente y después intentamos tokenizar la tarjeta con Mercado Pago.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        FormTextField(
            label = "Número de tarjeta",
            value = viewModel.numeroTarjeta,
            onValueChange = { valor ->
                val filtrado = valor.filter { it.isDigit() }.take(16)
                viewModel.onNumeroTarjetaChange(filtrado)
            },
            placeholder = "1234 5678 9012 3456",
            leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            visualTransformation = CardNumberVisualTransformation()
        )

        Text(
            text = "Si el número es inválido, te explicaremos exactamente por qué.",
            style = MaterialTheme.typography.labelSmall,
            color = MediumGray,
            modifier = Modifier.padding(start = 4.dp, top = 5.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        FormTextField(
            label = "Nombre del titular",
            value = viewModel.nombreTitular,
            onValueChange = { valor ->
                viewModel.onNombreTitularChange(valor.take(50))
            },
            placeholder = "Ej. Juan Pérez",
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next)
        )

        Text(
            text = "Solo letras, espacios, apóstrofes y guiones.",
            style = MaterialTheme.typography.labelSmall,
            color = MediumGray,
            modifier = Modifier.padding(start = 4.dp, top = 5.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        ExpirationAndCvvFields(viewModel = viewModel, compact = windowSize.isSmallScreen)

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
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                RoundedCornerShape(12.dp),
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

@Composable
private fun ExpirationAndCvvFields(viewModel: UserViewModel, compact: Boolean) {
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ExpirationField(viewModel)
            CvvField(viewModel)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                ExpirationField(viewModel)
            }
            Column(modifier = Modifier.weight(1f)) {
                CvvField(viewModel)
            }
        }
    }
}

@Composable
private fun ExpirationField(viewModel: UserViewModel) {
    FormTextField(
        label = "Vencimiento",
        value = viewModel.fechaExpiracion,
        onValueChange = { value ->
            viewModel.onFechaExpiracionChange(value.filter(Char::isDigit).take(4))
        },
        placeholder = "MM/AA",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        visualTransformation = ExpiryDateVisualTransformation()
    )
}

@Composable
private fun CvvField(viewModel: UserViewModel) {
    FormTextField(
        label = "CVV",
        value = viewModel.cvv,
        onValueChange = { value ->
            viewModel.onCvvChange(value.filter(Char::isDigit).take(4))
        },
        placeholder = "•••",
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        visualTransformation = PasswordVisualTransformation()
    )
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