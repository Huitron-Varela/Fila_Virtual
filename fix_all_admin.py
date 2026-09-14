import re
import glob

# Map of literal string to string resource ID
string_map = {
    '"Mis Establecimientos"': 'stringResource(Res.string.est_title_manage)',
    '"Registrar Nuevo Local"': 'stringResource(Res.string.est_title_add)',
    '"Editar Local"': 'stringResource(Res.string.est_title_edit)',
    '"No se encontraron resultados"': 'stringResource(Res.string.est_no_results)',
    '"No hay establecimientos registrados"': 'stringResource(Res.string.est_empty)',
    '"Opciones"': 'stringResource(Res.string.est_options)',
    '"Nombre del Establecimiento"': 'stringResource(Res.string.est_name)',
    '"Categoría"': 'stringResource(Res.string.est_category)',
    '"Dirección Completa"': 'stringResource(Res.string.est_address)',
    '"Horario de Operación (Ej. L-V 9am - 10pm)"': 'stringResource(Res.string.est_schedule)',
    '"Guardar Establecimiento"': 'stringResource(Res.string.est_save)',
    '"Guardar Cambios"': 'stringResource(Res.string.est_save_changes)',
    '"Guardando..."': 'stringResource(Res.string.est_saving)',
    '"todos"': 'stringResource(Res.string.est_all)',
    '"Visibilidad en la app para clientes"': 'stringResource(Res.string.est_visibility)',
    '"Sin Nombre"': 'stringResource(Res.string.est_no_name)',
    '"Sin categoría"': 'stringResource(Res.string.est_no_category)',
    '"Sin dirección"': 'stringResource(Res.string.est_no_address)',
    '"Sin horario"': 'stringResource(Res.string.est_no_schedule)',

    '"Gestión de Menú"': 'stringResource(Res.string.menu_title_manage)',
    '"Agregar Platillo"': 'stringResource(Res.string.menu_title_add)',
    '"Editar Platillo"': 'stringResource(Res.string.menu_title_edit)',
    '"No hay platillos registrados aún"': 'stringResource(Res.string.menu_empty)',
    '"Buscar platillos..."': 'stringResource(Res.string.menu_search)',
    '"Eliminar Platillo"': 'stringResource(Res.string.menu_delete_title)',
    '"¿Estás seguro de que deseas eliminar este platillo? Esta acción no se puede deshacer."': 'stringResource(Res.string.menu_delete_confirm)',
    '"Eliminar"': 'stringResource(Res.string.menu_delete_btn)',
    '"Nombre del Platillo"': 'stringResource(Res.string.menu_dish_name)',
    '"Descripción"': 'stringResource(Res.string.menu_dish_desc)',
    '"Describe los ingredientes, alérgenos y detalles especiales..."': 'stringResource(Res.string.menu_dish_desc_placeholder)',
    '"Precio"': 'stringResource(Res.string.menu_dish_price)',
    '"IMAGEN DEL PLATILLO"': 'stringResource(Res.string.menu_dish_image)',
    '"Activo en Menú"': 'stringResource(Res.string.menu_dish_active)',
    '"Visible para clientes"': 'stringResource(Res.string.menu_dish_active_desc)',
    '"Guardar Platillo"': 'stringResource(Res.string.menu_dish_save)',
    '"¡Platillo Guardado!"': 'stringResource(Res.string.menu_saved_success)',
    '"El platillo se ha agregado correctamente a tu menú y está disponible para tus clientes."': 'stringResource(Res.string.menu_saved_success_desc)',
    '"¡Platillo Actualizado!"': 'stringResource(Res.string.menu_updated_success)',
    '"Los datos del platillo se han actualizado correctamente."': 'stringResource(Res.string.menu_updated_success_desc)',
    
    '"Gestión de Personal"': 'stringResource(Res.string.emp_title_manage)',
    '"Agregar Empleado"': 'stringResource(Res.string.emp_title_add)',
    '"Editar Empleado"': 'stringResource(Res.string.emp_title_edit)',
    '"No hay empleados registrados aún"': 'stringResource(Res.string.emp_empty)',
    '"Buscar empleados..."': 'stringResource(Res.string.emp_search)',
    '"Nombre Completo"': 'stringResource(Res.string.emp_name)',
    '"Correo Electrónico"': 'stringResource(Res.string.emp_email)',
    '"Teléfono (Opcional)"': 'stringResource(Res.string.emp_phone)',
    '"Rol del Empleado"': 'stringResource(Res.string.emp_role)',
    '"Guardar Empleado"': 'stringResource(Res.string.emp_save)',
    '"Despedir Empleado"': 'stringResource(Res.string.emp_delete_title)',
    '"¿Estás seguro de que deseas eliminar este empleado? Esta acción revocará su acceso al sistema."': 'stringResource(Res.string.emp_delete_confirm)'
}

files = glob.glob("composeApp/src/commonMain/kotlin/com/example/fila_virtual/features/admin/**/*.kt", recursive=True)

for file_path in files:
    with open(file_path, 'r') as f:
        content = f.read()

    original = content
    
    # We replace exact strings
    for old, new in string_map.items():
        if old in content:
            # Check if it's inside a Text(
            # Usually we can just replace the string directly if it's an argument.
            # But let's just do a direct replacement!
            content = content.replace(old, new)
            
    if content != original:
        if 'import org.jetbrains.compose.resources.stringResource' not in content:
            content = content.replace('import androidx.compose.ui.unit.dp', 
                                      'import androidx.compose.ui.unit.dp\nimport org.jetbrains.compose.resources.stringResource\nimport fila_virtual.composeapp.generated.resources.*')
        
        with open(file_path, 'w') as f:
            f.write(content)

print("Admin screens updated.")
