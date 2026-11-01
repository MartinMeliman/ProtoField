package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.data.local.entity.EquipoConUbicacion
import cl.duoc.protofield.model.EstadosEquipo
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.ui.components.BarraInferior
import cl.duoc.protofield.ui.components.ChipEstadoEquipo
import cl.duoc.protofield.ui.components.Formato
import cl.duoc.protofield.ui.components.LineaIcono
import cl.duoc.protofield.ui.components.MenuCuenta
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.EquiposViewModel

/** PANTALLA 10 · EQUIPOS: buscador y filtro por estado. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquiposScreen(navController: NavController, viewModel: EquiposViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Equipos", "${estado.total} equipos registrados") },
                actions = {
                    IconButton(onClick = { navController.navigate(Rutas.escaner()) }) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR de un equipo")
                    }
                    MenuCuenta(navController)
                }
            )
        },
        bottomBar = { BarraInferior(navController, actual = Rutas.EQUIPOS) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Rutas.equipoNuevo()) },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo equipo") }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = estado.busqueda,
                onValueChange = viewModel::onBusquedaChange,
                label = { Text("Buscar por código, cliente o instalación") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            // Chips de filtro por estado (con la cantidad de cada uno)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EstadosEquipo.todos.forEach { e ->
                    FilterChip(
                        selected = estado.filtro == e,
                        onClick = { viewModel.onFiltroChange(e) },
                        label = { Text("$e (${estado.conteo[e] ?: 0})") },
                        leadingIcon = if (estado.filtro == e) { { Icon(Icons.Filled.Check, contentDescription = null) } } else null
                    )
                }
            }
            if (estado.equipos.isEmpty()) {
                Text("No hay equipos con ese filtro.", modifier = Modifier.padding(top = 16.dp))
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 96.dp)) {
                items(estado.equipos, key = { it.equipo.id }) { e ->
                    TarjetaEquipo(e, onClick = { navController.navigate(Rutas.detalle(e.equipo.id)) }, modifier = Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun TarjetaEquipo(datos: EquipoConUbicacion, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(datos.equipo.codigo, style = MaterialTheme.typography.titleMedium)
                ChipEstadoEquipo(datos.equipo.estado)
            }
            Text(datos.equipo.tipo, style = MaterialTheme.typography.bodyMedium)
            LineaIcono(Icons.Filled.Place, "${datos.clienteNombre} · ${datos.instalacionNombre}")
            val fecha = datos.ultimaFecha
            when {
                fecha == null -> LineaIcono(Icons.Filled.Schedule, "Sin pruebas")
                datos.ultimaAprobada == true -> LineaIcono(Icons.Filled.CheckCircle, "Última prueba ${Formato.fecha(fecha)}: aprobada")
                else -> LineaIcono(Icons.Filled.Warning, "Última prueba ${Formato.fecha(fecha)}: rechazada")
            }
        }
    }
}
