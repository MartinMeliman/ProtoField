package cl.duoc.protofield.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import cl.duoc.protofield.data.device.LectorQr
import cl.duoc.protofield.navigation.Rutas
import cl.duoc.protofield.ui.components.CampoTexto
import cl.duoc.protofield.ui.components.TituloBarra
import cl.duoc.protofield.viewmodel.EscanerViewModel

/**
 * PANTALLA 3 · ESCANEAR QR.
 * El técnico apunta a la etiqueta pegada en el equipo y la app abre su ficha.
 * Animación: la línea que "barre" el visor (rememberInfiniteTransition).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EscanerScreen(navController: NavController, visitaId: Long, viewModel: EscanerViewModel = viewModel()) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // Si encuentra el equipo, abre su detalle y saca el escáner del historial
    val abrirEquipo: (Long) -> Unit = { equipoId ->
        navController.navigate(Rutas.detalle(equipoId, visitaId)) {
            popUpTo(Rutas.ESCANER) { inclusive = true }
        }
    }

    // Línea animada que sube y baja dentro del visor
    val transicion = rememberInfiniteTransition(label = "escaner")
    val posicion by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "linea"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { TituloBarra("Escanear equipo", "Etiqueta QR pegada en el equipo") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visor
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color(0xFF1B1F27), RoundedCornerShape(24.dp))
                    .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                Icon(
                    Icons.Filled.QrCode2, contentDescription = null, tint = Color.White.copy(alpha = 0.25f),
                    modifier = Modifier.size(140.dp).align(Alignment.Center)
                )
                Box(
                    Modifier
                        .offset(y = (16 + 184 * posicion).dp)
                        .width(180.dp)
                        .height(3.dp)
                        .background(Color(0xFF7FB2FF), RoundedCornerShape(2.dp))
                )
            }

            Button(
                onClick = { LectorQr(context).escanear { texto -> viewModel.onCodigoLeido(texto, abrirEquipo) } },
                enabled = !estado.buscando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Abrir cámara y escanear")
            }

            HorizontalDivider()
            Text("¿Etiqueta dañada? Escribe el código", style = MaterialTheme.typography.labelLarge, modifier = Modifier.fillMaxWidth())
            CampoTexto(
                valor = estado.codigoManual,
                onValorChange = viewModel::onCodigoManualChange,
                etiqueta = "Código del equipo (ej: TR-0001)",
                error = estado.error,
                icono = Icons.Filled.Tag
            )
            OutlinedButton(onClick = { viewModel.buscarManual(abrirEquipo) }, enabled = !estado.buscando, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Buscar equipo")
            }
        }
    }

    // Diálogo: el código no existe → ofrecer registrarlo con el código ya escrito
    estado.noRegistrado?.let { codigo ->
        AlertDialog(
            onDismissRequest = viewModel::cerrarDialogo,
            icon = { Icon(Icons.Filled.QrCode2, contentDescription = null) },
            title = { Text("$codigo no está registrado") },
            text = { Text("Esta etiqueta no corresponde a ningún equipo guardado. Puedes registrarlo ahora.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cerrarDialogo()
                    navController.navigate(Rutas.equipoNuevo(visitaId = visitaId, codigo = codigo)) {
                        popUpTo(Rutas.ESCANER) { inclusive = true }
                    }
                }) { Text("Registrar equipo") }
            },
            dismissButton = { TextButton(onClick = viewModel::cerrarDialogo) { Text("Cancelar") } }
        )
    }
}