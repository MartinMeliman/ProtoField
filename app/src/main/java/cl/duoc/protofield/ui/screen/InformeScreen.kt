package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.model.Instrumentos
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.ui.components.Formato
import cl.duoc.protofield.ui.components.FotoEquipo
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.InformeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InformeScreen(navController: NavController, visitaId: Long, viewModel: InformeViewModel = viewModel()) {
    LaunchedEffect(visitaId) { viewModel.cargar(visitaId) }
    val estado by viewModel.estado.collectAsState()
    val detalle = estado.detalle
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Protocolo ${detalle?.visita?.numero.orEmpty()}", "Vista previa del informe") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (detalle == null) return@Scaffold
        val visita = detalle.visita
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFD7F5DD))) {
                Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF1B6E2E))
                    Text("Protocolo generado en terreno. El cliente lo recibe el mismo día.", color = Color(0xFF0B2914))
                }
            }

            // El "papel" del protocolo
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color(0xFF1A1C20)),
                border = BorderStroke(1.dp, Color(0xFFD5DBE6))
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PROTOCOLO DE PRUEBAS ELÉCTRICAS", fontWeight = FontWeight.Bold)
                    Text("Induztek Ingeniería SpA (datos ficticios) · ${visita.numero}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(color = Color(0xFF1F5FAF), thickness = 2.dp)
                    Linea("Cliente", "${detalle.clienteNombre} · RUT ${detalle.clienteRut}")
                    Linea("Instalación", "${detalle.instalacionNombre}, ${detalle.comuna}")
                    Linea("Fecha", Formato.fechaHora(visita.fechaHora))
                    Linea("Técnico", detalle.tecnicoNombre)
                    visita.temperatura?.let { t ->
                        Linea("Condiciones", "${Reglas.formatearNumero(t)} °C · ${visita.humedad?.let { Reglas.formatearNumero(it) } ?: "—"} % humedad")
                    }
                    if (visita.instrumentos.isNotBlank()) {
                        Linea("Instrumentos", visita.instrumentos.split(",").joinToString { Instrumentos.nombre(it) })
                    }
                    HorizontalDivider()
                    // Tabla de resultados
                    Row {
                        Text("Equipo", Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall)
                        Text("Prueba", Modifier.weight(1.6f), style = MaterialTheme.typography.labelSmall)
                        Text("Valor", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
                        Text("Res.", Modifier.weight(0.5f), style = MaterialTheme.typography.labelSmall)
                    }
                    estado.pruebas.forEach { fila ->
                        val p = fila.prueba
                        Row {
                            Text(fila.codigoEquipo, Modifier.weight(0.9f), style = MaterialTheme.typography.bodySmall)
                            Column(Modifier.weight(1.6f)) {
                                Text(p.tipoPrueba, style = MaterialTheme.typography.bodySmall)
                                Text(Reglas.textoCriterio(p.minimoUsado, p.maximoUsado, p.unidad), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                            Text("${Reglas.formatearNumero(p.valorMedido)} ${p.unidad}", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                            Text(
                                if (p.aprobada) "OK" else "NO", Modifier.weight(0.5f),
                                color = if (p.aprobada) Color(0xFF1B6E2E) else Color(0xFFB3261E), fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    HorizontalDivider()
                    Linea("Resumen", "${estado.totalEquipos} equipos, ${estado.pruebas.size} pruebas, ${estado.rechazadas} rechazada(s)")
                    if (visita.observacion.isNotBlank()) Linea("Observación", visita.observacion)
                    Linea("Recibido por", "${visita.recibidoPor} · ${visita.cerradaEn?.let { Formato.fechaHora(it) }.orEmpty()}")
                    visita.firmaPath?.let { FotoEquipo(ruta = it, modifier = Modifier.height(90.dp), descripcion = "Firma de ${visita.recibidoPor}", escala = ContentScale.Fit) }
                }
            }

            Button(
                onClick = { scope.launch { snackbar.showSnackbar("En la EFT: se genera el PDF y se comparte por correo o WhatsApp") } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Share, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Compartir PDF (EFT)")
            }
            OutlinedButton(
                onClick = {
                    navController.navigate(Rutas.VISITAS) {
                        popUpTo(Rutas.VISITAS) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Volver a las visitas") }
        }
    }
}

@Composable
private fun Linea(etiqueta: String, valor: String) {
    Row {
        Text("$etiqueta: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        Text(valor, style = MaterialTheme.typography.bodySmall)
    }
}