package cl.duoc.protofield.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import cl.duoc.protofield.navigation.siExiste
import cl.duoc.protofield.ui.components.CampoTexto
import cl.duoc.protofield.ui.components.MensajeError
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.PruebaViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PruebaFormScreen(
    navController: NavController,
    equipoId: Long,
    visitaId: Long,
    viewModel: PruebaViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val tipo = estado.seleccionada
    val unidad = tipo?.unidad.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra(if (estado.esCorreccion) "Corregir prueba" else "Nueva prueba", listOf(estado.codigoEquipo, estado.numeroVisita).filter { it.isNotBlank() }.joinToString(" · ")) },
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
            // Al corregir se mantiene el tipo de prueba y el criterio que se usó ese día
            if (estado.esCorreccion) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(
                        "Estás corrigiendo una prueba ya guardada. Se mantienen la fecha, el técnico y el criterio de ese día.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Solo las pruebas del catálogo para este tipo de equipo
            Text("Prueba para ${estado.tipoEquipo.lowercase()}", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                estado.opciones.forEach { opcion ->
                    FilterChip(
                        selected = estado.tipoPruebaId == opcion.id,
                        onClick = { viewModel.onTipoPruebaChange(opcion.id) },
                        label = { Text(opcion.nombre) },
                        leadingIcon = if (estado.tipoPruebaId == opcion.id) {
                            { Icon(Icons.Filled.Check, contentDescription = null) }
                        } else null
                    )
                }
            }
            MensajeError(estado.errores.tipoPrueba)

            if (estado.esLibre) {
                CampoTexto(estado.nombreLibre, viewModel::onNombreLibreChange, "Nombre de la prueba", estado.errores.nombreLibre, icono = Icons.Filled.Tag)
            }

            if (tipo != null) {
                Text(
                    (estado.valorAnterior?.let { "Medición anterior: ${Reglas.formatearNumero(it)} $unidad · " } ?: "Sin mediciones anteriores · ") +
                            "Criterio: ${Reglas.textoCriterio(tipo.minimo, tipo.maximo, unidad)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            CampoTexto(
                valor = estado.valorMedido,
                onValorChange = viewModel::onValorMedidoChange,
                etiqueta = "Valor medido",
                error = estado.errores.valorMedido,
                icono = Icons.Filled.Star,
                teclado = KeyboardType.Decimal,
                sufijo = unidad.ifBlank { null }
            )

            if (estado.esLibre) {
                // Prueba libre: el técnico decide si cumple
                Text("Resultado", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = estado.resultadoManual == true, onClick = { viewModel.onResultadoManualChange(true) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Cumple") }
                    SegmentedButton(
                        selected = estado.resultadoManual == false, onClick = { viewModel.onResultadoManualChange(false) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("No cumple") }
                }
                MensajeError(estado.errores.resultado)
            } else {
                VeredictoEnVivo(aprobada = estado.aprobadaPreview, criterio = tipo?.let { Reglas.textoCriterio(it.minimo, it.maximo, unidad) }.orEmpty())
            }

            // Aviso si cambió mucho respecto a la medición anterior
            AnimatedVisibility(visible = Reglas.esAlerta(estado.variacion)) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                    Text(
                        "Cambió ${estado.variacion?.let { Reglas.textoVariacion(it) }.orEmpty()} respecto a la medición anterior. " +
                                "Revisa la conexión del instrumento antes de guardar.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Si la visita tiene condiciones, la temperatura ya viene de ahí
            val condiciones = estado.condicionesVisita
            if (condiciones != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Thermostat, contentDescription = null)
                    Text(" Condiciones de la visita: $condiciones", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                CampoTexto(
                    valor = estado.temperatura,
                    onValorChange = viewModel::onTemperaturaChange,
                    etiqueta = "Temperatura ambiente",
                    error = estado.errores.temperatura,
                    icono = Icons.Filled.Thermostat,
                    teclado = KeyboardType.Decimal,
                    sufijo = "°C"
                )
            }

            CampoTexto(
                valor = estado.observaciones,
                onValorChange = viewModel::onObservacionesChange,
                etiqueta = if (estado.esRechazada) "Observaciones (obligatorio)" else "Observaciones (opcional)",
                error = estado.errores.observaciones,
                icono = Icons.Filled.Create,
                unaLinea = false
            )

            CampoTexto(
                valor = estado.tecnico,
                onValorChange = {},
                etiqueta = "Técnico responsable (de tu sesión)",
                error = null,
                icono = Icons.Filled.Person,
                soloLectura = true
            )

            Button(
                onClick = {
                    if (viewModel.validarFormulario()) {
                        viewModel.guardar(onGuardado = {
                            // Si venía de una visita, vuelve a la visita para seguir con el siguiente equipo
                            val volvioAVisita = !estado.esCorreccion && estado.visitaId != null &&
                                    navController.popBackStack(Rutas.VISITA, inclusive = false)
                            if (!volvioAVisita) navController.popBackStack()
                        })
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (estado.esCorreccion) "Guardar corrección" else "Guardar prueba") }
        }
    }
}

/** Tarjeta que muestra si la medición cumple MIENTRAS se escribe. */
@Composable
private fun VeredictoEnVivo(aprobada: Boolean?, criterio: String) {
    val colorFondo by animateColorAsState(
        targetValue = when (aprobada) {
            true -> Color(0xFFD7F5DD)
            false -> MaterialTheme.colorScheme.errorContainer
            null -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 400),
        label = "colorVeredicto"
    )
    AnimatedVisibility(visible = aprobada != null, enter = fadeIn() + scaleIn(), exit = fadeOut()) {
        Card(colors = CardDefaults.cardColors(containerColor = colorFondo), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    if (aprobada == true) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = if (aprobada == true) Color(0xFF1B6E2E) else MaterialTheme.colorScheme.error
                )
                Text(
                    (if (aprobada == true) "Cumple" else "NO cumple") + " el criterio ($criterio)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (aprobada == true) Color(0xFF0B2914) else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}