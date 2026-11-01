package cl.duoc.protofield.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.protofield.ui.screen.CambioEstadoScreen
import cl.duoc.protofield.ui.screen.CargaScreen
import cl.duoc.protofield.ui.screen.CatalogoScreen
import cl.duoc.protofield.ui.screen.CerrarProtocoloScreen
import cl.duoc.protofield.ui.screen.DetalleEquipoScreen
import cl.duoc.protofield.ui.screen.EquipoFormScreen
import cl.duoc.protofield.ui.screen.EquiposScreen
import cl.duoc.protofield.ui.screen.EscanerScreen
import cl.duoc.protofield.ui.screen.InformeScreen
import cl.duoc.protofield.ui.screen.LoginScreen
import cl.duoc.protofield.ui.screen.NuevaVisitaScreen
import cl.duoc.protofield.ui.screen.PruebaFormScreen
import cl.duoc.protofield.ui.screen.TipoPruebaFormScreen
import cl.duoc.protofield.ui.screen.VisitaScreen
import cl.duoc.protofield.ui.screen.VisitasScreen

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {

    // Argumentos opcionales: si no vienen, valen -1 (o texto vacío)
    fun largo(nombre: String) = navArgument(nombre) { type = NavType.LongType; defaultValue = -1L }
    fun texto(nombre: String) = navArgument(nombre) { type = NavType.StringType; defaultValue = "" }
    fun NavBackStackEntry.long(nombre: String) = arguments?.getLong(nombre) ?: -1L
    fun NavBackStackEntry.string(nombre: String) = arguments?.getString(nombre).orEmpty()

    NavHost(
        navController = navController,
        startDestination = Rutas.CARGA,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable(Rutas.CARGA) { CargaScreen(navController) }

        composable(Rutas.LOGIN) { LoginScreen(navController) }                                    // 1

        composable(Rutas.VISITAS) { VisitasScreen(navController) }                                // 2

        composable(Rutas.ESCANER, arguments = listOf(largo("visitaId"))) { e ->                   // 3
            EscanerScreen(navController, visitaId = e.long("visitaId"))
        }

        composable(Rutas.VISITA, arguments = listOf(largo("visitaId"))) { e ->                    // 4
            VisitaScreen(navController, visitaId = e.long("visitaId"))
        }

        composable(Rutas.DETALLE, arguments = listOf(largo("equipoId"), largo("visitaId"))) { e -> // 5
            DetalleEquipoScreen(navController, equipoId = e.long("equipoId"), visitaId = e.long("visitaId"))
        }

        composable(Rutas.PRUEBA, arguments = listOf(largo("equipoId"), largo("visitaId"), largo("pruebaId"))) { e -> // 6
            PruebaFormScreen(navController, equipoId = e.long("equipoId"), visitaId = e.long("visitaId"), pruebaId = e.long("pruebaId"))
        }

        composable(                                                                               // 7
            Rutas.EQUIPO_FORM,
            arguments = listOf(largo("equipoId"), largo("visitaId"), texto("codigo"))
        ) { e ->
            EquipoFormScreen(navController, equipoId = e.long("equipoId"), visitaId = e.long("visitaId"), codigo = e.string("codigo"))
        }

        composable(Rutas.CERRAR, arguments = listOf(largo("visitaId"))) { e ->                    // 8
            CerrarProtocoloScreen(navController, visitaId = e.long("visitaId"))
        }

        composable(Rutas.INFORME, arguments = listOf(largo("visitaId"))) { e ->                   // 9
            InformeScreen(navController, visitaId = e.long("visitaId"))
        }

        composable(Rutas.EQUIPOS) { EquiposScreen(navController) }                                // 10

        composable(Rutas.NUEVA_VISITA, arguments = listOf(largo("equipoId"))) { e ->              // 11
            NuevaVisitaScreen(navController, equipoId = e.long("equipoId"))
        }

        composable(Rutas.CAMBIAR_ESTADO, arguments = listOf(largo("equipoId"))) { e ->            // 12
            CambioEstadoScreen(navController, equipoId = e.long("equipoId"))
        }

        composable(Rutas.CATALOGO) { CatalogoScreen(navController) }                              // 13

        composable(Rutas.TIPO_PRUEBA, arguments = listOf(largo("id"), texto("tipoEquipo"))) { e -> // 14
            TipoPruebaFormScreen(navController, id = e.long("id"), tipoEquipo = e.string("tipoEquipo"))
        }
    }
}