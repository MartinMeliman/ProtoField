package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.model.TiposEquipo
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.ui.components.BarraInferior
import cl.duoc.protofield.ui.components.MenuCuenta
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.CatalogoViewModel

/**
 * PANTALLA 13 · CATÁLOGO DE PRUEBAS (supervisor).
 * Qué pruebas lleva cada tipo de equipo y cuándo se aprueban. Se edita sin tocar el código.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(navController: NavController, viewModel: CatalogoViewModel = viewModel()) {
    val grupos by viewModel.grupos.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Catálogo de pruebas", "Lo administra el supervisor") },
                actions = { MenuCuenta(navController) }
            )
        },
        bottomBar = { BarraInferior(navController, actual = Rutas.CATALOGO) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(grupos.entries.toList(), key = { it.key }) { (tipoEquipo, pruebas) ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "$tipoEquipo (${TiposEquipo.prefijo(tipoEquipo)})",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { navController.navigate(Rutas.tipoPruebaNueva(tipoEquipo)) }) {
                                Icon(Icons.Filled.Add, contentDescription = null); Text(" Agregar")
                            }
                        }
                        pruebas.forEach { p ->
                            HorizontalDivider()
                            Row(
                                Modifier.fillMaxWidth().clickable { navController.navigate(Rutas.tipoPruebaEditar(p.id)) }.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    p.nombre + if (p.activo) "" else " · desactivada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (p.activo) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(Reglas.textoCriterio(p.minimo, p.maximo, p.unidad), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Editar")
                            }
                        }
                    }
                }
            }
        }
    }
}