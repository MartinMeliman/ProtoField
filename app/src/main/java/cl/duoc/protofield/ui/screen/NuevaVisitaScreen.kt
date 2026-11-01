package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.navigation.siExiste
import cl.duoc.protofield.ui.components.CampoTexto
import cl.duoc.protofield.ui.components.ChipEstadoEquipo
import cl.duoc.protofield.ui.components.MensajeError
import cl.duoc.protofield.ui.components.SelectorLista
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.NuevaVisitaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** PANTALLA 11 · NUEVA VISITA (supervisor): a quién, dónde, cuándo y qué equipos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaVisitaScreen(navController: NavController, equipoId: Long, viewModel: NuevaVisitaViewModel = viewModel()) {
    LaunchedEffect(Unit) { viewModel.cargar(equipoId.siExiste()) }
    val estado by viewModel.estado.collectAsState()
    var mostrarCalendario by remember { mutableStateOf(false) } // estado de la VISTA (diálogo abierto)

    // El DatePicker entrega UTC: se formatea en UTC para no correr el día
    val formatoFecha = remember { SimpleDateFormat("dd-MM-yyyy", Locale("es", "CL")).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Nueva visita", "Le aparece al técnico en sus visitas") },
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SelectorLista(
                etiqueta = "Cliente",
                opciones = estado.clientes,
                seleccionado = estado.clientes.firstOrNull { it.id == estado.clienteId },
                textoDe = { it.razonSocial },
                onSeleccion = { viewModel.onClienteChange(it.id) },
                error = estado.errores.cliente,
                icono = Icons.Filled.Business
            )
            SelectorLista(
                etiqueta = if (estado.clienteId == null) "Primero elige el cliente" else "Instalación",
                opciones = estado.instalaciones,
                seleccionado = estado.instalaciones.firstOrNull { it.id == estado.instalacionId },
                textoDe = { "${it.nombre} (${it.comuna})" },
                onSeleccion = { viewModel.onInstalacionChange(it.id) },
                error = estado.errores.instalacion,
                icono = Icons.Filled.Place,
                habilitado = estado.clienteId != null
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Fecha: campo de solo lectura que abre el calendario al tocarlo
                Box(Modifier.weight(1.3f)) {
                    CampoTexto(
                        valor = estado.fecha?.let { formatoFecha.format(Date(it)) }.orEmpty(),
                        onValorChange = {},
                        etiqueta = "Fecha",
                        error = estado.errores.fecha,
                        icono = Icons.Filled.CalendarMonth,
                        soloLectura = true
                    )
                    // capa transparente encima para detectar el toque
                    Box(Modifier.matchParentSize().clickable { mostrarCalendario = true })
                }
                CampoTexto(
                    valor = estado.hora,
                    onValorChange = viewModel::onHoraChange,
                    etiqueta = "Hora",
                    error = estado.errores.hora,
                    icono = Icons.Filled.Schedule,
                    teclado = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            SelectorLista(
                etiqueta = "Técnico asignado",
                opciones = estado.tecnicos,
                seleccionado = estado.tecnicos.firstOrNull { it.id == estado.tecnicoId },
                textoDe = { it.nombre },
                onSeleccion = { viewModel.onTecnicoChange(it.id) },
                error = estado.errores.tecnico,
                icono = Icons.Filled.Person
            )

            Text("Equipos a probar", style = MaterialTheme.typography.labelLarge)
            if (estado.instalacionId == null) {
                Text("Elige la instalación para ver sus equipos.", style = MaterialTheme.typography.bodySmall)
            }
            estado.equiposInstalacion.forEach { equipo ->
                Card(Modifier.fillMaxWidth().clickable { viewModel.onEquipoToggle(equipo.id) }) {
                    Row(Modifier.padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = equipo.id in estado.equiposSeleccionados, onCheckedChange = { viewModel.onEquipoToggle(equipo.id) })
                        Column(Modifier.weight(1f)) {
                            Text(equipo.codigo, style = MaterialTheme.typography.titleSmall)
                            Text(equipo.tipo, style = MaterialTheme.typography.bodySmall)
                        }
                        ChipEstadoEquipo(equipo.estado, Modifier.padding(end = 8.dp))
                    }
                }
            }
            MensajeError(estado.errores.equipos)

            Button(
                onClick = { if (viewModel.validarFormulario()) viewModel.guardar(onGuardado = { navController.popBackStack() }) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Programar visita") }
        }
    }

    if (mostrarCalendario) {
        val calendario = rememberDatePickerState(initialSelectedDateMillis = estado.fecha)
        DatePickerDialog(
            onDismissRequest = { mostrarCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onFechaChange(calendario.selectedDateMillis)
                    mostrarCalendario = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { mostrarCalendario = false }) { Text("Cancelar") } }
        ) { DatePicker(state = calendario) }
    }
}