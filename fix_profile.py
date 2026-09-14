import re

file_path = "composeApp/src/commonMain/kotlin/com/example/fila_virtual/perfil/ProfileComponent.kt"

with open(file_path, 'r') as f:
    content = f.read()

replacements = [
    ('"Perfil"', 'stringResource(Res.string.nav_profile)'),
    ('"Cuenta Verificada"', 'stringResource(Res.string.profile_verified_account)'),
    ('"Información Personal"', 'stringResource(Res.string.profile_personal_info)'),
    ('"Editar Perfil"', 'stringResource(Res.string.profile_edit)'),
    ('Text("Seguridad"', 'Text(stringResource(Res.string.profile_security)'),
    ('"Configuración de Seguridad"', 'stringResource(Res.string.profile_security_config)'),
    ('Text("Otros"', 'Text(stringResource(Res.string.profile_others)'),
    ('"Centro de Ayuda"', 'stringResource(Res.string.profile_help_center)'),
    ('"Términos y Condiciones"', 'stringResource(Res.string.profile_terms)'),
    ('"Idioma"', 'stringResource(Res.string.profile_language)'),
    ('"Cerrar Sesión"', 'stringResource(Res.string.profile_logout)'),
    ('"Seleccionar idioma"', 'stringResource(Res.string.profile_select_language)'),
    ('Text("Cancelar"', 'Text(stringResource(Res.string.btn_cancel)'),
    ('"¿Cerrar sesión?"', 'stringResource(Res.string.profile_logout_confirm_title)'),
    ('"Sí, cerrar sesión"', 'stringResource(Res.string.profile_logout_confirm_btn)'),
    ('"Cambiando idioma..."', 'stringResource(Res.string.profile_changing_language)'),
    ('Text("Cerrar Sesión"', 'Text(stringResource(Res.string.profile_logout)')
]

for old, new in replacements:
    content = content.replace(old, new)

# Add imports
if 'import org.jetbrains.compose.resources.stringResource' not in content:
    content = content.replace('import androidx.compose.ui.unit.dp', 
                              'import androidx.compose.ui.unit.dp\nimport org.jetbrains.compose.resources.stringResource\nimport fila_virtual.composeapp.generated.resources.*')

with open(file_path, 'w') as f:
    f.write(content)

print("ProfileComponent updated.")
