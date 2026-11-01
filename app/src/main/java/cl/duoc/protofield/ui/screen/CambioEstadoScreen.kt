package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.model.EstadosEquipo
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.ui.components.CampoTexto
import cl.duoc.protofield.ui.components.ChipEstadoEquipo
import cl.duoc.protofield.ui.components.Formato
import cl.duoc.protofield.ui.components.MensajeError
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.CambioEstadoViewModel

/** PANTALLA 12 · CAMBIAR ESTADO (solo supervisor): estado nuevo + motivo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambioEstadoScreen(navController: NavController, equipoId: Long, viewModel: CambioEstadoViewModel = viewModel()) {
    LaunchedEffect(equipoId) { viewModel.cargar(equipoId) }
    val estado by viewModel.estado.collectAsState()
    val datos = estado.equipo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Cambiar estado", datos?.equipo?.codigo) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (datos == null) return@Scaffold
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(datos.equipo.codigo, style = MaterialTheme.typography.titleMedium)
                        ChipEstadoEquipo(datos.equipo.estado)
                    }
                    Text("${datos.equipo.tipo} · ${datos.instalacionNombre}", style = MaterialTheme.typography.bodySmall)
                    estado.ultimaPrueba?.let { p ->
                        Text(
                            "Última prueba: ${if (p.aprobada) "aprobada" else "RECHAZADA"} · ${p.tipoPrueba} " +
                                    "${Reglas.formatearNumero(p.valorMedido)} ${p.unidad} (${Formato.fecha(p.fecha)})",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (p.aprobada) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text("Nuevo estado", style = MaterialTheme.typography.labelLarge)
            // Lista de opciones con RadioButton (selectable hace que toda la fila se pueda tocar)
            EstadosEquipo.todos.forEach { opcion ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = estado.nuevoEstado == opcion, onClick = { viewModel.onEstadoChange(opcion) }, role = Role.RadioButton)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = estado.nuevoEstado == opcion, onClick = null)
                    Column(Modifier.padding(start = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ChipEstadoEquipo(opcion)
                            if (opcion == datos.equipo.estado) Text("  actual", style = MaterialTheme.typography.bodySmall)
                        }
                        Text(EstadosEquipo.descripcion(opcion), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            MensajeError(estado.errores.nuevoEstado)

            CampoTexto(
                valor = estado.motivo,
                onValorChange = viewModel::onMotivoChange,
                etiqueta = "Motivo del cambio (queda en el historial)",
                error = estado.errores.motivo,
                icono = Icons.Filled.Create,
                unaLinea = false
            )

            Button(
                onClick = { if (viewModel.validarFormulario()) viewModel.guardar(onGuardado = { navController.popBackStack() }) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar estado") }
        }
    }
}