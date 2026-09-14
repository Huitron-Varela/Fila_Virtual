import re

file_path = "composeApp/src/commonMain/kotlin/com/example/fila_virtual/perfil/EnProcesoScreen.kt"

with open(file_path, 'r') as f:
    content = f.read()

replacements = [
    ('titulo: String = "Próximamente"', 'titulo: String? = null'),
    ('Text(titulo', 'Text(titulo ?: stringResource(Res.string.in_process_default_title)'),
    ('"Regresar"', 'stringResource(Res.string.in_process_back)'),
    ('"Página en proceso"', 'stringResource(Res.string.in_process_title)'),
    ('"Estamos trabajando en esta sección. Pronto estará disponible."', 'stringResource(Res.string.in_process_desc)'),
    ('"Volver al perfil"', 'stringResource(Res.string.in_process_btn)')
]

for old, new in replacements:
    content = content.replace(old, new)

if 'import org.jetbrains.compose.resources.stringResource' not in content:
    content = content.replace('import androidx.compose.ui.unit.dp', 
                              'import androidx.compose.ui.unit.dp\nimport org.jetbrains.compose.resources.stringResource\nimport fila_virtual.composeapp.generated.resources.*')

with open(file_path, 'w') as f:
    f.write(content)

print("EnProcesoScreen updated.")
