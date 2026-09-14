package com.example.fila_virtual.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import fila_virtual.composeapp.generated.resources.*
import com.example.fila_virtual.data.Usuario
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.core.theme.*
import com.example.fila_virtual.core.LocalWindowSize
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileComponent(
    usuario: Usuario?,
    viewModel: UserViewModel,
    onLogout: () -> Unit,
    onNavigateToEdit: () -> Unit,
    onNavigateToSecurity: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {}
) {
    val windowSize = LocalWindowSize.current

    var showLogoutSheet by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }

    val logoutSheetState = rememberModalBottomSheetState()
    val languageSheetState = rememberModalBottomSheetState()

    var selectedLanguage by remember { 
        mutableStateOf(if (com.example.fila_virtual.core.getCurrentLanguage().startsWith("en")) "English" else "Español") 
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = stringResource(Res.string.nav_profile),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = windowSize.adaptiveDp(24), bottom = windowSize.adaptiveDp(16))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = windowSize.adaptiveDp(20))
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(windowSize.adaptiveDp(24)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = windowSize.adaptiveDp(32)).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfileHeader(usuario)
                    Spacer(modifier = Modifier.height(windowSize.adaptiveDp(16)))

                    Text(
                        text = usuario?.nombre ?: "Usuario",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = windowSize.adaptiveSp(22)),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = usuario?.email ?: "email@ejemplo.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediumGray
                    )

                    Spacer(modifier = Modifier.height(windowSize.adaptiveDp(16)))

                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(windowSize.adaptiveDp(16)),
                        border = BorderStroke(1.dp, BorderGray)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = windowSize.adaptiveDp(12), vertical = windowSize.adaptiveDp(6)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(windowSize.adaptiveDp(16)))
                            Spacer(modifier = Modifier.width(windowSize.adaptiveDp(6)))
                            Text(stringResource(Res.string.profile_verified_account), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(windowSize.adaptiveDp(24)))

            ProfileSectionCard(icon = Icons.Default.PersonOutline, title = stringResource(Res.string.profile_personal_info), iconTint = MaterialTheme.colorScheme.primary) {
                ProfileOptionItem(
                    icon = Icons.Default.Edit,
                    title = stringResource(Res.string.profile_edit),
                    onClick = onNavigateToEdit
                )
            }

            ProfileSectionCard(icon = Icons.Default.Security, title = "Seguridad", iconTint = MaterialTheme.colorScheme.primary) {
                ProfileOptionItem(icon = Icons.Default.Lock, title = stringResource(Res.string.profile_security_config), onClick = onNavigateToSecurity)
            }

            ProfileSectionCard(icon = Icons.Default.MoreHoriz, title = "Otros", iconTint = MaterialTheme.colorScheme.primary) {
                ProfileOptionItem(icon = Icons.AutoMirrored.Filled.Help, title = stringResource(Res.string.profile_help_center), onClick = onNavigateToHelp)
                ProfileOptionItem(icon = Icons.Default.Description, title = stringResource(Res.string.profile_terms), onClick = onNavigateToTerms)
                ProfileOptionItem(
                    icon = Icons.Default.Translate,
                    title = stringResource(Res.string.profile_language),
                    extraText = if (selectedLanguage == "Español") "ES" else "EN",
                    onClick = { showLanguageSheet = true }
                )
            }

            Spacer(modifier = Modifier.height(windowSize.adaptiveDp(16)))

            Button(
                onClick = { showLogoutSheet = true },
                modifier = Modifier.fillMaxWidth().height(windowSize.adaptiveDp(56)),
                shape = RoundedCornerShape(windowSize.adaptiveDp(16)),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
                Spacer(modifier = Modifier.width(windowSize.adaptiveDp(8)))
                Text(stringResource(Res.string.profile_logout), color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(windowSize.adaptiveDp(40)))
        }
    }

    var isChangingLanguage by remember { mutableStateOf(false) }

    if (showLanguageSheet) {
        ModalBottomSheet(onDismissRequest = { showLanguageSheet = false }, sheetState = languageSheetState, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(windowSize.adaptiveDp(24)).padding(bottom = windowSize.adaptiveDp(32)), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(Res.string.profile_select_language), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(windowSize.adaptiveDp(24)))
                LanguageOption("🇲🇽", "Español", selectedLanguage == "Español") {
                    showLanguageSheet = false
                    if (selectedLanguage != "Español") {
                        selectedLanguage = "Español"
                        isChangingLanguage = true
                        com.example.fila_virtual.core.changeAppLanguage("es")
                    }
                }
                LanguageOption("🇺🇸", "English", selectedLanguage == "English") {
                    showLanguageSheet = false
                    if (selectedLanguage != "English") {
                        selectedLanguage = "English"
                        isChangingLanguage = true
                        com.example.fila_virtual.core.changeAppLanguage("en")
                    }
                }
                Spacer(modifier = Modifier.height(windowSize.adaptiveDp(16)))
                TextButton(onClick = { showLanguageSheet = false }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.btn_cancel), color = MediumGray) }
            }
        }
    }

    if (isChangingLanguage) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { }) {
            Box(
                modifier = Modifier
                    .size(windowSize.adaptiveDp(200))
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(windowSize.adaptiveDp(16))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(windowSize.adaptiveDp(16)))
                    Text(stringResource(Res.string.profile_changing_language), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        
        // Hide the dialog after a short delay since recomposition will happen
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            isChangingLanguage = false
        }
    }

    if (showLogoutSheet) {
        ModalBottomSheet(onDismissRequest = { showLogoutSheet = false }, sheetState = logoutSheetState, containerColor = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.fillMaxWidth().padding(windowSize.adaptiveDp(24)).padding(bottom = windowSize.adaptiveDp(32)), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(windowSize.adaptiveDp(72)).background(SoftRedBg, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(windowSize.adaptiveDp(32)))
                }
                Spacer(modifier = Modifier.height(windowSize.adaptiveDp(20)))
                Text(stringResource(Res.string.profile_logout_confirm_title), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(windowSize.adaptiveDp(32)))
                Button(onClick = { showLogoutSheet = false; onLogout() }, modifier = Modifier.fillMaxWidth().height(windowSize.adaptiveDp(54)), shape = RoundedCornerShape(windowSize.adaptiveDp(14)), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text(stringResource(Res.string.profile_logout_confirm_btn), color = MaterialTheme.colorScheme.onError, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { showLogoutSheet = false }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.btn_cancel), color = MaterialTheme.colorScheme.onSurface) }
            }
        }
    }
}

@Composable
fun ProfileSectionCard(icon: ImageVector, title: String, iconTint: Color, content: @Composable ColumnScope.() -> Unit) {
    val windowSize = LocalWindowSize.current
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = windowSize.adaptiveDp(16)), shape = RoundedCornerShape(windowSize.adaptiveDp(24)), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(windowSize.adaptiveDp(16))) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = windowSize.adaptiveDp(16))) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(windowSize.adaptiveDp(24)))
                Spacer(modifier = Modifier.width(windowSize.adaptiveDp(12)))
                Text(text = title, style = MaterialTheme.typography.titleLarge.copy(fontSize = windowSize.adaptiveSp(18)), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            content()
        }
    }
}

@Composable
fun ProfileOptionItem(icon: ImageVector, title: String, extraText: String? = null, onClick: () -> Unit = {}) {
    val windowSize = LocalWindowSize.current
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(windowSize.adaptiveDp(12))).clickable { onClick() }.padding(vertical = windowSize.adaptiveDp(12)), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(windowSize.adaptiveDp(40)).background(MaterialTheme.colorScheme.background, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = DarkGray, modifier = Modifier.size(windowSize.adaptiveDp(20)))
        }
        Spacer(modifier = Modifier.width(windowSize.adaptiveDp(16)))
        Text(text = title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge.copy(fontSize = windowSize.adaptiveSp(15)), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        if (extraText != null) Text(text = extraText, color = MediumGray, style = MaterialTheme.typography.bodyMedium.copy(fontSize = windowSize.adaptiveSp(14)), modifier = Modifier.padding(horizontal = windowSize.adaptiveDp(8)))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = BorderGray, modifier = Modifier.size(windowSize.adaptiveDp(16)))
    }
}

@Composable
fun ProfileHeader(usuario: Usuario?) {
    val windowSize = LocalWindowSize.current
    val fotoUrl = usuario?.fotoUrl
    Box(modifier = Modifier.size(windowSize.adaptiveDp(100)).clip(CircleShape).background(SoftOrangeBg), contentAlignment = Alignment.Center) {
        if (!fotoUrl.isNullOrBlank()) {
            KamelImage(resource = asyncPainterResource(fotoUrl), contentDescription = "Foto", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, onFailure = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(windowSize.adaptiveDp(50)), tint = MaterialTheme.colorScheme.primary) })
        } else {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(windowSize.adaptiveDp(50)), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun LanguageOption(flag: String, name: String, isSelected: Boolean, onSelect: () -> Unit) {
    val windowSize = LocalWindowSize.current
    Surface(modifier = Modifier.fillMaxWidth().padding(vertical = windowSize.adaptiveDp(4)).clip(RoundedCornerShape(windowSize.adaptiveDp(12))).clickable { onSelect() }, color = if (isSelected) SoftOrangeBg else Color.Transparent, border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null) {
        Row(modifier = Modifier.padding(windowSize.adaptiveDp(16)), verticalAlignment = Alignment.CenterVertically) {
            Text(flag, fontSize = windowSize.adaptiveSp(24))
            Spacer(modifier = Modifier.width(windowSize.adaptiveDp(16)))
            Text(name, style = MaterialTheme.typography.bodyLarge.copy(fontSize = windowSize.adaptiveSp(16)), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(windowSize.adaptiveDp(20)))
        }
    }
}