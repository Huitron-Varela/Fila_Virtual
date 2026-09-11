package com.example.fila_virtual.features.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fila_virtual.components.BottomNavigationBar
import com.example.fila_virtual.components.NavigationDefaults
import com.example.fila_virtual.data.Establecimiento
import com.example.fila_virtual.data.EmpleadoDetalle
import com.example.fila_virtual.data.Producto
import com.example.fila_virtual.features.admin.empleados.AddEmployeeScreen
import com.example.fila_virtual.features.admin.empleados.EmployeesScreen
import com.example.fila_virtual.features.admin.inicio.AddEstablishmentScreen
import com.example.fila_virtual.features.admin.inicio.EstablishmentsScreen
import com.example.fila_virtual.features.admin.inicio.AdminDashboardScreen
import com.example.fila_virtual.features.admin.menu.AddDishScreen
import com.example.fila_virtual.features.admin.menu.MenuScreen
import com.example.fila_virtual.perfil.EditProfileScreen
import com.example.fila_virtual.perfil.EnProcesoScreen
import com.example.fila_virtual.core.LegalConstants
import com.example.fila_virtual.core.navigation.LegalScreen
import com.example.fila_virtual.features.user.UserViewModel
import com.example.fila_virtual.perfil.ProfileComponent
import kotlinx.coroutines.launch

@Composable
fun AdminMainScreen(
    viewModel: UserViewModel,
    onLogout: () -> Unit
) {
    val usuario = viewModel.usuario ?: return
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 4 })

    var isEditingProfile by remember { mutableStateOf(false) }
    var isAddingDish by remember { mutableStateOf(false) }
    var isAddingEmployee by remember { mutableStateOf(false) }
    var isManagingEstablecimientos by remember { mutableStateOf(false) }
    var isAddingEstablecimiento by remember { mutableStateOf(false) }
    var showSecurity by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    
    var selectedEstablecimientoId by remember { mutableStateOf("") }
    
    var productoToEdit by remember { mutableStateOf<Producto?>(null) }
    var empleadoToEdit by remember { mutableStateOf<EmpleadoDetalle?>(null) }
    var establecimientoToEdit by remember { mutableStateOf<Establecimiento?>(null) }

    if (isAddingDish) {
        AddDishScreen(
            establecimientoId = selectedEstablecimientoId,
            ownerUid = usuario.uid,
            productoToEdit = productoToEdit,
            onBack = { 
                isAddingDish = false
                productoToEdit = null
            }
        )
    } else if (isAddingEmployee) {
        AddEmployeeScreen(
            empleado = empleadoToEdit,
            establecimientoId = selectedEstablecimientoId,
            ownerUid = usuario.uid,
            viewModel = viewModel(),
            onNavigateBack = {
                isAddingEmployee = false
                empleadoToEdit = null
            }
        )
    } else if (isEditingProfile) {
        EditProfileScreen(
            usuario = usuario,
            viewModel = viewModel,
            onBack = { isEditingProfile = false }
        )
    } else if (isAddingEstablecimiento) {
        AddEstablishmentScreen(
            ownerUid = usuario.uid,
            establecimientoToEdit = establecimientoToEdit,
            onBack = { 
                isAddingEstablecimiento = false
                establecimientoToEdit = null
            }
        )
    } else if (isManagingEstablecimientos) {
        EstablishmentsScreen(
            currentAdminUid = usuario.uid,
            onBack = { isManagingEstablecimientos = false },
            onSelectEstablecimiento = { id ->
                selectedEstablecimientoId = id
                isManagingEstablecimientos = false
                scope.launch { pagerState.animateScrollToPage(2) }
            },
            onRegisterNew = {
                isAddingEstablecimiento = true
            },
            onEditEstablecimiento = { est ->
                establecimientoToEdit = est
                isAddingEstablecimiento = true
            }
        )
    } else if (showSecurity) {
        EnProcesoScreen(
            titulo = "Configuración de Seguridad",
            onBack = { showSecurity = false }
        )
    } else if (showHelp) {
        EnProcesoScreen(
            titulo = "Centro de Ayuda",
            onBack = { showHelp = false }
        )
    } else if (showTerms) {
        LegalScreen(
            title = "Términos y Condiciones",
            content = LegalConstants.TERMINOS_Y_CONDICIONES,
            onBack = { showTerms = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    items = NavigationDefaults.adminItems(),
                    selectedIndex = pagerState.currentPage,
                    onItemSelected = { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                    }
                )
            }
        ) { padding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.padding(padding)
            ) { page ->
                when (page) {
                    0 -> AdminDashboardScreen(
                        onNavigateToManage = { isManagingEstablecimientos = true }
                    )
                    1 -> EmployeesScreen(
                        establecimientoId = selectedEstablecimientoId,
                        ownerUid = usuario.uid,
                        onNavigateToAdd = { establecimientoId ->
                            selectedEstablecimientoId = establecimientoId
                            isAddingEmployee = true
                        },
                        onEditEmpleado = { emp ->
                            empleadoToEdit = emp
                            isAddingEmployee = true
                        }
                    )
                    2 -> MenuScreen(
                        establecimientoId = selectedEstablecimientoId,
                        ownerUid = usuario.uid,
                        onNavigateToAdd = {
                            isAddingDish = true
                        },
                        onNavigateToEdit = { prod ->
                            productoToEdit = prod
                            isAddingDish = true
                        }
                    )
                    3 -> ProfileComponent(
                        usuario = usuario,
                        viewModel = viewModel,
                        onLogout = { viewModel.signOut(onLogout) },
                        onNavigateToEdit = { isEditingProfile = true },
                        onNavigateToSecurity = { showSecurity = true },
                        onNavigateToHelp = { showHelp = true },
                        onNavigateToTerms = { showTerms = true }
                    )
                }
            }
        }
    }
}
