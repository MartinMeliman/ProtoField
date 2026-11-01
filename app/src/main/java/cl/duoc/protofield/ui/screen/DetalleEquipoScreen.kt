package cl.duoc.protofield.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.data.device.GeneradorQr
import cl.duoc.protofield.data.local.entity.CambioEstadoEntity
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.ui.components.ChipEstadoEquipo
import cl.duoc.protofield.ui.components.Formato
import cl.duoc.protofield.ui.components.FotoEquipo
import cl.duoc.protofield.ui.components.GraficoTendencia
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.DetalleEquipoViewModel
import cl.duoc.protofield.viewmodel.Pestana
import cl.duoc.protofield.viewmodel.PruebaConComparacion

/**
 * PANTALLA 5 · DETALLE DEL EQUIPO.
 * Datos de la placa, estado, tendencia, historial de pruebas y de estados.
 * El supervisor ve "Cambiar estado" y "Eliminar"; el técnico ve "Nueva prueba".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEquipoScreen(
    navController: NavController,
    equipoId: Long,
    visitaId: Long,
    viewModel: DetalleEquipoViewModel = viewModel()
) {
    LaunchedEffect(equipoId) { viewModel.cargar(equipoId) }
    val estado by viewModel.estado.collectAsState()
    val datos = estado.equipo

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra(datos?.equipo?.codigo ?: "Equipo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.mostrarQr(true) }) { Icon(Icons.Filled.QrCode2, contentDescription = "Ver etiqueta QR") }
                    IconButton(onClick = { navController.navigate(Rutas.equipoEditar(equipoId)) }) { Icon(Icons.Filled.Edit, contentDescription = "Editar equipo") }
                    if (estado.esSupervisor) {
                        IconButton(onClick = viewModel::pedirEliminar) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar equipo") }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!estado.esSupervisor && datos != null) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Rutas.prueba(equipoId, visitaId)) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva prueba") }
                )
            }
        }
    ) { padding ->
        if (datos == null) return@Scaffold
        val equipo = datos.equipo
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(equipo.tipo, style = MaterialTheme.typography.titleLarge)
                    Text("${datos.clienteNombre} · ${datos.instalacionNombre}", style = MaterialTheme.typography.bodyMedium)
                    ChipEstadoEquipo(equipo.estado)
                    Text(
                        "Marca/modelo: ${equipo.marca.ifBlank { "—" }} ${equipo.modelo} · Serie: ${equipo.serie}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text("GPS: ${equipo.ubicacion ?: "sin ubicación"}", style = MaterialTheme.typography.bodySmall)
                    equipo.fotoPath?.let { FotoEquipo(ruta = it, modifier = Modifier.padding(top = 6.dp)) }
                }
            }

            // Aviso para el supervisor: última prueba rechazada y el estado no se ha revisado
            val rechazada = estado.rechazadaSinRevisar
            if (estado.esSupervisor && rechazada != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Text(
                                "Prueba rechazada el ${Formato.fecha(rechazada.fecha)}. El equipo sigue como «${equipo.estado}». Revisa si hay que cambiar su estado.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                if (estado.esSupervisor) {
                    OutlinedButton(onClick = { navController.navigate(Rutas.cambiarEstado(equipoId)) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Shield, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Cambiar estado")
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("El estado lo cambia el supervisor.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                TabRow(selectedTabIndex = estado.pestana.ordinal) {
                    Tab(selected = estado.pestana == Pestana.PRUEBAS, onClick = { viewModel.cambiarPestana(Pestana.PRUEBAS) },
                        text = { Text("Pruebas (${estado.historial.size})") })
                    Tab(selected = estado.pestana == Pestana.ESTADOS, onClick = { viewModel.cambiarPestana(Pestana.ESTADOS) },
                        text = { Text("Estados (${estado.cambios.size})") })
                }
            }

            // AnimatedContent: el contenido cambia con una transición al cambiar de pestaña
            item {
                AnimatedContent(targetState = estado.pestana, label = "pestana") { pestana ->
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (pestana == Pestana.PRUEBAS) {
                            estado.tendencia?.let { t ->
                                GraficoTendencia(t)
                                if (estado.esSupervisor && t.fechaCruce != null) {
                                    OutlinedButton(onClick = { navController.navigate(Rutas.nuevaVisita(equipoId)) }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Programar prueba preventiva")
                                    }
                                }
                            }
                            if (estado.historial.isEmpty()) Text("Este equipo aún no tiene pruebas registradas.")
                            estado.historial.forEach { item ->
                                TarjetaPrueba(item, onCorregir = { navController.navigate(Rutas.corregirPrueba(equipoId, item.prueba.id)) })
                            }
                        } else {
                            estado.cambios.forEach { FilaCambio(it) }
                        }
                    }
                }
            }
        }
    }

    // ---------- Diálogo: etiqueta QR ----------
    if (estado.mostrarQr && datos != null) {
        val codigo = datos.equipo.codigo
        val imagen = remember(codigo) { GeneradorQr.generar(GeneradorQr.textoPara(codigo)).asImageBitmap() }
        AlertDialog(
            onDismissRequest = { viewModel.mostrarQr(false) },
            title = { Text("Etiqueta de $codigo") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(bitmap = imagen, contentDescription = "Código QR de $codigo", modifier = Modifier.size(200.dp))
                    Text(codigo, style = MaterialTheme.typography.titleMedium)
                    Text("Se imprime y se pega en el equipo. Al escanearla, la app abre esta ficha.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.mostrarQr(false) }) { Text("Cerrar") } }
        )
    }

    // ---------- Diálogo: eliminar ----------
    estado.dialogoEliminar?.let { dialogo ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarEliminar,
            icon = { Icon(if (dialogo.sePuede) Icons.Filled.Delete else Icons.Filled.Warning, contentDescription = null) },
            title = { Text(if (dialogo.sePuede) "¿Eliminar ${datos?.equipo?.codigo}?" else "No se puede eliminar") },
            text = {
                Text(
                    if (dialogo.sePuede) "No tiene pruebas registradas. Esta acción no se puede deshacer."
                    else "Tiene ${dialogo.cantidadPruebas} prueba(s) registradas. Si lo borras, se pierde la trazabilidad. " +
                            "Para sacarlo de uso, cambia su estado a «Fuera de servicio»."
                )
            },
            confirmButton = {
                if (dialogo.sePuede) {
                    TextButton(onClick = { viewModel.confirmarEliminar(onEliminado = { navController.popBackStack() }) }) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(onClick = {
                        viewModel.cancelarEliminar()
                        navController.navigate(Rutas.cambiarEstado(equipoId))
                    }) { Text("Cambiar estado") }
                }
            },
            dismissButton = { TextButton(onClick = viewModel::cancelarEliminar) { Text(if (dialogo.sePuede) "Cancelar" else "Entendido") } }
        )
    }
}

@Composable
private fun TarjetaPrueba(item: PruebaConComparacion, onCorregir: () -> Unit) {
    val p = item.prueba
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (!p.aprobada || item.alerta) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        else CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (p.aprobada) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = if (p.aprobada) "Aprobada" else "Rechazada",
                    tint = if (p.aprobada) Color(0xFF1B6E2E) else MaterialTheme.colorScheme.error
                )
                Text(p.tipoPrueba, style = MaterialTheme.typography.titleSmall)
            }
            Text("${Reglas.formatearNumero(p.valorMedido)} ${p.unidad}", style = MaterialTheme.typography.headlineSmall)
            Text("Criterio: ${Reglas.textoCriterio(p.minimoUsado, p.maximoUsado, p.unidad)}", style = MaterialTheme.typography.bodySmall)
            item.variacion?.let { v ->
                Text(
                    "${Reglas.textoVariacion(v)} respecto a la medición anterior" + if (item.alerta) " · revisar" else "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (item.alerta) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (p.observaciones.isNotBlank()) Text("«${p.observaciones}»", style = MaterialTheme.typography.bodySmall)
            Text(
                "${Formato.fechaHora(p.fecha)} · ${p.tecnico} · ${Reglas.formatearNumero(p.temperaturaAmbiente)} °C",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Solo mientras la visita no esté cerrada (después es parte del protocolo firmado)
            if (item.corregible) {
                TextButton(onClick = onCorregir) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Text(" Corregir")
                }
            }
        }
    }
}

/** Una línea del historial de estados (como una línea de tiempo). */
@Composable
private fun FilaCambio(cambio: CambioEstadoEntity) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.padding(top = 4.dp).size(12.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
        Column {
            Text(cambio.estadoNuevo, style = MaterialTheme.typography.titleSmall)
            Text(cambio.motivo, style = MaterialTheme.typography.bodySmall)
            Text("${Formato.fechaHora(cambio.fecha)} · ${cambio.usuario}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}