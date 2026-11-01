package cl.duoc.protofield.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.model.Roles
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.viewmodel.CuentaViewModel

private data class ItemMenu(val ruta: String, val texto: String, val icono: ImageVector)

/**
 * Barra inferior. Cambia según el ROL:
 *  - técnico: Visitas, Equipos
 *  - supervisor: Visitas, Equipos, Catálogo
 */
@Composable
fun BarraInferior(navController: NavController, actual: String, viewModel: CuentaViewModel = viewModel()) {
    val sesion by viewModel.sesion.collectAsState()
    val items = buildList {
        add(ItemMenu(Rutas.VISITAS, "Visitas", Icons.Filled.Event))
        add(ItemMenu(Rutas.EQUIPOS, "Equipos", Icons.Filled.ElectricalServices))
        if (sesion?.rol == Roles.SUPERVISOR) add(ItemMenu(Rutas.CATALOGO, "Catálogo", Icons.Filled.Category))
    }
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = actual == item.ruta,
                onClick = {
                    if (actual != item.ruta) {
                        navController.navigate(item.ruta) {
                            popUpTo(Rutas.VISITAS) // "atrás" siempre vuelve a Visitas
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(item.icono, contentDescription = null) },
                label = { Text(item.texto) }
            )
        }
    }
}

/** Botón de la cuenta (arriba a la derecha): muestra nombre, rol y "Cerrar sesión". */
@Composable
fun MenuCuenta(navController: NavController, viewModel: CuentaViewModel = viewModel()) {
    val sesion by viewModel.sesion.collectAsState()
    var abierto by remember { mutableStateOf(false) }
    IconButton(onClick = { abierto = true }) {
        Icon(Icons.Filled.AccountCircle, contentDescription = "Mi cuenta")
    }
    DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(sesion?.nombre.orEmpty(), style = MaterialTheme.typography.titleMedium)
            Text(Roles.nombre(sesion?.rol.orEmpty()), style = MaterialTheme.typography.bodySmall)
        }
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text("Cerrar sesión") },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            onClick = {
                abierto = false
                viewModel.cerrarSesion(onListo = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true } // borra todo el historial
                    }
                })
            }
        )
    }
}