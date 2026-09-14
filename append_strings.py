import xml.etree.ElementTree as ET
from xml.dom import minidom

strings = {
    "profile_verified_account": {"es": "Cuenta Verificada", "en": "Verified Account"},
    "profile_edit": {"es": "Editar Perfil", "en": "Edit Profile"},
    "profile_security": {"es": "Seguridad", "en": "Security"},
    "profile_security_config": {"es": "Configuración de Seguridad", "en": "Security Configuration"},
    "profile_others": {"es": "Otros", "en": "Others"},
    "profile_help_center": {"es": "Centro de Ayuda", "en": "Help Center"},
    "profile_terms": {"es": "Términos y Condiciones", "en": "Terms and Conditions"},
    "profile_changing_language": {"es": "Cambiando idioma...", "en": "Changing language..."},
    "profile_logout_confirm_btn": {"es": "Sí, cerrar sesión", "en": "Yes, log out"},
    
    "in_process_default_title": {"es": "Próximamente", "en": "Coming Soon"},
    "in_process_back": {"es": "Regresar", "en": "Back"},
    "in_process_title": {"es": "Página en proceso", "en": "Page in process"},
    "in_process_desc": {"es": "Estamos trabajando en esta sección. Pronto estará disponible.", "en": "We are working on this section. It will be available soon."},
    "in_process_btn": {"es": "Volver al perfil", "en": "Back to profile"},
    
    "legal_understood": {"es": "Entendido", "en": "Understood"},
    
    "nav_admin_home": {"es": "Inicio", "en": "Home"},
    "nav_admin_employees": {"es": "Empleados", "en": "Employees"},
    "nav_admin_menu": {"es": "Menú", "en": "Menu"},
    
    "dashboard_title": {"es": "Panel de Control 👋", "en": "Dashboard 👋"},
    "dashboard_subtitle": {"es": "ADMIN DASHBOARD", "en": "ADMIN DASHBOARD"},
    "dashboard_manage_establishments": {"es": "Gestionar Establecimientos", "en": "Manage Establishments"},
    "dashboard_manage_desc": {"es": "ADMINISTRAR SUCURSALES Y LOCALES", "en": "MANAGE BRANCHES AND LOCATIONS"},
    "dashboard_sales_trend": {"es": "TENDENCIA DE VENTAS", "en": "SALES TREND"},
    "dashboard_weekly_performance": {"es": "Rendimiento Semanal", "en": "Weekly Performance"},
    "dashboard_this_week": {"es": "ESTA SEMANA", "en": "THIS WEEK"},
    "dashboard_top_selling": {"es": "MÁS VENDIDOS", "en": "TOP SELLING"},
    "dashboard_sales": {"es": "VENTAS", "en": "SALES"},
    "dashboard_orders": {"es": "ÓRDENES", "en": "ORDERS"},
    "dashboard_ticket": {"es": "TICKET", "en": "TICKET"},

    "est_title_manage": {"es": "Mis Establecimientos", "en": "My Establishments"},
    "est_title_add": {"es": "Registrar Nuevo Local", "en": "Register New Location"},
    "est_title_edit": {"es": "Editar Local", "en": "Edit Location"},
    "est_no_results": {"es": "No se encontraron resultados", "en": "No results found"},
    "est_empty": {"es": "No hay establecimientos registrados", "en": "No establishments registered"},
    "est_options": {"es": "Opciones", "en": "Options"},
    "est_name": {"es": "Nombre del Establecimiento", "en": "Establishment Name"},
    "est_category": {"es": "Categoría", "en": "Category"},
    "est_address": {"es": "Dirección Completa", "en": "Full Address"},
    "est_schedule": {"es": "Horario de Operación (Ej. L-V 9am - 10pm)", "en": "Operating Hours (E.g., M-F 9am - 10pm)"},
    "est_save": {"es": "Guardar Establecimiento", "en": "Save Establishment"},
    "est_save_changes": {"es": "Guardar Cambios", "en": "Save Changes"},
    "est_saving": {"es": "Guardando...", "en": "Saving..."},
    "est_all": {"es": "todos", "en": "all"},
    "est_visibility": {"es": "Visibilidad en la app para clientes", "en": "Visibility in app for clients"},
    "est_no_name": {"es": "Sin Nombre", "en": "No Name"},
    "est_no_category": {"es": "Sin categoría", "en": "No category"},
    "est_no_address": {"es": "Sin dirección", "en": "No address"},
    "est_no_schedule": {"es": "Sin horario", "en": "No schedule"},

    "menu_title_manage": {"es": "Gestión de Menú", "en": "Menu Management"},
    "menu_title_add": {"es": "Agregar Platillo", "en": "Add Dish"},
    "menu_title_edit": {"es": "Editar Platillo", "en": "Edit Dish"},
    "menu_empty": {"es": "No hay platillos registrados aún", "en": "No dishes registered yet"},
    "menu_search": {"es": "Buscar platillos...", "en": "Search dishes..."},
    "menu_delete_title": {"es": "Eliminar Platillo", "en": "Delete Dish"},
    "menu_delete_confirm": {"es": "¿Estás seguro de que deseas eliminar este platillo? Esta acción no se puede deshacer.", "en": "Are you sure you want to delete this dish? This action cannot be undone."},
    "menu_delete_btn": {"es": "Eliminar", "en": "Delete"},
    "menu_dish_name": {"es": "Nombre del Platillo", "en": "Dish Name"},
    "menu_dish_desc": {"es": "Descripción", "en": "Description"},
    "menu_dish_desc_placeholder": {"es": "Describe los ingredientes, alérgenos y detalles especiales...", "en": "Describe ingredients, allergens and special details..."},
    "menu_dish_price": {"es": "Precio", "en": "Price"},
    "menu_dish_category": {"es": "Categoría", "en": "Category"},
    "menu_dish_image": {"es": "IMAGEN DEL PLATILLO", "en": "DISH IMAGE"},
    "menu_dish_active": {"es": "Activo en Menú", "en": "Active in Menu"},
    "menu_dish_active_desc": {"es": "Visible para clientes", "en": "Visible to customers"},
    "menu_dish_save": {"es": "Guardar Platillo", "en": "Save Dish"},
    "menu_saved_success": {"es": "¡Platillo Guardado!", "en": "Dish Saved!"},
    "menu_saved_success_desc": {"es": "El platillo se ha agregado correctamente a tu menú y está disponible para tus clientes.", "en": "The dish has been successfully added to your menu and is available to your customers."},
    "menu_updated_success": {"es": "¡Platillo Actualizado!", "en": "Dish Updated!"},
    "menu_updated_success_desc": {"es": "Los datos del platillo se han actualizado correctamente.", "en": "The dish details have been successfully updated."},
    
    "emp_title_manage": {"es": "Gestión de Personal", "en": "Staff Management"},
    "emp_title_add": {"es": "Agregar Empleado", "en": "Add Employee"},
    "emp_title_edit": {"es": "Editar Empleado", "en": "Edit Employee"},
    "emp_empty": {"es": "No hay empleados registrados aún", "en": "No employees registered yet"},
    "emp_search": {"es": "Buscar empleados...", "en": "Search employees..."},
    "emp_name": {"es": "Nombre Completo", "en": "Full Name"},
    "emp_email": {"es": "Correo Electrónico", "en": "Email"},
    "emp_phone": {"es": "Teléfono (Opcional)", "en": "Phone (Optional)"},
    "emp_role": {"es": "Rol del Empleado", "en": "Employee Role"},
    "emp_save": {"es": "Guardar Empleado", "en": "Save Employee"},
    "emp_delete_title": {"es": "Despedir Empleado", "en": "Dismiss Employee"},
    "emp_delete_confirm": {"es": "¿Estás seguro de que deseas eliminar este empleado? Esta acción revocará su acceso al sistema.", "en": "Are you sure you want to remove this employee? This action will revoke their system access."}
}

paths = [
    ("composeApp/src/commonMain/composeResources/values/strings.xml", "es"),
    ("composeApp/src/commonMain/composeResources/values-en/strings.xml", "en")
]

for path, lang in paths:
    tree = ET.parse(path)
    root = tree.getroot()
    
    # Avoid duplicates
    existing_keys = [child.attrib.get('name') for child in root if child.tag == 'string']
    
    for key, vals in strings.items():
        if key not in existing_keys:
            el = ET.Element('string', name=key)
            el.text = vals[lang]
            root.append(el)
            
    xml_str = ET.tostring(root, encoding='utf-8', xml_declaration=True)
    parsed = minidom.parseString(xml_str)
    pretty = parsed.toprettyxml(indent="    ")
    
    # Remove extra blank lines generated by minidom
    clean_lines = [line for line in pretty.split('\n') if line.strip()]
    
    with open(path, "w", encoding='utf-8') as f:
        f.write('\n'.join(clean_lines))

print("Strings appended successfully.")
