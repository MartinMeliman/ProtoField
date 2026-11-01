package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.model.Criterio
import cl.duoc.protofield.model.TiposEquipo
import cl.duoc.protofield.navigation.siExiste
import cl.duoc.protofield.ui.components.CampoTexto
import cl.duoc.protofield.ui.components.MensajeError
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.TipoPruebaViewModel

/** PANTALLA 14 · NUEVA / EDITAR PRUEBA DEL CATÁLOGO (supervisor). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TipoPruebaFormScreen(navController: NavController, id: Long, tipoEquipo: String, viewModel: TipoPruebaViewModel = viewModel()) {
    LaunchedEffect(Unit) { viewModel.cargar(id.siExiste(), tipoEquipo.ifBlank { null }) }
    val estado by viewModel.estado.collectAsState()
    val usaMinimo = estado.criterio == Criterio.MINIMO || estado.criterio == Criterio.RANGO
    val usaMaximo = estado.criterio == Criterio.MAXIMO || estado.criterio == Criterio.RANGO

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra(if (estado.esEdicion) "Editar prueba" else "Nueva prueba del catálogo", estado.tipoEquipo) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tipo de equipo", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TiposEquipo.todos.forEach { tipo ->
                    FilterChip(
                        selected = estado.tipoEquipo == tipo,
                        onClick = { viewModel.onTipoEquipoChange(tipo) },
                        enabled = !estado.esEdicion,
                        label = { Text(tipo) },
                        leadingIcon = if (estado.tipoEquipo == tipo) { { Icon(Icons.Filled.Check, contentDescription = null) } } else null
                    )
                }
            }
            MensajeError(estado.errores.tipoEquipo)

            CampoTexto(estado.nombre, viewModel::onNombreChange, "Nombre de la prueba", estado.errores.nombre, icono = Icons.Filled.Tag)
            CampoTexto(estado.unidad, viewModel::onUnidadChange, "Unidad (opcional: MΩ, ms, %…)", null, icono = Icons.Filled.Straighten)

            Text("Se aprueba cuando el valor es…", style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Criterio.entries.forEachIndexed { i, c ->
                    SegmentedButton(
                        selected = estado.criterio == c,
                        onClick = { viewModel.onCriterioChange(c) },
                        shape = SegmentedButtonDefaults.itemShape(index = i, count = Criterio.entries.size)
                    ) { Text(c.etiqueta) }
                }
            }
            if (usaMinimo) {
                CampoTexto(estado.minimo, viewModel::onMinimoChange, if (estado.criterio == Criterio.RANGO) "Desde (mínimo)" else "Mayor o igual a",
                    estado.errores.minimo, icono = Icons.Filled.Tune, teclado = KeyboardType.Decimal, sufijo = estado.unidad.ifBlank { null })
            }
            if (usaMaximo) {
                CampoTexto(estado.maximo, viewModel::onMaximoChange, if (estado.criterio == Criterio.RANGO) "Hasta (máximo)" else "Menor o igual a",
                    estado.errores.maximo, icono = Icons.Filled.Tune, teclado = KeyboardType.Decimal, sufijo = estado.unidad.ifBlank { null })
            }
            if (estado.criterio == Criterio.LIBRE) {
                Text("Sin límite: el técnico indica en terreno si cumple o no.", style = MaterialTheme.typography.bodySmall)
            }

            if (estado.esEdicion) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Prueba activa", style = MaterialTheme.typography.bodyLarge)
                        Text("Si la desactivas, ya no aparece al registrar pruebas.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = estado.activo, onCheckedChange = viewModel::onActivoChange)
                }
                Card {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                        Text(
                            "Si cambias el criterio, las pruebas ya registradas mantienen el criterio con que se evaluaron. " +
                                    "Los protocolos entregados no cambian.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Button(
                onClick = { if (viewModel.validarFormulario()) viewModel.guardar(onGuardado = { navController.popBackStack() }) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (estado.esEdicion) "Guardar cambios" else "Agregar prueba") }
        }
    }
}