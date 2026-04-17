// ─────────────────────────────────────────────────────────────────────────────
// ARCHIVO 1: NavegacionGraph.kt  (reemplaza el existente)
// Agrega la ruta Screen.EditarMenu para que el admin pueda navegar al editor
// ─────────────────────────────────────────────────────────────────────────────
package com.example.viagourmet.Presentacion.navegacion

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.viagourmet.Presentacion.screens.admin.AdminPedidosScreen
import com.example.viagourmet.Presentacion.screens.admin.EditarMenuScreen          // ← NUEVO
import com.example.viagourmet.Presentacion.screens.cocinero.CocinerosScreen
import com.example.viagourmet.Presentacion.screens.cuenta.CuentaScreen
import com.example.viagourmet.Presentacion.screens.login.LoginScreen
import com.example.viagourmet.Presentacion.screens.menu.MenuScreen
import com.example.viagourmet.Presentacion.screens.menu.ProductoDetalleScreen
import com.example.viagourmet.Presentacion.screens.menu.ProductoDetalleViewModel
import com.example.viagourmet.Presentacion.screens.mipedido.MiPedidoScreen
import com.example.viagourmet.Presentacion.screens.modulolibre.ModuloLibreScreen
import com.example.viagourmet.Presentacion.screens.registro.RegistroScreen
import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.SessionManager

sealed class Screen(val route: String) {
    object Login           : Screen("login")
    object Registro        : Screen("registro")
    object Menu            : Screen("menu")
    object Cuenta          : Screen("cuenta")
    object Admin           : Screen("admin")
    object Cocinero        : Screen("cocinero")
    object MiPedido        : Screen("mi_pedido")
    object ModuloLibre     : Screen("modulo_libre")
    object EditarMenu      : Screen("editar_menu")                  // ← NUEVO
    object ProductoDetalle : Screen("producto/{productoId}") {
        fun createRoute(productoId: Int) = "producto/$productoId"
    }
}

@Composable
fun NavegacionGraph(sessionManager: SessionManager) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { rol ->
                    val dest = when (rol) {
                        RolUsuario.CLIENTE   -> Screen.Menu.route
                        RolUsuario.COCINERO  -> Screen.Cocinero.route
                        RolUsuario.ADMIN,
                        RolUsuario.EMPLEADO  -> Screen.Admin.route
                    }
                    navController.navigate(dest) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegistro = { navController.navigate(Screen.Registro.route) }
            )
        }

        composable(Screen.Registro.route) {
            RegistroScreen(
                onRegistroExitoso = { rol ->
                    val dest = when (rol) {
                        RolUsuario.CLIENTE   -> Screen.Menu.route
                        RolUsuario.COCINERO  -> Screen.Cocinero.route
                        RolUsuario.ADMIN,
                        RolUsuario.EMPLEADO  -> Screen.Admin.route
                    }
                    navController.navigate(dest) {
                        popUpTo(Screen.Registro.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Screen.Menu.route) {
            MenuScreen(
                onNavigateToDetalle = { productoId ->
                    navController.navigate(Screen.ProductoDetalle.createRoute(productoId))
                },
                onNavigateToCuenta = { navController.navigate(Screen.Cuenta.route) },
                onNavigateToMiPedido = { navController.navigate(Screen.MiPedido.route) },
                onNavigateToModuloLibre = { navController.navigate(Screen.ModuloLibre.route) },
                onCerrarSesion = {
                    sessionManager.cerrarSesion()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Menu.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.ProductoDetalle.route,
            arguments = listOf(navArgument("productoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0
            val viewModel: ProductoDetalleViewModel = hiltViewModel()
            ProductoDetalleScreen(
                productoId = productoId,
                onNavigateBack = { navController.popBackStack() },
                onAgregarAlPedido = { producto, cantidad ->
                    viewModel.agregarAlCarrito(producto, cantidad)
                }
            )
        }

        composable(Screen.Cuenta.route) {
            CuentaScreen(
                onNavigateBack = { navController.popBackStack() },
                onSeguirComprando = { navController.popBackStack() },
                onVerEstadoPedido = { _ ->
                    navController.navigate(Screen.MiPedido.route) {
                        popUpTo(Screen.Menu.route)
                    }
                }
            )
        }

        composable(Screen.MiPedido.route) {
            MiPedidoScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.ModuloLibre.route) {
            ModuloLibreScreen(
                onNavigateBack = { navController.popBackStack() },
                onPedidoEnviado = {
                    navController.navigate(Screen.MiPedido.route) {
                        popUpTo(Screen.Menu.route)
                    }
                }
            )
        }

        composable(Screen.Admin.route) {
            AdminPedidosScreen(
                onCerrarSesion = {
                    sessionManager.cerrarSesion()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Admin.route) { inclusive = true }
                    }
                },
                // ← NUEVO: botón en AdminPedidosScreen para ir al editor de menú
                onEditarMenu = { navController.navigate(Screen.EditarMenu.route) }
            )
        }

        composable(Screen.Cocinero.route) {
            CocinerosScreen(
                onCerrarSesion = {
                    sessionManager.cerrarSesion()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Cocinero.route) { inclusive = true }
                    }
                }
            )
        }

        // ── NUEVA RUTA ───────────────────────────────────────────────────────
        composable(Screen.EditarMenu.route) {
            EditarMenuScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}