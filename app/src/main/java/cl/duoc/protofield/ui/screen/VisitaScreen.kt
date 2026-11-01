package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.data.local.entity.EquipoEnVisita
import cl.duoc.protofield.model.Instrumentos
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.ui.components.CampoTexto
import cl.duoc.protofield.ui.components.ChipEstadoVisita
import cl.duoc.protofield.ui.components.Formato
import cl.duoc.protofield.ui.components.MensajeError
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.VisitaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitaScreen(navController: NavController, visitaId: Long, viewModel: VisitaViewModel = viewModel()) {
    LaunchedEffect(visitaId) { viewModel.cargar(visitaId) }
    val estado by viewModel.estado.collectAsState()
    val detalle = estado.detalle

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra(detalle?.visita?.numero ?: "Visita", if (estado.estaCerrada) "Protocolo entregado" else "Visita técnica") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (detalle == null) return@Scaffold
        val visita = detalle.visita
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Encabezado
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(detalle.clienteNombre, style = MaterialTheme.typography.titleMedium)
                            ChipEstadoVisita(visita.estado)
                        }
                        Text("Instalación: ${detalle.instalacionNombre}, ${detalle.comuna}", style = MaterialTheme.typography.bodyMedium)
                        Text("Fecha: ${Formato.fechaHora(visita.fechaHora)}", style = MaterialTheme.typography.bodyMedium)
                        Text("Técnico: ${detalle.tecnicoNombre}", style = MaterialTheme.typography.bodyMedium)
                        if (!estado.estaCerrada) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                LinearProgressIndicator(
                                    progress = { if (estado.equipos.isEmpty()) 0f else estado.probados.toFloat() / estado.equipos.size },
                                    modifier = Modifier.weight(1f)
                                )
                                Text("${estado.probados} de ${estado.equipos.size} probados", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            if (!estado.estaCerrada) {
                // Condiciones de la visita (se anotan una sola vez)
                item {
                    val sinCondiciones = visita.temperatura == null
                    Card(
                        colors = if (sinCondiciones) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                        else CardDefaults.cardColors()
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Thermostat, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Condiciones de la visita", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    if (sinCondiciones) "Sin registrar. Se anotan una vez para todas las pruebas."
                                    else "${Reglas.formatearNumero(visita.temperatura ?: 0.0)} °C · ${visita.humedad?.let { Reglas.formatearNumero(it) } ?: "—"} % humedad · ${visita.instrumentos.replace(",", ", ")}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (estado.esTecnicoAsignado) {
                                TextButton(onClick = viewModel::abrirCondiciones) { Text(if (sinCondiciones) "Registrar" else "Editar") }
                            }
                        }
                    }
                }
                // Siguiente equipo pendiente
                val siguiente = estado.siguiente
                if (estado.esTecnicoAsignado && siguiente != null) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Bolt, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Siguiente: ${siguiente.equipo.codigo}", style = MaterialTheme.typography.titleSmall)
                                    Text("${siguiente.equipo.tipo} · ${siguiente.equipo.marca} ${siguiente.equipo.modelo}", style = MaterialTheme.typography.bodySmall)
                                }
                                Button(onClick = { navController.navigate(Rutas.prueba(siguiente.equipo.id, visitaId)) }) { Text("Probar") }
                            }
                        }
                    }
                }
            }

            item { Text("EQUIPOS A PROBAR", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(estado.equipos, key = { it.equipo.id }) { item ->
                FilaEquipo(item, onClick = { navController.navigate(Rutas.detalle(item.equipo.id, visitaId)) }, modifier = Modifier.animateItem())
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when {
                        estado.estaCerrada -> Button(onClick = { navController.navigate(Rutas.informe(visitaId)) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Description, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Ver protocolo entregado")
                        }
                        estado.esTecnicoAsignado -> {
                            OutlinedButton(onClick = { navController.navigate(Rutas.escaner(visitaId)) }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Filled.QrCodeScanner, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Escanear QR de un equipo")
                            }
                            OutlinedButton(onClick = { navController.navigate(Rutas.equipoNuevo(visitaId = visitaId)) }, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Filled.Add, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Agregar equipo no planificado")
                            }
                            Button(
                                onClick = { navController.navigate(Rutas.cerrar(visitaId)) },
                                enabled = estado.puedeCerrar,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Description, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Cerrar protocolo")
                            }
                            if (!estado.puedeCerrar) {
                                Text(
                                    "Faltan ${estado.faltan} equipo(s) por probar para poder cerrar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        else -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Lock, contentDescription = null)
                            Text("El protocolo lo cierra el técnico asignado, en terreno.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }

    // ---------- Hoja inferior: condiciones de la visita (formulario validado) ----------
    if (estado.mostrarCondiciones) {
        val c = estado.condiciones
        ModalBottomSheet(onDismissRequest = viewModel::cerrarCondiciones) {
            Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Condiciones de la visita", style = MaterialTheme.typography.titleLarge)
                Text("Se copian en todas las pruebas de la visita y en el protocolo.", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CampoTexto(
                        valor = c.temperatura, onValorChange = viewModel::onTemperaturaChange, etiqueta = "Temperatura",
                        error = c.errores.temperatura, icono = Icons.Filled.Thermostat, teclado = KeyboardType.Decimal, sufijo = "°C",
                        modifier = Modifier.weight(1f)
                    )
                    CampoTexto(
                        valor = c.humedad, onValorChange = viewModel::onHumedadChange, etiqueta = "Humedad",
                        error = c.errores.humedad, icono = Icons.Filled.WaterDrop, teclado = KeyboardType.Decimal, sufijo = "%",
                        modifier = Modifier.weight(1f)
                    )
                }
                Text("Instrumentos que vas a usar", style = MaterialTheme.typography.labelLarge)
                Instrumentos.todos.forEach { instrumento ->
                    Row(
                        Modifier.fillMaxWidth().clickable { viewModel.onInstrumentoToggle(instrumento.id) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = instrumento.id in c.instrumentos, onCheckedChange = { viewModel.onInstrumentoToggle(instrumento.id) })
                        Column {
                            Text(instrumento.nombre, style = MaterialTheme.typography.bodyLarge)
                            Text("Calibrado ${instrumento.calibracion}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                MensajeError(c.errores.instrumentos)
                Button(
                    onClick = { if (viewModel.validarCondiciones()) viewModel.guardarCondiciones() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Guardar condiciones") }
            }
        }
    }
}

@Composable
private fun FilaEquipo(item: EquipoEnVisita, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.equipo.codigo, style = MaterialTheme.typography.titleMedium)
                Text("${item.equipo.tipo} · ${item.equipo.marca} ${item.equipo.modelo}", style = MaterialTheme.typography.bodySmall)
            }
            when {
                item.pruebas == 0 -> Text("Pendiente", color = Color(0xFF7A4E00), style = MaterialTheme.typography.labelLarge)
                item.rechazadas > 0 -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Text(" ${item.rechazadas} rechazada(s)", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                }
                else -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF1B6E2E))
                    Text(" ${item.pruebas} prueba(s)", color = Color(0xFF1B6E2E), style = MaterialTheme.typography.labelLarge)
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
        }
    }
}