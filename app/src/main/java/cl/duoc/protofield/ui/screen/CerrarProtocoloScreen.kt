package cl.duoc.protofield.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import cl.duoc.protofield.model.Reglas
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.ui.components.AreaFirma
import cl.duoc.protofield.ui.components.CampoTexto
import cl.duoc.protofield.ui.components.MensajeError
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.CerrarProtocoloViewModel

/** PANTALLA 8 · CERRAR PROTOCOLO: resumen + quién recibe + firma del cliente. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CerrarProtocoloScreen(navController: NavController, visitaId: Long, viewModel: CerrarProtocoloViewModel = viewModel()) {
    LaunchedEffect(visitaId) { viewModel.cargar(visitaId) }
    val estado by viewModel.estado.collectAsState()
    val detalle = estado.detalle

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Cerrar protocolo", listOfNotNull(detalle?.visita?.numero, detalle?.clienteNombre).joinToString(" · ")) },
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
            // Resumen
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Cifra("${estado.totalEquipos}", "equipos", Modifier.weight(1f))
                Cifra("${estado.pruebas.size}", "pruebas", Modifier.weight(1f))
                Cifra("${estado.rechazadas.size}", "rechazadas", Modifier.weight(1f), if (estado.rechazadas.isEmpty()) null else MaterialTheme.colorScheme.error)
            }
            if (estado.rechazadas.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        estado.rechazadas.forEach {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Text(
                                    " ${it.codigoEquipo} · ${it.prueba.tipoPrueba}: ${Reglas.formatearNumero(it.prueba.valorMedido)} ${it.prueba.unidad}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }

            CampoTexto(estado.observacion, viewModel::onObservacionChange, "Observación general para el cliente", estado.errores.observacion,
                icono = Icons.Filled.Create, unaLinea = false)
            CampoTexto(estado.recibidoPor, viewModel::onRecibidoPorChange, "Nombre de quien recibe (cliente)", estado.errores.recibidoPor,
                icono = Icons.Filled.Person)
            CampoTexto(estado.correoCliente, viewModel::onCorreoChange, "Correo para enviar el protocolo", estado.errores.correoCliente,
                icono = Icons.Filled.Email, teclado = KeyboardType.Email)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Firma de quien recibe", style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = viewModel::borrarFirma) { Text("Borrar") }
            }
            AreaFirma(
                trazos = estado.trazos,
                hayError = estado.errores.firma != null,
                onInicio = viewModel::onFirmaInicio,
                onMovimiento = viewModel::onFirmaMovimiento,
                onTamano = viewModel::onTamanoFirma
            )
            MensajeError(estado.errores.firma)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                Text("Al cerrar, las pruebas de esta visita quedan bloqueadas.", style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (viewModel.validarFormulario()) {
                        viewModel.cerrar(onCerrado = {
                            // Muestra el informe y saca el formulario del historial
                            navController.navigate(Rutas.informe(visitaId)) {
                                popUpTo(Rutas.CERRAR) { inclusive = true }
                            }
                        })
                    }
                },
                enabled = !estado.guardando,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (estado.guardando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                else Text("Cerrar y generar protocolo")
            }
        }
    }
}

@Composable
private fun Cifra(valor: String, texto: String, modifier: Modifier = Modifier, color: Color? = null) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(valor, style = MaterialTheme.typography.headlineSmall, color = color ?: MaterialTheme.colorScheme.onSurface)
            Text(texto, style = MaterialTheme.typography.bodySmall)
        }
    }
}