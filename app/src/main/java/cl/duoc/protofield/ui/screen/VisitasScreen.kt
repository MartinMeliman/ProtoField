package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import cl.duoc.protofield.data.local.entity.VisitaResumen
import cl.duoc.protofield.model.Roles
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.ui.components.BarraInferior
import cl.duoc.protofield.ui.components.Formato
import cl.duoc.protofield.ui.components.MenuCuenta
import cl.duoc.protofield.ui.components.TarjetaVisita
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.VisitasViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitasScreen(navController: NavController, viewModel: VisitasViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val esSupervisor = estado.rol == Roles.SUPERVISOR

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TituloBarra(
                        titulo = if (esSupervisor) "Visitas" else "Mis visitas",
                        subtitulo = if (esSupervisor) "Todas las visitas del equipo técnico" else "Hola, ${estado.nombre.substringBefore(' ')}"
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Rutas.escaner()) }) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR de un equipo")
                    }
                    MenuCuenta(navController)
                }
            )
        },
        bottomBar = { BarraInferior(navController, actual = Rutas.VISITAS) },
        floatingActionButton = {
            if (esSupervisor) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Rutas.nuevaVisita()) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva visita") }
                )
            }
        }
    ) { padding ->
        if (estado.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
        ) {
            if (!esSupervisor) {
                item {
                    // Recordatorio: la app funciona sin internet (caso, punto 3.3)
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.CloudOff, contentDescription = null)
                            Text("Todo lo que registres se guarda en el teléfono, aunque no haya señal.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            seccion(if (esSupervisor) "Hoy" else "Hoy · ${Formato.diaCorto(System.currentTimeMillis())}", estado.hoy, esSupervisor, false, navController, "No hay visitas para hoy.")
            seccion("Próximas", estado.proximas, esSupervisor, true, navController, "Sin visitas programadas.")
            seccion("Protocolos cerrados", estado.cerradas, esSupervisor, true, navController, "Todavía no hay protocolos cerrados.")
        }
    }
}

/** Título + lista de tarjetas. animateItem = las tarjetas se deslizan al aparecer o cambiar. */
private fun androidx.compose.foundation.lazy.LazyListScope.seccion(
    titulo: String,
    visitas: List<VisitaResumen>,
    mostrarTecnico: Boolean,
    mostrarDia: Boolean,
    navController: NavController,
    vacio: String
) {
    item(key = "titulo_$titulo") {
        Text(titulo.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
    if (visitas.isEmpty()) item(key = "vacio_$titulo") { Text(vacio, style = MaterialTheme.typography.bodyMedium) }
    items(visitas, key = { it.id }) { visita ->
        TarjetaVisita(
            visita = visita,
            mostrarTecnico = mostrarTecnico,
            mostrarDia = mostrarDia,
            onClick = { navController.navigate(Rutas.visita(visita.id)) },
            modifier = Modifier.animateItem()
        )
    }
}