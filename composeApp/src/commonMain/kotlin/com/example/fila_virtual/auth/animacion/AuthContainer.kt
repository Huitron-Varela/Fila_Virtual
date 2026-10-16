package com.example.fila_virtual.auth.animacion

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

import com.example.fila_virtual.auth.login.LoginScreen
import com.example.fila_virtual.auth.register.RegisterScreen
import com.example.fila_virtual.navigation.Screens
import com.example.fila_virtual.core.LocalWindowSize
import com.example.fila_virtual.core.theme.PrimaryOrange
import fila_virtual.composeapp.generated.resources.Res
import fila_virtual.composeapp.generated.resources.*

// Duración compartida para todas las animaciones (ms)
private const val ANIM_DURATION = 700

@Composable
fun AuthContainer(
    currentScreen: Screens,
    onNavigate: (Screens) -> Unit,
    onGoogleSignIn: () -> Unit = {}
) {
    val isLogin = currentScreen == Screens.Login
    val windowSize = LocalWindowSize.current

    // Altura fija del header — nunca cambia, el logo siempre ocupa este espacio
    val headerHeight = if (windowSize.isSmallScreen) 150.dp else 200.dp

    // ── Formulario: anima de 0.dp (Register, sube hasta arriba)
    //                         a headerHeight (Login, queda debajo del header) ──
    val formTopOffset by animateDpAsState(
        targetValue = if (isLogin) headerHeight else 0.dp,
        animationSpec = tween(durationMillis = ANIM_DURATION, easing = FastOutSlowInEasing),
        label = "form_slide"
    )

    // Esquinas superiores redondeadas solo cuando el form está abajo (Login)
    val cornerRadius by animateDpAsState(
        targetValue = if (isLogin) 40.dp else 0.dp,
        animationSpec = tween(durationMillis = ANIM_DURATION, easing = FastOutSlowInEasing),
        label = "corner_radius"
    )

    // ── Color del logo y texto:
    //    isLogin  → fondo detrás del logo es NARANJA  → logo en BLANCO   (t=0)
    //    isRegister → fondo detrás del logo es BLANCO → logo en NARANJA  (t=1)
    val colorProgress by animateFloatAsState(
        targetValue = if (isLogin) 0f else 1f,
        animationSpec = tween(durationMillis = ANIM_DURATION, easing = FastOutSlowInEasing),
        label = "logo_color"
    )
    val logoColor: Color = lerp(Color.White, PrimaryOrange, colorProgress)

    val logoSize    = if (windowSize.isSmallScreen) 56.dp else 72.dp
    val brandFontSp = if (windowSize.isSmallScreen) 26.sp  else 34.sp

    Box(modifier = Modifier.fillMaxSize()) {

        // ── CAPA 1 (fondo): color primario naranja ─────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
        )

        // ── CAPA 2 (formulario): Surface blanca que anima su posición ──────
        // Sube hasta 0.dp (cubre todo, incluso el área del header) en Register
        // Baja hasta headerHeight en Login
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = formTopOffset),
            shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
            color = MaterialTheme.colorScheme.background
        ) {}

        Crossfade(
            targetState = isLogin,
            animationSpec = tween(ANIM_DURATION),
            modifier = Modifier.fillMaxSize(),
            label = "form_crossfade"
        ) { showLogin ->
            if (showLogin) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeight)
                ) {
                    LoginScreen(
                        onNavigate = onNavigate,
                        onGoogleSignIn = onGoogleSignIn
                    )
                }
            } else {
                RegisterScreen(
                    onNavigate = onNavigate,
                    topContentPadding = headerHeight,
                    formOffset = formTopOffset
                )
            }
        }

        // ── CAPA 3 (siempre encima): logo + texto "AlToque" ─────────────────
        // Posición absolutamente fija. Solo cambia el COLOR (blanco ↔ naranja).
        // Al estar dibujada DESPUÉS de Surface, queda por encima de ella siempre.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight),          // altura fija, nunca se mueve
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.logoblancot),
                contentDescription = "Logo AlToque",
                modifier = Modifier.size(logoSize),
                colorFilter = ColorFilter.tint(logoColor)   // blanco ↔ naranja
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.app_name),
                color = logoColor,                           // blanco ↔ naranja
                fontSize = brandFontSp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                fontFamily = FontFamily.Cursive,
                letterSpacing = 1.sp
            )
        }
    }
}
