package cl.duoc.protofield.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cl.duoc.protofield.model.EstadosEquipo
import cl.duoc.protofield.model.EstadosVisita

/** Colores de cada estado. Siempre con ícono: el estado no depende solo del color (accesibilidad). */
private data class Estilo(val fondo: Color, val texto: Color, val icono: ImageVector?)

private fun estiloEquipo(estado: String) = when (estado) {
    EstadosEquipo.PENDIENTE -> Estilo(Color(0xFFFFE4B5), Color(0xFF5C3B00), Icons.Filled.Schedule)
    EstadosEquipo.OPERATIVO -> Estilo(Color(0xFFD7F5DD), Color(0xFF0B4A1E), Icons.Filled.Check)
    EstadosEquipo.MANTENCION -> Estilo(Color(0xFFD6E3FF), Color(0xFF0B2E66), Icons.Filled.Build)
    else -> Estilo(Color(0xFFF9DEDC), Color(0xFF7A1410), Icons.Filled.Block)
}

private fun estiloVisita(estado: String) = when (estado) {
    EstadosVisita.EN_CURSO -> Estilo(Color(0xFFD6E3FF), Color(0xFF0B2E66), null)
    EstadosVisita.CERRADA -> Estilo(Color(0xFFD7F5DD), Color(0xFF0B4A1E), null)
    else -> Estilo(Color(0xFFE4E8F0), Color(0xFF353C4A), null)
}

@Composable
private fun Pildora(texto: String, estilo: Estilo, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(estilo.fondo, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        estilo.icono?.let { Icon(it, contentDescription = null, tint = estilo.texto, modifier = Modifier.size(14.dp)) }
        Text(texto, color = estilo.texto, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun ChipEstadoEquipo(estado: String, modifier: Modifier = Modifier) = Pildora(estado, estiloEquipo(estado), modifier)

@Composable
fun ChipEstadoVisita(estado: String, modifier: Modifier = Modifier) = Pildora(estado, estiloVisita(estado), modifier)